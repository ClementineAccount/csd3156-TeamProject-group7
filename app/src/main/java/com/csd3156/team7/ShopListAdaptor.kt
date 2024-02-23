package com.csd3156.team7

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding


class ShopListAdaptor(var shopActivity: ShopActivity, private var dataSource: List<ShopItem>, var player: Player)
    : RecyclerView.Adapter<ItemViewHolder>() {


    //val sharedPreferences: SharedPreferences = context.getSharedPreferences("player", Context.MODE_PRIVATE)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_item, parent, false)

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ShopItemBinding.inflate(layoutInflater,parent,false)


        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataSource[position]
//        holder.itemName.text = item.name
//        holder.itemImage.setImageResource(item.imageResourceId)
//        holder.itemQuantity.text = item.quantity.toString()
//        holder.itemDescription.text = item.description
//        holder.itemPrice.text = "Cost: ${item.price}"
        holder.bind(dataSource[position])




//        holder.buyButton.setOnClickListener{
//
//            if(shopActivity.player.currentCurrency - item.price > 0)
//            {
//                //shop.playerCurrency -= item.price
//                player.currentCurrency -= item.price
//
//                item.quantity++
//                holder.itemQuantity.text = item.quantity.toString()
//                shopActivity.setCurrencyText(player.currentCurrency)
//
//                shopActivity.playerViewModel.viewModelScope.launch {
//                    shopActivity.playerViewModel.repository.setPlayerCurrency(player.currentCurrency)
//
//                    val sharedPref = shopActivity.getSharedPreferences("Player", Context.MODE_PRIVATE)
//                    with(sharedPref.edit()) {
//                        putInt("PlayerCurrency", player.currentCurrency) // save the player's currency to the datastore
//                        apply() // apply is asynchronous, commit is synchronous
//                    }
//
//                    // save item quantity to the datastore
//                    val itemQuantity = sharedPref.getInt(item.name, 0)
//                    with(sharedPref.edit()) {
//                        putInt(item.name, itemQuantity + 1)
//                        apply()
//                    }
//
//                }
//
//
//            }
//
//        }
//
//        holder.sellButton.setOnClickListener{
//            if(item.quantity > 0) {
//                //shop.playerCurrency += item.price / 2
//                player.currentCurrency += item.price / 2
//
//                item.quantity--
//                holder.itemQuantity.text = item.quantity.toString()
//                shopActivity.setCurrencyText(player.currentCurrency)
//
//                shopActivity.playerViewModel.viewModelScope.launch {
//                    shopActivity.playerViewModel.repository.setPlayerCurrency(player.currentCurrency)
//
//                    val sharedPref = shopActivity.getSharedPreferences("Player", Context.MODE_PRIVATE)
//                    with(sharedPref.edit()) {
//                        putInt("PlayerCurrency", player.currentCurrency) // save the player's currency to the datastore
//                        apply() // apply is asynchronous, commit is synchronous
//                    }
//
//                    // save item quantity to the datastore
//                    val itemQuantity = sharedPref.getInt(item.name, 0)
//                    with(sharedPref.edit()) {
//                        putInt(item.name, itemQuantity - 1)
//                        apply()
//                    }
//
//                }
//
//            }
//        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    fun setItems(items: List<ShopItem>) {
        dataSource = items
        shopActivity.setCurrencyText(player.currentCurrency)
        notifyDataSetChanged()
    }





}