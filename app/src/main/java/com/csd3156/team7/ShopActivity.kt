package com.csd3156.team7

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R


class ShopActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdaptor: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var inventoryList: MutableList<ShopItem> = mutableListOf()

    var player: Player = Player("Test", 1000)

    lateinit var playerViewModel: PlayerInventoryViewModel

    fun setCurrencyText() {
        val currency: TextView = findViewById(R.id.shop_currency)
        currency.text = "${player.currentCurrency} Shapes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shop)
        viewManager = LinearLayoutManager(this)
        playerViewModel = ViewModelProvider(this)[PlayerInventoryViewModel::class.java]

        val currencyText: TextView = findViewById(R.id.shop_currency)

        playerViewModel.currentPlayerCurrency.observe(this) {

            currencyText.text = "${player.currentCurrency} Shapes"
//            val test : LiveData<Int> = playerViewModel.repository.getPlayerCurrency().asLiveData()
//            Log.d("DEBUG1", test.value.toString())
        }

//        val sharedPref = getSharedPreferences("Player", MODE_PRIVATE)
        setCurrencyText()

        val imageResId: Int = R.drawable.square_placeholder
        inventoryList.add(ShopItem("Cube", imageResId, 5, "Produces 10 per 1 second", 10))
        inventoryList.add(ShopItem("Sphere", imageResId, 1, "Produces 5 per 1 second", 5))
        viewAdaptor = ShopListAdaptor(this, inventoryList, player)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventoryList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdaptor
        }
        //load()


        if (savedInstanceState != null) {
            val savedList = savedInstanceState.getParcelableArrayList<ShopItem>("SAVED_ITEMS")
            player.currentCurrency = savedInstanceState.getInt("PC")

            inventoryList = savedList?.toMutableList() ?: mutableListOf()
            (viewAdaptor as ShopListAdaptor).setItems(inventoryList)
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the list of items, including their checked states
        outState.putInt("PC", player.currentCurrency)

        outState.putParcelableArrayList("SAVED_ITEMS", ArrayList(inventoryList))
    }


}