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
import android.os.Debug
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.toUpperCase
import com.csd3156.team7.ShopListAdaptor.Companion.selectedFarmName
import com.google.ar.core.examples.kotlin.helloar.NFCActivity

class ItemViewHolder(private val binding: ShopItemBinding):RecyclerView.ViewHolder(binding.root){




    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(item: ShopItem, position: Int, isSelected: Boolean, onItemSelected: (Int) -> Unit)
    {
        binding.itemName.text = item.name
        binding.itemImage.setImageResource(item.imageResourceId)
        binding.itemImage.setColorFilter(item.color)

        binding.itemQuantity.text = item.quantity.toString()

        binding.itemDescription.text = item.description

        binding.itemPrice.text = "Price: ${item.price}"
        binding.itemContainerBackground.isSelected = isSelected
        if(binding.itemContainerBackground.isSelected)
        {
            selectedFarmName = item.name


        }
        else
        {
            selectedFarmName = ""
        }

        binding.selectButton.setOnClickListener{

            onItemSelected(position)
        }

        Log.d("ItemViewHolder", "Weather: ${ShopActivity.weatherCondition}")


        // If the weather condition is cloudy, double the
        // buying (& selling) price of the item.
        // Directly accessing the weather condition returns "".
        CoroutineScope(Dispatchers.IO).launch {

            // Keep case sensitivity in mind here.
            if (ShopActivity.playerViewModel.getWeather("q").current.condition
                .text.uppercase().contains("CLOUDY"))
            {
                binding.itemPrice.text = "Price: ${item.price * 2}"
            }
            else
            {
                binding.itemPrice.text = "Price: ${item.price}"
            }

        }
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

                if (ShopActivity.weatherCondition.uppercase().contains("CLOUDY"))
                {
                    ShopActivity.playerViewModel.playerCurrencyObject.currency -= item.price * 2
                }
                else
                {
                    ShopActivity.playerViewModel.playerCurrencyObject.currency -= item.price
                }


                ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)

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

                // Double the selling price if it's night time.
                val nightTimeModifier = when (ShopActivity.nightTime) { true -> 2 false -> 1 }
                ShopActivity.playerViewModel.playerCurrencyObject.currency +=
                    item.price * item.quantity * nightTimeModifier

                ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)


                Log.d(
                    "ItemViewHolder","Currency: ${ShopActivity.playerViewModel.currentPlayerCurrency.value}"
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
            ShopActivity.playerViewModel.setPlayerCurrency(ShopActivity.playerViewModel.playerCurrencyObject.currency)
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