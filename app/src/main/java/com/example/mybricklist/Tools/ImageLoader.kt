package com.example.mybricklist.Tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Model.InventoryParts
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageLoader(context: Context, dbHandler: dbHandler, inventoryPartslist: MutableList<InventoryParts>) : AsyncTask<String, Int?, String?>() {

    val fileType = "pictures";
    val legoLink = "https://www.lego.com/service/bricks/5/2/"; // +   code = 300126
    val bricklinkColorLink = "http://img.bricklink.com/P/"; //color/code.gif?
    val bricklinkNoColorLink = "https://www.bricklink.com/PL/"; //code.jpg

    val mCtx = context;
    val db = dbHandler;
    val list = inventoryPartslist;

    override fun doInBackground(vararg params: String?): String? {

        if(list == null || list.isEmpty()){
            return "Empty list error";
        }

        val invId = list[0].inventoryId;
        val path1 = mCtx.getExternalFilesDir(fileType).toString() + "/${invId}";
        val directory = File(path1);
        if (!directory.exists()) {
            directory.mkdir()
        } else {
            Log.e("FOLDER ERROR","Isnieje juz taki folder");
            return "Directory exists";
        }

        list.forEach lit@{
            val color = it.colorId.toString();
            val itemId = it.itemId;
            val specialId = it.id;
            //val invId = it.inventoryId;
            var code = "";
            code = db.getItemCode(it);
            val iterator = listOf(legoLink, bricklinkColorLink, bricklinkNoColorLink).listIterator()
            var linkOk = false;
            for (item in iterator) {
                var link = "";
                var path = "";
                if(item == legoLink){
                    if(code == ""){
                        continue;
                    }
                    link = legoLink + code;
                    path = link;
                } else if (item == bricklinkColorLink){
                    link = bricklinkColorLink + color + '/' + itemId;  //+ ".gif";
                    path = link + ".gif";
                } else if (item == bricklinkNoColorLink){
                    link = bricklinkNoColorLink + itemId;
                    path = link + ".jpg";
                }

                var `in`: InputStream? = null
                var bmp: Bitmap? = null
                var responseCode = -1
                try {
                    val url = URL(path);
                    val con: HttpURLConnection = url.openConnection() as HttpURLConnection;
                    con.setDoInput(true)
                    con.connect()
                    responseCode = con.getResponseCode()
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        linkOk = true;
                        //download
                        `in` = con.getInputStream()
                        bmp = BitmapFactory.decodeStream(`in`)
                        `in`.close();
                        File(directory, "${specialId}_${itemId}.jpg").writeBitmap(bmp, Bitmap.CompressFormat.JPEG, 85)

                    }
                    if(linkOk){
                        it.photoPath = path1 + "/${specialId}_${itemId}.jpg";
                        db.updateInventoryPartsPhotoPath(it);
                        break;
                    }
                } catch (ex: Exception) {
                    Log.e("Exception", ex.toString())
                }
            }
        }
        return "Success";
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Toast.makeText(mCtx, "Dodano zdjęcia!", Toast.LENGTH_SHORT).show();
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values);
//        var xd = ".";
//        Toast.makeText(mCtx, xd, Toast.LENGTH_SHORT).show();
//        xd += "."
//        if(xd.length == 4){
//            xd = "."
//        }
    }

    override fun onPreExecute() {
        super.onPreExecute();
        Toast.makeText(mCtx, "Dodawanie zdjęć!", Toast.LENGTH_SHORT).show();
    }


    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }


}
