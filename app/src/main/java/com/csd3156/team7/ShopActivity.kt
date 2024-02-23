package com.csd3156.team7

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.examples.kotlin.helloar.R
import kotlinx.coroutines.launch


class ShopActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdaptor: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var inventoryList: MutableList<ShopItem> = mutableListOf()

    var player: Player = Player("Test", 1000)

    companion object {
        lateinit var playerViewModel: PlayerShopViewModel
    }

    fun setCurrencyText(currency : Int) {
        val currencyTextView: TextView = findViewById(R.id.shop_currency)
        currencyTextView.text = "${currency} Shapes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shop)
        viewManager = LinearLayoutManager(this)
        playerViewModel = ViewModelProvider(this)[PlayerShopViewModel::class.java]

        val cubeQuantity = getSharedPreferences("Player", MODE_PRIVATE).getInt("Cube", 0)
        val sphereQuantity = getSharedPreferences("Player", MODE_PRIVATE).getInt("Sphere", 0)
        val imageResId: Int = R.drawable.square_placeholder

        val firstLaunch : Boolean = getSharedPreferences("Player", MODE_PRIVATE).getBoolean("FirstLaunch", true)
        if (firstLaunch) {
            lifecycleScope.launch {
                playerViewModel.insertItem(ShopItem("Cube", imageResId, cubeQuantity,
                    "Produces 10 per 1 second", 10))
                playerViewModel.insertItem(ShopItem("Sphere", imageResId, sphereQuantity,
                    "Produces 5 per 1 second", 5))
            }
            getSharedPreferences("Player", MODE_PRIVATE).edit().putBoolean("FirstLaunch", false).apply()
        }

        playerViewModel.currentPlayerCurrency.observe(this)
        {
            player.currentCurrency = it
            playerViewModel.playerCurrencyObject.currency = it
            setCurrencyText(it)
        }


        // copy viewModel data to this inventory list
        playerViewModel.allItems.observe(this) {
            // the inventory list is updated
            inventoryList = it.toMutableList()
            (viewAdaptor as ShopListAdaptor).setItems(inventoryList)
        }

//        inventoryList.add(ShopItem("Cube", imageResId, cubeQuantity,
//            "Produces 10 per 1 second", 10))
//
//        inventoryList.add(ShopItem("Sphere", imageResId, sphereQuantity,
//            "Produces 5 per 1 second", 5))

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