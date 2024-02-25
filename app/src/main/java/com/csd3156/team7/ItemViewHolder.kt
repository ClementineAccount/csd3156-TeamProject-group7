package com.csd3156.team7

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding
import kotlinx.coroutines.*
class ItemViewHolder(private val binding: ShopItemBinding):RecyclerView.ViewHolder(binding.root){

    fun bind(item: ShopItem)
    {
        binding.itemName.text = item.name
        binding.itemImage.setImageResource(item.imageResourceId)
        binding.itemQuantity.text = item.quantity.toString()
        binding.itemDescription.text = item.description
        binding.itemPrice.text = "Cost: ${item.price}"
        binding.unlockButton.visibility = if (item.researched) View.GONE else View.VISIBLE
        binding.unlockButton.text = "RESEARCH: ${item.price * 100} CREDITS"

        binding
        binding.unlockButton.setOnClickListener {
            onUnlockAttempt(item)
        }

        binding.buyButton.setOnClickListener {

            if (ShopActivity.playerViewModel.playerCurrencyObject.currency - item.price < 0) {
                Log.d("ItemViewHolder", "Not enough currency")
                return@setOnClickListener
            }

            ShopActivity.playerViewModel.updateItemQuantity(item.itemId, item.quantity + 1)

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

            ShopActivity.playerViewModel.updateItemQuantity(item.itemId, item.quantity - 1)

            ShopActivity.playerViewModel.playerCurrencyObject.currency += item.price
            ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)

            // null bug?
            Log.d("ItemViewHolder", "Currency: ${ShopActivity.playerViewModel.currentPlayerCurrency.value}")

//            ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.currentPlayerCurrency.value?.plus(item.price)!!)
        }
    }
    fun onUnlockAttempt(item: ShopItem)
    {
        if(ShopActivity.playerViewModel.playerCurrencyObject.currency  >= item.price * 100)
        {

            ShopActivity.playerViewModel.playerCurrencyObject.currency -= item.price * 100
            ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)

            ShopActivity.playerViewModel.updateItemResearchState(item.itemId, true)

        }
        else {

            binding.unlockButton.text = "NOT ENOUGH CREDITS"
            Handler(Looper.getMainLooper()).postDelayed({
                binding.unlockButton.text = "RESEARCH: ${item.price * 100} CREDITS"
            }, 500)
        }


    }
}