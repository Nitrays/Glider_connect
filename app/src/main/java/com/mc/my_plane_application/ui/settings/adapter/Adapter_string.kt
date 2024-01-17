package com.mc.my_plane_application.ui.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mc.my_plane_application.R

class Adapter_string(private val deviceList: MutableList<String>, private val onClick: ((selectedDevice: String) -> Unit)? = null) : RecyclerView.Adapter<Adapter_string.ViewHolder>() {

    // Comment s'affiche ma vue
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun showItem(device: String, onClick: ((selectedDevice: String) -> Unit)? = null) {
            itemView.findViewById<TextView>(R.id.title).text = device

            if(onClick != null) {
                itemView.setOnClickListener {
                    onClick(device)
                }
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
        holder.showItem(deviceList[position], onClick)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

}


