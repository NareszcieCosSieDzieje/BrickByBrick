package com.example.mybricklist

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Model.InventoryParts


class InventoryPartsAdapter(var mCtx: Context, var resources: Int, var items: List<InventoryParts>, dbHandler: dbHandler): ArrayAdapter<InventoryParts>(mCtx, resources, items) {

    val fileType = "pictures";
    val db = dbHandler;
    var data = items;

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) : View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx);
        val view: View = layoutInflater.inflate(resources, null);

        val itemImageView: ImageView = view.findViewById(R.id.itemImageView);
        val descriptionTextView: TextView = view.findViewById(R.id.itemDescriptionTextView);
        val itemCountTextView: TextView = view.findViewById(R.id.itemCountTextView);
        val itemPlusButton: Button = view.findViewById(R.id.itemPlusButton);
        val itemMinusButton: Button = view.findViewById(R.id.itemMinusButton);

        var mItem: InventoryParts = items[position];

        if(mItem.photoPath != "") {
            val bmImg = BitmapFactory.decodeFile(mItem.photoPath);
            itemImageView.setImageBitmap(bmImg);
        }

        if(mItem.quantityInStore == mItem.quantityInSet){
            view.setBackgroundColor(Color.GREEN);
        }

        itemPlusButton.setOnClickListener(){
            if(mItem.quantityInStore < mItem.quantityInSet){
                mItem.quantityInStore++;
            }
            this.db.updateInventoryParts(mItem);
            if(mItem.quantityInStore == mItem.quantityInSet){
                view.setBackgroundColor(Color.GREEN);
            }
            this.notifyDataSetChanged();
        }

        itemMinusButton.setOnClickListener(){
            if(mItem.quantityInStore > 0){
                mItem.quantityInStore--;
            }
            this.db.updateInventoryParts(mItem);
            this.notifyDataSetChanged();
        }

        //itemImageView.setImageDrawable()
        var description = db.getDescription(mItem.itemId, mItem.colorId);
        description[description.lastIndex] = "[${description.lastOrNull()}]";
        var strDescription = description.toString();
        strDescription = strDescription.replace(',', '\n').substring(1, strDescription.length-1);
        descriptionTextView.setText(strDescription);
        itemCountTextView.setText("${mItem.quantityInStore} out of ${mItem.quantityInSet}");

        return view;
    }

}