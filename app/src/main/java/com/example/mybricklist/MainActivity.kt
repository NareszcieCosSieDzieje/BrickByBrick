package com.example.mybricklist

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Model.Inventory
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.time.LocalDateTime


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
        val id = inventory.id;
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            delete = true;
            Toast.makeText(applicationContext, "Usuwanie projektu.", Toast.LENGTH_SHORT).show();
            val db = dbHandler(this);
            db.deleteInventory(inventory.id);

            val fileType1 = "pictures";
            val fileType2 = "SavedXMLS";
            val folder: File? = this.getExternalFilesDir(fileType2);
            if(folder != null){
                val fileName: String = folder.path + "/${id}.xml";
                val myFile = File(fileName)
                if (myFile.exists()) {
                    myFile.delete()
                }
            }
            val picPath = this.getExternalFilesDir(fileType1);
            var folderId = File(picPath.toString()+ '/' + id.toString());
            deleteRecursive(folderId, true);
            updateList()
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

        deleteAllButton.setOnClickListener{

            var dialog:AlertDialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Usuwanie projektów.")
            builder.setMessage("Czy chcesz usunąć wszystkie projekty?.")
            val dialogClickListener = DialogInterface.OnClickListener{_,which ->
                var shouldDelete = false;
                when(which){
                    DialogInterface.BUTTON_POSITIVE -> shouldDelete = true;
                    DialogInterface.BUTTON_NEGATIVE -> shouldDelete = false;
                    DialogInterface.BUTTON_NEUTRAL -> shouldDelete = false;
                }
                if(shouldDelete){
                    db.clearAll();
                    val fileType1 = "pictures";
                    val fileType2 = "SavedXMLS";
                    val picPath = this.getExternalFilesDir(fileType1);
                    val xmlPath = this.getExternalFilesDir(fileType2);
                    var fileA = File(picPath.toString());
                    deleteRecursive(fileA, false);
                    var fileB = File(xmlPath.toString());
                    deleteRecursive(fileB, false);
                    updateList();
                }
            }
            builder.setPositiveButton("YES", dialogClickListener)
            builder.setNegativeButton("NO", dialogClickListener)
            builder.setNeutralButton("CANCEL", dialogClickListener)
            dialog = builder.create()
            dialog.show()
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateList(){
        val db = dbHandler(this);
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
    }

    private fun deleteRecursive(fileOrDirectory: File, inclusive: Boolean ) {
        if (fileOrDirectory.isDirectory()){
            var iter = fileOrDirectory.listFiles().iterator();
            for (item in iter){
                deleteRecursive(item, true);
            }
        }
        if(inclusive){
            fileOrDirectory.delete();
        }
    }



}
