package com.csd3156.team7

import android.content.Context.MODE_PRIVATE
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.csd3156.team7.ShopActivity.Companion.playerViewModel
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding


class ShopListAdaptor(var shopActivity: ShopActivity, private var dataSource: List<ShopItem>, var player: Player)
    : RecyclerView.Adapter<ItemViewHolder>() {

        companion object{

            lateinit var shopListAdaptor : ShopListAdaptor
            var selectedID : Int = 0
        }





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
//                    // new: update the item quantity in the database
//                    shopActivity.playerViewModel.updateItemQuantity(item.name, item.quantity)
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
//                    // new: update the item quantity in the database
//                    shopActivity.playerViewModel.updateItemQuantity(item.name, item.quantity)
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
        shopActivity.setCurrencyText(playerViewModel.playerCurrencyObject.currency)
        shopActivity.getSharedPreferences("Player", MODE_PRIVATE).edit().putInt("PlayerCurrency", playerViewModel.playerCurrencyObject.currency).apply()
        notifyDataSetChanged()
    }





}
