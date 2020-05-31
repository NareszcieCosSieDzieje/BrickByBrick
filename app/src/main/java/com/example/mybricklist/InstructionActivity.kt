package com.example.mybricklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Model.InventoryParts
import com.example.mybricklist.Tools.XML_Writer
import kotlinx.android.synthetic.main.activity_instruction.*

class InstructionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)

        val inventoryID = intent.getIntExtra("LEGO_SET_NUM", 0);

        val db = dbHandler(this);

        //TODO: ustaw listenery na buttony i na liste

        var adapter =  InventoryPartsAdapter(this, R.layout.custom_item, db.getAllInventoryParts(inventoryID).sortedWith( compareBy{ it.quantityInStore-it.quantityInSet==0 } ), db); // instructionListView
        inventorypartsListView.setAdapter(adapter);

        goBackToMainFromInstructionButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
        }

        generateXMLButton.setOnClickListener{
            val xml = XML_Writer(db.getAllInventoryParts(inventoryID).filter { it.quantityInSet-it.quantityInStore > 0 } as MutableList<InventoryParts>, this);
            xml.saveXML();
            Toast.makeText(this, "Dodano plik xml brakujących części!", Toast.LENGTH_SHORT).show();
        }


    }



}

