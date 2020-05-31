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
    val bricklinkColorLink = "http://img.bricklink.com/P/7/3001old.gif";
    val bricklinkNoColorLink = "https://www.bricklink.com/PL/3430c02.jpg";

    val mCtx = context;
    val db = dbHandler;
    val list = inventoryPartslist;

    override fun doInBackground(vararg params: String?): String? {

        //TODO: for each list for each link if prev not success

        list.forEach{

            val color = it.colorId.toString();

            listOf(legoLink, bricklinkColorLink, bricklinkNoColorLink).forEach{
                var link = "";
                if(it == legoLink){
                    link = legoLink + elementCode;
                } else if (it == bricklinkColorLink){
                    link = bricklinkNoColorLink + elementCode;      //+ ".jpg"; //TODO: jpg|gif?
                } else if (it == bricklinkNoColorLink){
                    link = bricklinkColorLink + color + elementCode; //+ ".jpg"; //TODO: to samo gif czy jpg
                }

                //val path = params[0]; //FIXME:

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
                        //download
                        `in` = con.getInputStream()
                        bmp = BitmapFactory.decodeStream(`in`)
                        `in`.close()
                        File(mCtx.getExternalFilesDir(fileType), nazwa_pliku).writeBitmap(bmp, Bitmap.CompressFormat.JPEG, 85)
                    }
                } catch (ex: Exception) {
                    Log.e("Exception", ex.toString())
                }

            }
        }
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }


}
