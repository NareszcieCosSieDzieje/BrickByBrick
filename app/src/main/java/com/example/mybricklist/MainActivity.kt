package com.example.mybricklist

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Model.Inventory
import com.example.mybricklist.Tools.XML_Helper
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.jar.Manifest


class MainActivity : AppCompatActivity(), OnItemClickListener {

    val ARCHIVED_ITEMS = "ARCHIVED_ITEMS";
    var archived: Boolean = false;

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClicked(inventory: Inventory) {
        Toast.makeText(this,"Otwieranie instrukcji ${inventory.name}",Toast.LENGTH_LONG).show();
        inventory.lastAccessed = LocalDateTime.now();
        val db = dbHandler(this);
        db.updateInventory(inventory);

        val intent = Intent(this, InstructionActivity::class.java);
        intent.putExtra("LEGO_SET_NUM", inventory.id);
        startActivity(intent);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemLongClicked(inventory: Inventory): Boolean {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("USUWANIE PROJEKTU")
        builder.setMessage("Czy chcesz usunąć projekt z bazy danych?")
        var delete = false;

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            delete = true;
            Toast.makeText(applicationContext, "Usuwanie projektu.", Toast.LENGTH_SHORT).show();
            val db = dbHandler(this);
            db.deleteInventory(inventory.id);

            var inventories = db.getAllInventories();
            var filteredInventories = inventories.filter { (it.isActive) || (archived) };
            var sortedInventories = filteredInventories.sortedBy { it.lastAccessed }.reversed();

            mainRecycleView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mainRecycleView.adapter = InventoryAdapter(this, sortedInventories, db, this);
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            delete = false;
            Toast.makeText(applicationContext, "Anulowanie.", Toast.LENGTH_SHORT).show();
        }

        builder.show()
        return delete;
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val permissions = ContextCompat.checkSelfPermission(this, Manifest.)


        val preferences: SharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        val editor = preferences.edit();
        this.archived =  preferences.getBoolean(ARCHIVED_ITEMS, false);

        val db = dbHandler(this);
        //Log.d("EPIC_LIST", db.getAllInventories().toString());
        //db.showAllTables();

        //db.clearAll();
//
//        val xmlHelper = XML_Helper(db);
//        xmlHelper.execute(*arrayOf("http://fcds.cs.put.poznan.pl/MyWeb/BL/70403.xml", "70403", "Smocza Góra"));
//
//        TimeUnit.SECONDS.sleep(4L)

//        //db.showAllTables();
//
//        var l = db.getAllInventories();
//        var iterator = l.listIterator();
//        for (item in iterator) {
//            Log.d("ITEM1:", "$item");
//        }
//
//
//        var ll = db.getAllInventoryParts(70403);
//        var iteratorr = ll.listIterator();
//        for (itemm in iteratorr) {
//            Log.d("ITEM2:", "$itemm");
//        }


        var inventories = db.getAllInventories();
        var filteredInventories = inventories.filter { (it.isActive) || (archived) };
        var sortedInventories = filteredInventories.sortedBy { it.lastAccessed }.reversed();

        mainRecycleView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mainRecycleView.adapter = InventoryAdapter(this, sortedInventories, db, this);


        if(inventories.isEmpty()){
            Toast.makeText(this, "Brak projektów!", Toast.LENGTH_SHORT).show();
        } else if (filteredInventories.isEmpty()){
            Toast.makeText(this, "Istniejące projekty są nieaktywne!", Toast.LENGTH_SHORT).show();
        }


        goToSettingsButton.setOnClickListener(){
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent);
        }

        addSetInMainButton.setOnClickListener(){
            val intent = Intent(this, ItemActivity::class.java);
            startActivity(intent);
        }


    }






}
