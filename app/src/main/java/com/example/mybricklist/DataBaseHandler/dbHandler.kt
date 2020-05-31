package com.example.mybricklist.DataBaseHandler

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.mybricklist.Model.Inventory
import com.example.mybricklist.Model.InventoryParts
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime


class dbHandler(context: Context) :
                SQLiteOpenHelper(context, dbHandler.DATABASE_NAME,
                                 null, dbHandler.DATABASE_VERSION) {

    val mCtx: Context = context;
   // val database: SQLiteDatabase;


    companion object {
        private val DATABASE_VERSION = 1;
        private val DATABASE_NAME = "BrickList";
        private val DATABASE_PATH = "databases";

        private val INVENTORY_TABLE = "Inventories"
        private val INVENTORY_ID = "id";
        private val INVENTORY_NAME = "Name";
        private val INVENTORY_ACTIVE = "Active";
        private val INVENTORY_LASTACCESSED = "LastAccessed";

        private val INVENTORY_TABLE_PARTS = "InventoriesParts"
        private val INVENTORY_PARTS_ID = "id";
        private val INVENTORY_PARTS_INVENTORY_ID = "InventoryID";
        private val INVENTORY_PARTS_TYPE_ID = "TypeID";
        private val INVENTORY_PARTS_ITEM_ID = "ItemID";
        private val INVENTORY_PARTS_QUANTITY_IN_SET = "QuantityInSet";
        private val INVENTORY_PARTS_QUANTITY_IN_STORE = "QuantityInStore";
        private val INVENTORY_PARTS_COLOR_ID = "ColorID";
        private val INVENTORY_PARTS_EXTRA = "Extra";
    }



    private val preferences: SharedPreferences = mCtx.getSharedPreferences("PREFS",  Context.MODE_PRIVATE)


    private fun installedDatabaseIsOutdated(): Boolean {
        return preferences.getInt(DATABASE_NAME, 0) < DATABASE_VERSION
    }

    private fun writeDatabaseVersionInPreferences() {
        preferences.edit().apply {
            putInt(DATABASE_NAME, DATABASE_VERSION);
            apply();
        }
    }

    private fun installDatabaseFromAssets() {
        val inputStream = mCtx.assets.open("$DATABASE_PATH/$DATABASE_NAME.db")
        try {
            val outputFile = File(mCtx.getDatabasePath(DATABASE_NAME).path);
            val outputStream = FileOutputStream(outputFile);
            inputStream.copyTo(outputStream);
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (exception: Throwable) {
            throw RuntimeException("The $DATABASE_NAME database couldn't be installed.", exception)
        }
    }

    @Synchronized
    private fun installOrUpdateIfNecessary() {
        if (installedDatabaseIsOutdated()) {
            mCtx.deleteDatabase(DATABASE_NAME);
            installDatabaseFromAssets();
            writeDatabaseVersionInPreferences();
        }
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        installOrUpdateIfNecessary();
        return super.getWritableDatabase();
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        installOrUpdateIfNecessary();
        return super.getReadableDatabase();
    }



//    init {
//        database = open()
//    }

//
//
//    fun open(): SQLiteDatabase {
//        val dbFile = mCtx.getDatabasePath("$DATABASE_NAME.db")
//        if (!dbFile.exists()){
//            try {
//                mCtx.openOrCreateDatabase("$DATABASE_NAME.db", Context.MODE_PRIVATE,null)
//            } catch (e: IOException){
//                Log.d("error_db", "DATABASE_OPEN_ERROR");
//                throw RuntimeException("Error opening db")
//            }
//        }
//        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE);
//    }
//
//    override fun close() {
//        database.close()
//    }

   override fun onCreate(db: SQLiteDatabase) {
       //onCreate(db);
   }

   override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
       //onUpgrade(db, oldVersion, newVersion);
   }


   fun showAllTables(){

       val c: Cursor = this.readableDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
       if (c.moveToFirst()) {
           while (!c.isAfterLast) {
               Log.d("Table Name=> ", c.getString(0));
               c.moveToNext()
           }
       }


   }


    fun getDescription(_itemCode: String, _colorCode: Int): MutableList<String> {
        var desc = mutableListOf<String>();
        val cN = this.readableDatabase.rawQuery("SELECT * FROM Parts WHERE Code=?", arrayOf(_itemCode));
        if (cN!!.moveToFirst()) {
                desc.add(cN.getString(cN.getColumnIndex("Name")));
        } else {
            //?
            desc.add("Part name err");
        }
        val cC = this.readableDatabase.rawQuery("SELECT * FROM Colors WHERE Code=?", arrayOf(_colorCode.toString()));
        if (cC!!.moveToFirst()) {
            desc.add(cC.getString(cC.getColumnIndex("Name")));
        } else {
            //?
            desc.add("Color name err");
        }
        desc.add(_itemCode);
        return desc;
    }


   fun getAllCategories() {
       val c = this.readableDatabase.rawQuery("SELECT Name FROM Categories", null);
       c.moveToFirst();
       Log.d("code", c.getString(c.getColumnIndex("Code")) );
   }

   fun addInventory(inventory: Inventory): Boolean {
       val values: ContentValues = ContentValues();
       values.put(INVENTORY_ID, inventory.id);
       values.put(INVENTORY_NAME, inventory.name);
       val active =  if (inventory.isActive) 1 else 0;
       values.put(INVENTORY_ACTIVE, active);
       values.put(INVENTORY_LASTACCESSED, inventory.lastAccessed.toString());

       val _success: Boolean = this.writableDatabase.insert(INVENTORY_TABLE, null, values) > 0;
       return _success
   }

    fun updateInventory( inventory: Inventory ): Boolean{
        val values: ContentValues = ContentValues();
        values.put(INVENTORY_NAME, inventory.name); //These Fields should be your String values of actual column names
        val active =  if (inventory.isActive) 1 else 0;
        values.put(INVENTORY_ACTIVE, active);
        values.put(INVENTORY_LASTACCESSED, inventory.lastAccessed.toString()); //FIXME: CZY TO OKEJ?
        val _success:Boolean = this.writableDatabase.update(INVENTORY_TABLE, values,  "$INVENTORY_ID=?", arrayOf(inventory.id.toString())) > 0;
        return _success;
    }

    fun clearAll(){
        this.writableDatabase.delete(INVENTORY_TABLE, null, null);
        this.writableDatabase.delete(INVENTORY_TABLE_PARTS, null, null);
    }

    fun deleteInventory( _id: Int ): Boolean {
        var _success: Boolean = this.writableDatabase.delete(INVENTORY_TABLE, "$INVENTORY_ID=?", arrayOf(_id.toString())) > 0
        _success = this.deleteInventoryParts(_id);
        return _success;
    }

    fun addInventoryParts( inventoryParts: InventoryParts ): Boolean {
        val values: ContentValues = ContentValues();
        values.put(INVENTORY_PARTS_INVENTORY_ID, inventoryParts.inventoryId);
        values.put(INVENTORY_PARTS_TYPE_ID, inventoryParts.typeId);
        values.put(INVENTORY_PARTS_ITEM_ID, inventoryParts.itemId);
        values.put(INVENTORY_PARTS_QUANTITY_IN_SET, inventoryParts.quantityInSet);
        values.put(INVENTORY_PARTS_QUANTITY_IN_STORE, inventoryParts.quantityInStore);
        values.put(INVENTORY_PARTS_COLOR_ID, inventoryParts.colorId);
        values.put(INVENTORY_PARTS_EXTRA, inventoryParts.extra);
        val _success: Boolean = this.writableDatabase.insert(INVENTORY_TABLE_PARTS, null, values) > 0;
        return _success;
   }


    fun updateInventoryParts( inventoryParts: InventoryParts ): Boolean{
        val values: ContentValues = ContentValues();
        //values.put(INVENTORY_PARTS_INVENTORY_ID, inventoryParts.id);
        values.put(INVENTORY_PARTS_TYPE_ID, inventoryParts.typeId);
        values.put(INVENTORY_PARTS_ITEM_ID, inventoryParts.itemId);
        //values.put(INVENTORY_PARTS_QUANTITY_IN_SET, inventoryParts.quantityInSet);
        values.put(INVENTORY_PARTS_QUANTITY_IN_STORE, inventoryParts.quantityInStore);
        values.put(INVENTORY_PARTS_COLOR_ID, inventoryParts.colorId);
        values.put(INVENTORY_PARTS_EXTRA, inventoryParts.extra);
        val _success: Boolean = this.writableDatabase.update(INVENTORY_TABLE_PARTS, values,  "$INVENTORY_PARTS_ID=?", arrayOf(inventoryParts.id.toString())) > 0
        return _success;
    }

    fun deleteInventoryParts( _id: Int ): Boolean {
        val _success: Boolean = this.writableDatabase.delete(INVENTORY_TABLE_PARTS, "$INVENTORY_PARTS_ID=?", arrayOf(_id.toString())) > 0;
        return _success;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllInventories(): MutableList<Inventory> {
        val inventoryList = mutableListOf<Inventory>();
        var cursor: Cursor? = null
        try {
            cursor = this.readableDatabase.rawQuery("SELECT * FROM $INVENTORY_TABLE", null)
        } catch (e: SQLiteException) {
            Log.d("GET_INVENTORIES_ERROR", e.stackTrace.toString());
            throw RuntimeException("Error loading inventories!")
        }
        var id: Int;
        var name: String;
        var active: Boolean;
        var lastAccessed: LocalDateTime;
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getString(cursor.getColumnIndex(INVENTORY_ID)).toString().toInt();
                name = cursor.getString(cursor.getColumnIndex(INVENTORY_NAME));
                active = cursor.getString(cursor.getColumnIndex(INVENTORY_ACTIVE)).toInt() == 1;
                lastAccessed = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(INVENTORY_LASTACCESSED)));
                //TODO: print czy to działa wgl
                inventoryList.add(Inventory(id,name,active, lastAccessed))
                cursor.moveToNext()
            }
        }
        return inventoryList;
    }

    fun getAllInventoryParts( _inventoryId: Int ): MutableList<InventoryParts>  {
        val inventoryPartsList = mutableListOf<InventoryParts>();
        var cursor: Cursor? = null
        try {
            cursor = this.readableDatabase.rawQuery("SELECT * FROM $INVENTORY_TABLE_PARTS WHERE $INVENTORY_PARTS_INVENTORY_ID=$_inventoryId", null)
        } catch (e: SQLiteException) {
            throw RuntimeException("Error loading inventories_parts!")
        }

        var id: Int;
        var typeId: String;
        var itemId: String;
        var quantityInSet: Int;
        var quantityInStore: Int;
        var colorId: Int;
        var extra: Int;
        if (cursor!!.moveToFirst()) {
            while (cursor.isAfterLast == false) {
                id = cursor.getString(cursor.getColumnIndex(INVENTORY_PARTS_ID)).toString().toInt();
                typeId = cursor.getString(cursor.getColumnIndex(INVENTORY_PARTS_TYPE_ID)).toString();
                itemId = cursor.getString(cursor.getColumnIndex(INVENTORY_PARTS_ITEM_ID)).toString();
                quantityInSet = cursor.getString(cursor.getColumnIndex(INVENTORY_PARTS_QUANTITY_IN_SET)).toString().toInt();
                quantityInStore = cursor.getString(cursor.getColumnIndex(INVENTORY_PARTS_QUANTITY_IN_STORE)).toString().toInt();
                colorId = cursor.getString(cursor.getColumnIndex(INVENTORY_PARTS_COLOR_ID)).toString().toInt();
                extra = cursor.getString(cursor.getColumnIndex(INVENTORY_PARTS_EXTRA)).toString().toInt();

                inventoryPartsList.add(InventoryParts(id, _inventoryId, typeId, itemId, quantityInSet, quantityInStore, colorId, extra));
                cursor.moveToNext()
            }
        }
        return inventoryPartsList;
    }


   fun getInventoryIdByName(_name: String): Int {
       val inventoryPartsList = mutableListOf<InventoryParts>();
       var cursor: Cursor? = null
       var Id: Int = 0;
       try {
           cursor = this.readableDatabase.rawQuery("SELECT * FROM $INVENTORY_TABLE WHERE $INVENTORY_NAME=$_name", null)
       } catch (e: SQLiteException) {
           throw RuntimeException("Error loading inventory by name!")
       }
       if (cursor!!.moveToFirst()) {
           Id = cursor.getString(cursor.getColumnIndex(INVENTORY_ID)).toString().toInt();
       }
       return Id;
   }

//    fun closeDB(){
//        this.close();
//    }

}
