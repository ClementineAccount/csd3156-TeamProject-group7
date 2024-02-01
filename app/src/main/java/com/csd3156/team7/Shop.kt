package com.csd3156.team7

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R


class Shop : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdaptor: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var inventoryList: MutableList<ShopItem> = mutableListOf()

    var playerCurrency: Int = 1000

    lateinit var vModel : PlayerInventoryViewModel

    fun setCurrencyText() {
        val currency: TextView = findViewById(R.id.shop_currency)

        // Update from previous instance of app
//        val pc : Int = vModel.repository.getPlayerCurrency().toString().toInt()
        /// error
//        playerCurrency = pc
        currency.text = "$playerCurrency Shapes"
    }

    //private val viewModel: PlayerInventoryViewModel by viewModels { PlayerInventoryViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shop)
        viewManager = LinearLayoutManager(this)

//        vModel = ViewModelProvider(this)[PlayerInventoryViewModel::class.java]
        vModel = ViewModelProvider.AndroidViewModelFactory(application).create(PlayerInventoryViewModel::class.java)
//        vModel.currentPlayerCurrency.observe(this) {
//            Log.d("D",vModel.currentPlayerCurrency.value.toString())
//        }
        val sharedPref = getSharedPreferences("Player", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()

//        viewModel.listShopItem.observe(this) {
             // TODO: items-side data changes?
//        }

        //val currency:TextView = findViewById(R.id.shop_currency)
        setCurrencyText()


        val imageResId: Int = R.drawable.square_placeholder
        inventoryList.add(ShopItem("Cube", imageResId, 5, "Produces 10 per 1 second", 10))
        inventoryList.add(ShopItem("Sphere", imageResId, 1, "Produces 5 per 1 second", 5))
        viewAdaptor = ShopListAdaptor(this, inventoryList)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventoryList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdaptor
        }
        //load()


        if (savedInstanceState != null) {
            val savedList = savedInstanceState.getParcelableArrayList<ShopItem>("SAVED_ITEMS")
            playerCurrency = savedInstanceState.getInt("Currency")
            inventoryList = savedList?.toMutableList() ?: mutableListOf()
            (viewAdaptor as ShopListAdaptor).setItems(inventoryList)

            //setCurrencyText()


        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the list of items, including their checked states
        outState.putInt("Currency", playerCurrency)
        outState.putParcelableArrayList("SAVED_ITEMS", ArrayList(inventoryList))
    }


}