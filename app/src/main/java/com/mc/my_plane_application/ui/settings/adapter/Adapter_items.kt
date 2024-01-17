package com.mc.my_plane_application.ui.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mc.my_plane_application.R
import com.mc.my_plane_application.ui.settings.data.SettingsItem

class Adapter_items(private val itemList: Array<SettingsItem>) : RecyclerView.Adapter<Adapter_items.ViewHolder>() {

    // Comment s'affiche ma vue
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun showItem(item: SettingsItem) {
            itemView.findViewById<TextView>(R.id.title).text = item.name
            itemView.findViewById<ImageView>(R.id.button_item).setImageResource(item.icon)

            itemView.setOnClickListener(){
                    item.onClick()
            }
        }
    }

    // Retourne une « vue » / « layout » pour chaque élément de la liste
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    // Connect la vue ET la données
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.showItem(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}


