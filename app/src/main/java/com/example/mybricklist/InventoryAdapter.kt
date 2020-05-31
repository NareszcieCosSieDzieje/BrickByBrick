package com.example.mybricklist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mybricklist.DataBaseHandler.dbHandler
import com.example.mybricklist.Model.Inventory
import kotlinx.android.synthetic.main.custom_project_item.view.*

class InventoryAdapter(val context: Context, val items: List<Inventory>, dbHandler: dbHandler, val itemClickListener: OnItemClickListener): RecyclerView.Adapter<ViewHolder>() {

    var db = dbHandler;

    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.custom_project_item, parent, false), db);
    }

    // Binds each Inventory in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var inv = items.get(position);
        holder.bind(inv, itemClickListener); //items.get(position)
    }

//    fun refresh(){
//        this.notifyDataSetChanged();
//    }

}

    class ViewHolder (view: View, dbHandler: dbHandler) : RecyclerView.ViewHolder(view) {
        val invName = view.projectNameTextView;
        val invID = view.projectIdTextView;
        val invArch = view.projectArchivedSwitch;
        var db = dbHandler;

        fun bind(inventory: Inventory, clickListener: OnItemClickListener)
        {
            invName.text = inventory.name;
            invID.text = inventory.id.toString();
            invArch.isChecked = inventory.isActive;

            invArch?.setOnCheckedChangeListener { _, isChecked ->
                inventory.isActive = isChecked;
                db.updateInventory(inventory);
            }

            itemView.setOnClickListener {
                clickListener.onItemClicked(inventory)
            }

            itemView.setOnLongClickListener{
                clickListener.onItemLongClicked(inventory);
            }
        }
    }


interface OnItemClickListener{
    fun onItemClicked(inventory: Inventory);
    fun onItemLongClicked(inventory: Inventory): Boolean;
}

