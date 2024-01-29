package com.csd3156.team7

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity



class ShopListAdaptor(var shop: Shop, private var dataSource: List<ShopItem>): RecyclerView.Adapter<ItemViewHolder>() {
    //val sharedPreferences: SharedPreferences = context.getSharedPreferences("player", Context.MODE_PRIVATE)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.farm_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataSource[position]
        holder.itemName.text = item.name
        holder.itemImage.setImageResource(item.imageResourceId)
        holder.itemQuantity.text = item.quantity.toString()
        holder.itemDescription.text = item.description
        holder.itemPrice.text = "Cost: ${item.price}"




        holder.buyButton.setOnClickListener{

            if(shop.playerCurrency - item.price > 0)
            {
                shop.playerCurrency -= item.price

                item.quantity++
                holder.itemQuantity.text = item.quantity.toString()
                shop.setCurrencyText()

            }

        }

        holder.sellButton.setOnClickListener{
            if(item.quantity > 0) {
                shop.playerCurrency += item.price / 2
                item.quantity--
                holder.itemQuantity.text = item.quantity.toString()
                shop.setCurrencyText()

            }


        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    fun setItems(items: List<ShopItem>) {
        dataSource = items
        shop.setCurrencyText()
        notifyDataSetChanged()
    }





}
