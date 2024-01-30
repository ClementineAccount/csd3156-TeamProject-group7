package com.csd3156.team7

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R

class ItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    val itemName: TextView = itemView.findViewById(R.id.item_name)
    val itemImage: ImageView = itemView.findViewById(R.id.item_image)
    val itemQuantity: TextView = itemView.findViewById(R.id.item_quantity)
    val itemDescription: TextView = itemView.findViewById(R.id.item_description)
    val itemPrice : TextView = itemView.findViewById(R.id.item_price)


    val buyButton: Button = itemView.findViewById(R.id.buy_button)
    val sellButton: Button = itemView.findViewById(R.id.sell_button)

//    val viewModel = ViewModelProvider(this).get(PlayerInventoryViewModel::class.java)
//    viewModel.listShopItem.observe(this) {    }

    fun bind(item: ShopItem) {
        itemName.text = item.name
        itemImage.setImageResource(item.imageResourceId)
        itemQuantity.text = item.quantity.toString()
        itemDescription.text = item.description
        itemPrice.text = item.price.toString()

        buyButton.setOnClickListener {
            // Handle buy action
        }

        sellButton.setOnClickListener {
            // Handle sell action
        }
    }
}