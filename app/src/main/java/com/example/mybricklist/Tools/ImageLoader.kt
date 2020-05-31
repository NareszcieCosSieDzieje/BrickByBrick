package com.example.mybricklist.Tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Model.InventoryParts
import java.io.BufferedInputStream
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

        //TODO: for each list for each link if prev not success

        list.forEach lit@{
            val color = it.colorId.toString();
            val itemId = it.itemId;
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
                    link = bricklinkColorLink + color + '/' + itemId;  //+ ".gif"; //TODO: jpg|gif?
                    path = link + ".gif";
                } else if (item == bricklinkNoColorLink){
                    link = bricklinkNoColorLink + itemId; //TODO: to samo gif czy jpg
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
                        `in`.close()
                        File(mCtx.getExternalFilesDir(fileType), "${itemId}_${code}.jpg").writeBitmap(bmp, Bitmap.CompressFormat.JPEG, 85)
                    }
                    if(linkOk){
                        it.photoPath = mCtx.getExternalFilesDir(fileType).toString() + "/${itemId}_${code}.jpg";
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

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }


}
