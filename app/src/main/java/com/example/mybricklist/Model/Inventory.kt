package com.example.mybricklist.Model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

class Inventory()  {

    var id: Int = 0;
    var name: String = "";
    var isActive: Boolean = true;
    @RequiresApi(Build.VERSION_CODES.O)
    var lastAccessed = LocalDateTime.now();


    constructor(_id:Int, _name:String, _isActive: Boolean, _lastAccessed: LocalDateTime) : this() {
        this.id = _id;
        this.name = _name;
        this.isActive = _isActive;
        this.lastAccessed = _lastAccessed;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTime(){
      //  this.lastAccessed = LocalDateTime.now();
    }

    fun Deactivate(){
        if(this.isActive){
            this.isActive=false;
        }
    }

    fun Activate(){
        if(!this.isActive){
            this.isActive=true;
        }
    }

    override fun toString(): String {
        return "Inventory(id=$id, name='$name', isActive=$isActive, lastAccessed=$lastAccessed)";
    }


}