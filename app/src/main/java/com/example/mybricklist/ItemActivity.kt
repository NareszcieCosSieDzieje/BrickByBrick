package com.example.mybricklist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Tools.XML_Helper
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class ItemActivity : AppCompatActivity() {

    var nameOnce: Boolean = true;
    var numberOnce: Boolean = true;

    //FIXME: ? var db: dbHandler? = null;

    val XML_LINK = "XML_LINK";
    var URL_PREFIX = "http://fcds.cs.put.poznan.pl/MyWeb/BL/";
    val URL_SUFFIX = ".XML";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val db = dbHandler(this);


        addItemButton.setOnClickListener(){
            val xmlHelper = XML_Helper(db, this);
            val num = setNumberEditText.text.toString();
            val name = setNameEditText.text.toString();
            val preferences: SharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            URL_PREFIX = preferences.getString(XML_LINK, "http://fcds.cs.put.poznan.pl/MyWeb/BL/").toString();
            val URL = URL_PREFIX + num + URL_SUFFIX;
            xmlHelper.execute(*arrayOf(URL, num, name));
            Toast.makeText(this, "Dodawanie projektu!", Toast.LENGTH_SHORT).show();
            //TimeUnit.SECONDS.sleep(2L);
        }


        checkItemButton.setOnClickListener(){
            //TODO: DOROBIC
        }


        setNumberEditText.setOnClickListener{
            if(numberOnce){
                setNumberEditText.setText("")
                numberOnce = false;
            };
        }

        setNameEditText.setOnClickListener{
            if(nameOnce){
                setNameEditText.setText("")
                nameOnce = false;
            };
        }


        fromItemToMainImageButton.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
        }

    }
}
