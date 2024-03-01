package com.csd3156.team7

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.RequiresApi
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class ShopActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdaptor: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var inventoryList: MutableList<ShopItem> = mutableListOf()
    private val startingCurrency = 2000
    private var player: Player = Player("Test", startingCurrency)
    private lateinit var musicService: MusicService
    private var isBound = false


    // Kotlin Time
    @RequiresApi(Build.VERSION_CODES.O)
    var time = LocalTime.now()

    companion object {

        fun addDefaultItems()
        {
//            playerViewModel = ViewModelProvider(this)[PlayerShopViewModel::class.java]
            //lifecycleScope.launch
            playerViewModel.insertItem(ShopItem("Pyramid", R.drawable.triangle, 0,
                    "Produces 5 per 1 second", 5, true,1000, Color.RED))
            playerViewModel.insertItem(ShopItem("Cube", R.drawable.square_placeholder, 0,
                    "Produces 10 per 1 second", 10, false,500, Color.GREEN))
            playerViewModel.insertItem(ShopItem("Sphere", R.drawable.circle, 0,
                    "Produces 15 per 1 second", 15, false,2000, Color.BLUE))
        }


        lateinit var playerViewModel: PlayerShopViewModel
        var weatherCondition: String = ""

        @RequiresApi(Build.VERSION_CODES.O)
        var nightTime: Boolean = LocalTime.now().hour < 6 || LocalTime.now().hour > 18 &&
            LocalTime.now().second == 0
    }

    fun setCurrencyText(currency : Int) {
        val currencyTextView: TextView = findViewById(R.id.shop_currency)
        currencyTextView.text = "CREDIT: $currency"
    }

    fun addDefaultItems() {

//        playerViewModel = ViewModelProvider(this)[PlayerShopViewModel::class.java]

            lifecycleScope.launch {
            playerViewModel.insertItem(ShopItem("Pyramid", R.drawable.triangle, 0,
                "Produces 5 per 1 second", 5, true,1000, Color.RED))
            playerViewModel.insertItem(ShopItem("Cube", R.drawable.square_placeholder, 0,
                "Produces 10 per 1 second", 10, false,500, Color.GREEN))
            playerViewModel.insertItem(ShopItem("Sphere", R.drawable.circle, 0,
                "Produces 15 per 1 second", 15, false,2000, Color.BLUE))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ShopActivity", "onCreate")
        Log.d("MusicService", "ShopActivity->onCreate")
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            //startService(intent)
            Log.d("ShopActivity", "bindService")
            Log.d("MusicService", "ShopActivity->bindService")

        }
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
            addDefaultItems()
            getSharedPreferences("Player", MODE_PRIVATE).edit().putBoolean("FirstLaunch", false).apply()
            getSharedPreferences("Player", MODE_PRIVATE).edit().putInt("Currency", startingCurrency).apply()
        }

        // scope to make sure the view model is not null
        lifecycleScope.launch {
            val currency = getSharedPreferences("Player", MODE_PRIVATE).getInt("Currency", startingCurrency)
            Log.d("ShopActivity", "Start Currency: $currency")
            player.currentCurrency = currency
            playerViewModel.playerCurrencyObject.currency = currency
            setCurrencyText(player.currentCurrency)
        }

//        playerViewModel.currentPlayerCurrency.observe(this) {
//            Log.d("ShopActivity", "Currency: $it")
//            if (it == 0) { player.currentCurrency = startingCurrency; }
//            else {
//                player.currentCurrency = it
//                playerViewModel.playerCurrencyObject.currency = it
//            }
//            setCurrencyText(player.currentCurrency)
//        }

        // copy viewModel data to this inventory list
        playerViewModel.allItems.observe(this)
        {
            
            inventoryList = it.toMutableList() // the inventory list is updated
            (viewAdaptor as ShopListAdaptor).setItems(inventoryList)

            var quantity : Int
            CoroutineScope(Dispatchers.IO).launch {
                for (item in inventoryList) {
                    quantity = playerViewModel.getItemQuantity(item.itemId)
//                    Log.d("ShopActivity", "Quantity: $quantity")
                }
            }
        }


        viewAdaptor = ShopListAdaptor(this, inventoryList,player = player)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInventoryList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdaptor
        }

        // log the current time every 60 seconds
        // convert GMT LocalTime to actual local time (Singapore, GMT+8)
        val runnable = object : Runnable {
            override fun run() {
                val timeNow = LocalTime.now().plusHours(8)
                // display the current time only if seconds is 0
                if (timeNow.second == 0) {
                    // parse the time milliseconds away & into a string with the format "HH:mm:ss"
                    val timeNowString = timeNow.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    Log.d("ShopActivity", "Current Time: $timeNowString")

                    // update the weather, night/day time every 60 seconds
                    Handler().postDelayed(this, 60000)

                    lifecycleScope.launch {
                        weatherCondition = playerViewModel.getWeather("q").current.condition.text
                        weatherTextView.text = weatherCondition

                        if (weatherCondition.uppercase().contains("CLOUDY")) {
                            if (!weatherTextView.text.contains("x2 Prices")) {
                                weatherTextView.append("\n(x2 Prices)")
                            }
                        }
                        else {
                            if (weatherTextView.text.contains("x2 Prices")) {
                                weatherTextView.text = weatherTextView.text
                                    .substring(0,weatherTextView.text.length - 11)
                            }
                        }

                        nightTime = timeNow.hour < 6 || timeNow.hour > 18
                        val nightTimeTextView: TextView = findViewById(R.id.timeTextView)
                        if (nightTime) {
                            nightTimeTextView.text = "Night (x2 Selling)"
                        }
                        else {
                            nightTimeTextView.text = "Day"
                        }
                    }
                }
                else {
                    val delay = 60 - timeNow.second
                    Handler().postDelayed(this, delay * 1000L)
                }
            }
        }

        Handler().postDelayed(runnable, 10000)

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
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            musicService?.playMusic(R.raw.background_music_1)
            Log.d("MusicService", "ShopActivity->onServiceConnected")
            Log.d("ShopActivity", "onServiceConnected")
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            Log.d("MusicService", "ShopActivity->onServiceDisconnected")

            Log.d("ShopActivity", "onServiceDisconnected")
            isBound = false
            //musicService?.stopService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicService", "ShopActivity->onDestroy")
        Log.d("ShopActivity", "onDestroy")
        if (isBound) {
            Log.d("MusicService", "ShopActivity->unbindService")
            Log.d("ShopActivity", "unbindService")
            //musicService?.stopMusic()
            unbindService(connection)

            isBound = false
        }

        // set the currency to the shared preferences
        getSharedPreferences("Player", MODE_PRIVATE).edit().putInt("Currency", playerViewModel.playerCurrencyObject.currency).apply()
        Log.d("ShopActivity", "Currency: ${playerViewModel.playerCurrencyObject.currency}")
    }

//    override fun onStop() {
//        super.onStop()
//        Log.d("MusicService", "ShopActivity->onStop")
//        Log.d("ShopActivity", "onStop")
//        if (isBound) {
//            Log.d("MusicService", "ShopActivity->unbindService")
//            Log.d("ShopActivity", "unbindService")
//            //musicService?.stopMusic()
//            unbindService(connection)
//
//            isBound = false
//        }
//
//        // set the currency to the shared preferences
//
//    }

    override fun onPause() {
        super.onPause()
        if (isBound) {
            musicService.pauseMusic() // Assuming you have a method like this in your service
        }
    }

    override fun onResume() {
        super.onResume()
        if (isBound) {
            musicService.playMusic(R.raw.background_music_1) // Assuming you have a method like this in your service
        }
    }


}