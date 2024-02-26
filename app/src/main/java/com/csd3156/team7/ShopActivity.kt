package com.csd3156.team7

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
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
    val STARTING_CURRENCY = 2000
    var player: Player = Player("Test", STARTING_CURRENCY)

    companion object {
        lateinit var playerViewModel: PlayerShopViewModel
    }

    fun setCurrencyText(currency : Int) {
        val currencyTextView: TextView = findViewById(R.id.shop_currency)
        currencyTextView.text = "CREDIT: ${currency}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shop)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        viewManager = LinearLayoutManager(this)
        playerViewModel = ViewModelProvider(this)[PlayerShopViewModel::class.java]

        val pyramidQuantity = getSharedPreferences("Player", MODE_PRIVATE).getInt("Pyramid", 0)
        val cubeQuantity = getSharedPreferences("Player", MODE_PRIVATE).getInt("Cube", 0)
        val sphereQuantity = getSharedPreferences("Player", MODE_PRIVATE).getInt("Sphere", 0)
        val squareImageResId: Int = R.drawable.square_placeholder
        val circleImageResId: Int = R.drawable.circle
        val triangleImageResId: Int = R.drawable.triangle

        val redColorHex : String = "#DC143C"
        val greenColorHex : String = "#228B22"
        val blueColorHex : String = "#00BFFF"

        val redColor = Color.parseColor(redColorHex)
        val greenColor =  Color.parseColor(greenColorHex)
        val blueColor = Color.parseColor(blueColorHex)



        val firstLaunch : Boolean = getSharedPreferences("Player", MODE_PRIVATE).getBoolean("FirstLaunch", true)
        if (firstLaunch) {
            lifecycleScope.launch {
                playerViewModel.insertItem(ShopItem("Pyramid", triangleImageResId, pyramidQuantity,
                    "Produces 5 per 1 second", 5, true,1000,redColor))
                playerViewModel.insertItem(ShopItem("Cube", squareImageResId, cubeQuantity,
                    "Produces 10 per 1 second", 10, false,500,greenColor))
                playerViewModel.insertItem(ShopItem("Sphere", circleImageResId, sphereQuantity,
                    "Produces 15 per 1 second", 15, false,2000,blueColor))

            }
            getSharedPreferences("Player", MODE_PRIVATE).edit().putBoolean("FirstLaunch", false).apply()
        }

        playerViewModel.currentPlayerCurrency.observe(this)
        {
            if (it == 0) {
                player.currentCurrency = STARTING_CURRENCY
                setCurrencyText(player.currentCurrency)
            }
            else {
                player.currentCurrency = it
                playerViewModel.playerCurrencyObject.currency = it
                setCurrencyText(player.currentCurrency)


            }
        }


        // copy viewModel data to this inventory list
        playerViewModel.allItems.observe(this) {
            // the inventory list is updated

            inventoryList = it.toMutableList()

            (viewAdaptor as ShopListAdaptor).setItems(inventoryList)
        }

//        inventoryList.add(ShopItem("Cube", imageResId, cubeQuantity,"Produces 10 per 1 second", 10))
//        inventoryList.add(ShopItem("Sphere", imageResId, sphereQuantity,"Produces 5 per 1 second", 5))

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

        Log.d("ShopActivity", "1213Test: $cubeQuantity")

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the list of items, including their checked states
        outState.putInt("PC", player.currentCurrency)

        outState.putParcelableArrayList("SAVED_ITEMS", ArrayList(inventoryList))
    }



    override fun onBackPressed() {
        // Use NavUtils to navigate up to the parent activity as specified in the AndroidManifest
        NavUtils.navigateUpFromSameTask(this)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this activity,
                // the Up button is shown. Use NavUtils to allow users to navigate up one level in the application structure.
                // When pressing Up from this activity, the implementation of navigating to the parent activity
                // should ensure that the back button returns the user to the home screen.
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}