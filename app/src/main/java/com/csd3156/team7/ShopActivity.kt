package com.csd3156.team7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csd3156.team7.Weather.Weather
import com.csd3156.team7.Weather.WeatherServiceClient
import com.google.ar.core.examples.kotlin.helloar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ShopActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdaptor: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var inventoryList: MutableList<ShopItem> = mutableListOf()
    private val startingCurrency = 2000
    private var player: Player = Player("Test", startingCurrency)
    private var musicService: MusicService? = null
    private var isBound = false


    companion object {
        lateinit var playerViewModel: PlayerShopViewModel
        var weatherCondition: String = ""
    }

    fun setCurrencyText(currency : Int) {
        val currencyTextView: TextView = findViewById(R.id.shop_currency)
        currencyTextView.text = "CREDIT: $currency"
    }

    fun AddDefaultItems() {
            lifecycleScope.launch {
            playerViewModel.insertItem(ShopItem("Pyramid", R.drawable.triangle, 0,
                "Produces 5 per 1 second", 5, true,1000, Color.RED))
            playerViewModel.insertItem(ShopItem("Cube", R.drawable.square_placeholder, 0,
                "Produces 10 per 1 second", 10, false,500, Color.GREEN))
            playerViewModel.insertItem(ShopItem("Sphere", R.drawable.circle, 0,
                "Produces 15 per 1 second", 15, false,2000, Color.BLUE))
        } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shop)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        viewManager = LinearLayoutManager(this)
        playerViewModel = ViewModelProvider(this)[PlayerShopViewModel::class.java]

        lifecycleScope.launch {

                // Assuming you have a valid API key and the necessary permissions set up
                val response = WeatherServiceClient.create().getWeather("Singapore", 1.3521, 103.8198)
                if (response.isSuccessful) {
                    response.body()?.current?.condition?.text?.let { conditionText ->
                        val weather = Weather(_description = conditionText, "1")
                        // Use your weather data here
                    }
                }
        }

        val weatherTextView: TextView = findViewById(R.id.weatherTextView)
        lifecycleScope.launch {
            weatherCondition = playerViewModel.getWeather("q").current.condition.text

            // Log.d("ShopActivity", "Weather: ${weatherCondition}")
            weatherTextView.text = weatherCondition

            if (weatherCondition.uppercase().contains("CLOUDY")) {
                weatherTextView.append(" (x2 Prices)")
            }
        }


        val squareImageResId: Int = R.drawable.square_placeholder
        val circleImageResId: Int = R.drawable.circle
        val triangleImageResId: Int = R.drawable.triangle

        val redColorHex = "#DC143C"
        val greenColorHex = "#228B22"
        val blueColorHex = "#00BFFF"

        val redColor = Color.parseColor(redColorHex)
        val greenColor =  Color.parseColor(greenColorHex)
        val blueColor = Color.parseColor(blueColorHex)

        val firstLaunch : Boolean = getSharedPreferences("Player", MODE_PRIVATE).getBoolean("FirstLaunch", true)
        if (firstLaunch) {
            AddDefaultItems()
            getSharedPreferences("Player", MODE_PRIVATE).edit().putBoolean("FirstLaunch", false).apply()
        }

        playerViewModel.currentPlayerCurrency.observe(this)
        {
            Log.d("ShopActivity", "Currency: $it")
            if (it == 0) { player.currentCurrency = startingCurrency; }
            else {
                player.currentCurrency = it
                playerViewModel.playerCurrencyObject.currency = it
            }

            setCurrencyText(player.currentCurrency)
        }


        // copy viewModel data to this inventory list
        playerViewModel.allItems.observe(this)
        {
            
            inventoryList = it.toMutableList() // the inventory list is updated
            (viewAdaptor as ShopListAdaptor).setItems(inventoryList)

            var quantity : Int
            CoroutineScope(Dispatchers.IO).launch {
                for (item in inventoryList) {
                    quantity = playerViewModel.getItemQuantity(item.itemId)
                    Log.d("ShopActivity", "Quantity: $quantity")
                }
            }
        }


        viewAdaptor = ShopListAdaptor(this, inventoryList,player = player)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventoryList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdaptor
        }


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

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.LocalBinder
            musicService = binder.getService()
            isBound = true
            musicService?.playMusic()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MusicService::class.java).also { intent ->
            startService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)

        }

    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }


}