package com.example.mybricklist.Tools

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.AsyncTask.execute
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.ItemActivity
import com.example.mybricklist.MainActivity
import com.example.mybricklist.Model.Inventory
import com.example.mybricklist.Model.InventoryParts
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.net.ConnectException
import java.net.URL
import java.net.UnknownHostException
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class XML_Helper(db: dbHandler, context: Context) : AsyncTask<String, Int?, String?>(){

    val db = db;
    val mCtx = context;
    var message = "Dodano projekt!";
    var shouldDisplay = false;
    var invId = 0;

    var successEndState = false;

    override fun onPreExecute() {
        super.onPreExecute()
        Toast.makeText(mCtx, "Dodawanie projektu!", Toast.LENGTH_SHORT).show();
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result);
        Toast.makeText(mCtx, message, Toast.LENGTH_SHORT).show();
        if(successEndState) {
            val img = ImageLoader(mCtx, db, db.getAllInventoryParts(invId)).execute(*arrayOf(""));
        }
    }

//    override fun onProgressUpdate(vararg values: Int?) {
//        super.onProgressUpdate(*values)
//    }

    @SuppressLint("WrongThread")
    override fun doInBackground(vararg params: String?): String {
        var endState = true;
        var id: Int = 0;
        var name: String = "";
        try{

            id = params[1]!!.toInt();
            invId = id;
            name = params[2]!!;

            val url = URL(params[0]);
            val connection = url.openConnection();
            connection.connect();
            val isStream = url.openStream();
            var text: String? = null;

        //    val inputAsString = isStream.bufferedReader().use { it.readText() }
           // Log.d("INPUT_STREAM", inputAsString);

            var inventory = Inventory();
            inventory.id = id;
            inventory.name = name;

            val unique = db.addInventory(inventory);
            if(!unique){
                throw BadIdException("Inventory ID already in use!");
            }

            val factory = XmlPullParserFactory.newInstance();
            factory.isNamespaceAware = true;
            val parser = factory.newPullParser();
            parser.setInput(isStream, null);
            var eventType = parser.eventType;
            var inventoryPart: InventoryParts? = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                var tagname = parser.name;
                var addItem: Boolean = false;
                when (eventType) {
                    XmlPullParser.START_TAG -> if (tagname.equals("ITEM", ignoreCase = true)) {
                        inventoryPart = InventoryParts();
                    } else {}
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG -> if (tagname.equals("ITEM", ignoreCase = true)) {

                    } else if (tagname.equals("ITEMTYPE", ignoreCase = true)) {
                        inventoryPart!!.typeId = text.toString();
                    } else if (tagname.equals("ITEMID", ignoreCase = true)) {
                        inventoryPart!!.itemId = text.toString();
                    }
                    else if (tagname.equals("QTY", ignoreCase = true)) {
                       inventoryPart!!.quantityInSet = Integer.parseInt(text.toString());
                    }
                    else if (tagname.equals("COLOR", ignoreCase = true)) {
                        inventoryPart!!.colorId = Integer.parseInt(text.toString());
                    }
                    else if (tagname.equals("EXTRA", ignoreCase = true)) {
                        inventoryPart!!.extra = (text?.toIntOrNull()?.let { it } ?: 0) as Int
                    }
                    else if (tagname.equals("ALTERNATE", ignoreCase = true)) {
                        val alt: String = text.toString();
                        addItem = true;
                        if(alt != "N"){
                            addItem = false;
                        }
                        endState = false;
                    }
                    else if (tagname.equals("MATCHID", ignoreCase = true)) {
                        //DO NOTHING
                    }
                    else if (tagname.equals("COUNTERPART", ignoreCase = true)) {
                        //DO NOTHING
                    }
                    else -> {
                    }
                }
                if(addItem){
                    inventoryPart!!.inventoryId = inventory.id;
                    inventoryPart!!.quantityInStore = 0;
                    inventoryPart!!.photoPath = "";
                    inventoryPart?.let {
                        Log.d("INVENTORY PART", inventoryPart.toString());
                        db.addInventoryParts(inventoryPart);
                    }
                    addItem = false;
                }
                eventType = parser.next()
            }
            isStream.close();
        } catch (ce: ConnectException) {
            this.message = "Błąd połączenia!";
            this.shouldDisplay = true;
            endState=true;
            ce.printStackTrace()
        } catch(uh: UnknownHostException){
            this.message = "Niepoprawny URL, sprawdź ustawienia!";
            this.shouldDisplay = true;
            endState=true;
            uh.printStackTrace()
        }
        catch (e: XmlPullParserException) {
            e.printStackTrace();
        } catch (e: IOException) {
            e.printStackTrace();
        } catch (be: BadIdException){
            this.message = "Istnieje już projekt o takim numerze!";
            this.shouldDisplay = true;
            be.printStackTrace();
        } catch(ne: IllegalArgumentException) {
            this.message = "Nie podano numeru zestawu!";
            this.shouldDisplay = true;
            ne.printStackTrace();
        } finally {
            if(endState && !shouldDisplay){
                this.message = "Błąd dodawania projektu! Sprawdź numer zestawu LEGO";
            } else if (!shouldDisplay) {
                successEndState = true;
            }
        }

        return "Success";
    }
}

class BadIdException(message:String): Exception(message)