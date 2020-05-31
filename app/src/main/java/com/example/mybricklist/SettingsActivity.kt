package com.example.mybricklist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    val ARCHIVED_ITEMS = "ARCHIVED_ITEMS";
    val XML_LINK = "XML_LINK";

    var URL_PREFIX = "http://fcds.cs.put.poznan.pl/MyWeb/BL/";
    val URL_SUFFIX = ".XML";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val preferences: SharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        val editor = preferences.edit();

        URL_PREFIX = preferences.getString(XML_LINK, "http://fcds.cs.put.poznan.pl/MyWeb/BL/").toString();
        urlSettingsEditView.setText(URL_PREFIX);

        archivedSettingsSwitch.isChecked = preferences.getBoolean(ARCHIVED_ITEMS, false);

        archivedSettingsSwitch.setOnCheckedChangeListener { _, isChecked ->

            //Log.d("IsChecked", isChecked.toString())
                editor.putBoolean(ARCHIVED_ITEMS, isChecked).apply();
        }

        backFromSettingsButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
        }

        urlSettingsEditView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                //if (urlSettingsEditView.hasFocus()) {
                    editor.putString(XML_LINK, urlSettingsEditView.text.toString()).apply();
                //}
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        setToDefaultButton.setOnClickListener{
            val defPref = "http://fcds.cs.put.poznan.pl/MyWeb/BL/";
            val defArchv = false;
            editor.putString(XML_LINK, defPref).apply();
            editor.putBoolean(ARCHIVED_ITEMS, defArchv).apply();
            urlSettingsEditView.setText(defPref);
            URL_PREFIX = defPref;
            archivedSettingsSwitch.isChecked = false
        }

    }
}
