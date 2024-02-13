package com.csd3156.team7

import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.databinding.ShopItemBinding

class ItemViewHolder(private val binding: ShopItemBinding):RecyclerView.ViewHolder(binding.root){


    fun bind(item: ShopItem) {
        binding.itemName.text = item.name
        binding.itemImage.setImageResource(item.imageResourceId)
        binding.itemQuantity.text = item.quantity.toString()
        binding.itemDescription.text = item.description
        binding.itemPrice.text = "Cost: ${item.price}"


        binding.buyButton.setOnClickListener {
            ShopActivity.playerViewModel.updateItemQuantity(item.name, item.quantity)

        }

        binding.sellButton.setOnClickListener {
            ShopActivity.playerViewModel.updateItemQuantity(item.name, item.quantity)
            // Handle sell action
        }
    }
}