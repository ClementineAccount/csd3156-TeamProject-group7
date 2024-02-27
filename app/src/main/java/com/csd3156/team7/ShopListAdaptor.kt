package com.csd3156.team7

import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.csd3156.team7.ShopActivity.Companion.playerViewModel
import com.csd3156.team7.Weather.WeatherService
import com.csd3156.team7.Weather.WeatherServiceClient
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding


class ShopListAdaptor(var shopActivity: ShopActivity, private var dataSource: List<ShopItem>, var player: Player)
    : RecyclerView.Adapter<ItemViewHolder>() {

        companion object{
            lateinit var shopListAdaptor : ShopListAdaptor
            var selectedID : Int = 0
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_item, parent, false)

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ShopItemBinding.inflate(layoutInflater,parent,false)


        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataSource[position]
        holder.bind(dataSource[position])
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    fun setItems(items: List<ShopItem>) {
        dataSource = items



        shopActivity.setCurrencyText(playerViewModel.playerCurrencyObject.currency)
        shopActivity.getSharedPreferences("Player", MODE_PRIVATE).edit().putInt("PlayerCurrency", playerViewModel.playerCurrencyObject.currency).apply()
        notifyDataSetChanged()
    }





}
