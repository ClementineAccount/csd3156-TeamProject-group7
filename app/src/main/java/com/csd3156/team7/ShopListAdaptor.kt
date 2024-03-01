package com.csd3156.team7

import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.csd3156.team7.ShopActivity.Companion.playerViewModel
import com.csd3156.team7.Weather.WeatherService
import com.csd3156.team7.Weather.WeatherServiceClient
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding


class ShopListAdaptor(var shopActivity: ShopActivity, private var dataSource: List<ShopItem>, var player: Player)
    : RecyclerView.Adapter<ItemViewHolder>() {


    private var selectedPosition = 0

    companion object{
            lateinit var shopListAdaptor : ShopListAdaptor
            var selectedID : Int = 0
            var selectedFarmName : String = ""
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Inflate the item view and create a new ItemViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_item, parent, false)

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ShopItemBinding.inflate(layoutInflater,parent,false)


        return ItemViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        // Bind the data to the ViewHolder
        val item = dataSource[position]
        holder.bind(item,position, selectedPosition == position)
        {
            // Handle item selection
            newPosition ->
            if (selectedPosition != newPosition) {
                val oldPosition = selectedPosition
                selectedPosition = newPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(newPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    fun setItems(items: List<ShopItem>) {
        // Update the data source and notify the adapter
        dataSource = items
        notifyDataSetChanged()
    }

    fun getColorComponents(colorInt: Int): Triple<Int, Int, Int> {
        // Extract RGB components from color integer
        val red = (colorInt shr 16) and 0xff
        val green = (colorInt shr 8) and 0xff
        val blue = colorInt and 0xff
        return Triple(red, green, blue)
    }
}
