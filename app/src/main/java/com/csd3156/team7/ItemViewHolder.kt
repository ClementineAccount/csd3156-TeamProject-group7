package com.csd3156.team7


import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View

import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding
import kotlinx.coroutines.*

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Toast
import androidx.compose.ui.text.toUpperCase
import com.google.ar.core.examples.kotlin.helloar.NFCActivity

class ItemViewHolder(private val binding: ShopItemBinding):RecyclerView.ViewHolder(binding.root){




    fun bind(item: ShopItem)
    {
        binding.itemName.text = item.name
        binding.itemImage.setImageResource(item.imageResourceId)
        binding.itemImage.setColorFilter(item.color)

        binding.itemQuantity.text = item.quantity.toString()

        binding.itemDescription.text = item.description

        binding.itemPrice.text = "Price: ${item.price}"
        binding.unlockButton.visibility = if (item.researched) View.GONE else View.VISIBLE
        binding.unlockButton.text = "Unlock: ${item.creditsToResearch} CREDIT"
        binding.unlockButton.setOnClickListener {
            onUnlockAttempt(item)
        }


        if (ShopActivity.playerViewModel.playerCurrencyObject.currency - item.price < 0) {
            binding.buyButton.isEnabled = false
        }
        else
        {
            binding.buyButton.isEnabled = true
        }
        binding.buyButton.setOnClickListener {

            if (ShopActivity.playerViewModel.playerCurrencyObject.currency - item.price < 0) {

                binding.buyButton.text = "NOT ENOUGH CREDITS"
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.buyButton.text = "BUY"
                }, 500)
                //Toast.makeText(binding.root.context, "NOT ENOUGH CREDITS", Toast.LENGTH_SHORT).show()
                Log.d("ItemViewHolder", "Not enough currency")
                return@setOnClickListener
            }
            else {

                ShopActivity.playerViewModel.updateItemQuantity(item.itemId, item.quantity + 1)

                // Keep case sensitivity in mind here.
                if (ShopActivity.weatherCondition.uppercase().contains("CLOUDY"))
                {
                    ShopActivity.playerViewModel.playerCurrencyObject.currency -= item.price * 2
                }
                else
                {
                    ShopActivity.playerViewModel.playerCurrencyObject.currency -= item.price
                }
                ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)

                // null bug?
                Log.d(
                    "ItemViewHolder",
                    "Currency: ${ShopActivity.playerViewModel.currentPlayerCurrency.value}"
                )
            }
        }

        if (item.quantity == 0) {
            binding.sellButton.isEnabled = false
        }
        else
        {
            binding.sellButton.isEnabled = true
        }
        binding.sellButton.setOnClickListener {

            if (item.quantity == 0) {

                Log.d("ItemViewHolder", "No items to sell")
                return@setOnClickListener
            }
            else {

                // Sell all items at once. (as intended)
                ShopActivity.playerViewModel.updateItemQuantity(item.itemId, 0 )

                ShopActivity.playerViewModel.playerCurrencyObject.currency += item.price * item.quantity
                ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)

                // null bug?
                Log.d(
                    "ItemViewHolder",
                    "Currency: ${ShopActivity.playerViewModel.currentPlayerCurrency.value}"
                )
            }

        }

        binding.changeColorButton.setOnClickListener{
            ShopListAdaptor.selectedID = item.itemId

            val intent = Intent(itemView.context, NFCActivity::class.java)
            itemView.context.startActivity(intent)
        }
    }
    fun onUnlockAttempt(item: ShopItem)
    {
        if(ShopActivity.playerViewModel.playerCurrencyObject.currency  >= item.creditsToResearch)
        {

            ShopActivity.playerViewModel.playerCurrencyObject.currency -= item.creditsToResearch

            ShopActivity.playerViewModel.updateItemResearchState(item.itemId, true)

        }
        else {

            binding.unlockButton.text = "NOT ENOUGH CREDITS"
            Handler(Looper.getMainLooper()).postDelayed({
                binding.unlockButton.text = "RESEARCH: ${item.creditsToResearch} CREDITS"
            }, 500)


        }




    }



}