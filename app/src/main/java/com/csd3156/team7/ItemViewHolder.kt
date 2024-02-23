package com.csd3156.team7

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding

class ItemViewHolder(private val binding: ShopItemBinding):RecyclerView.ViewHolder(binding.root){

    fun bind(item: ShopItem)
    {
        binding.itemName.text = item.name
        binding.itemImage.setImageResource(item.imageResourceId)
        binding.itemQuantity.text = item.quantity.toString()
        binding.itemDescription.text = item.description
        binding.itemPrice.text = "Cost: ${item.price}"

        binding.buyButton.setOnClickListener {

            if (ShopActivity.playerViewModel.playerCurrencyObject.currency - item.price < 0) {
                Log.d("ItemViewHolder", "Not enough currency")
                return@setOnClickListener
            }

            ShopActivity.playerViewModel.updateItemQuantity(item.name, item.quantity + 1)

            ShopActivity.playerViewModel.playerCurrencyObject.currency -= item.price
            ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)

            // null bug?
            Log.d("ItemViewHolder", "Currency: ${ShopActivity.playerViewModel.currentPlayerCurrency.value}")
        }


        binding.sellButton.setOnClickListener {

            if (item.quantity == 0) {
                Log.d("ItemViewHolder", "No items to sell")
                return@setOnClickListener
            }

            ShopActivity.playerViewModel.updateItemQuantity(item.name, item.quantity - 1)

            ShopActivity.playerViewModel.playerCurrencyObject.currency += item.price / 2
            ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)

            // null bug?
            Log.d("ItemViewHolder", "Currency: ${ShopActivity.playerViewModel.currentPlayerCurrency.value}")

//            ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.currentPlayerCurrency.value?.plus(item.price)!!)
        }
    }
}