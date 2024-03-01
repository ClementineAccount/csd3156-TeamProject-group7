/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.examples.kotlin.helloar

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.csd3156.team7.FarmItem
import com.csd3156.team7.MapsActivity
import com.csd3156.team7.MusicService
import com.csd3156.team7.PlayerInventoryViewModel
import com.csd3156.team7.PlayerShopViewModel
import com.csd3156.team7.ShopActivity
import com.csd3156.team7.ShopItem
import com.csd3156.team7.SoundEffectsManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Config.InstantPlacementMode
import com.google.ar.core.Earth
import com.google.ar.core.GeospatialPose
import com.google.ar.core.Session
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper
import com.google.ar.core.examples.java.common.helpers.DepthSettings
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper
import com.google.ar.core.examples.java.common.helpers.InstantPlacementSettings
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.examples.kotlin.common.helpers.ARCoreSessionLifecycleHelper
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.random.Random


/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3D model.
 */

class MyLocationListener(private val context: Context, private val locationCallback: (Location) -> Unit) :
  LocationListener {

  override fun onLocationChanged(location: Location) {
    // Handle new location updates here
    locationCallback.invoke(location)
  }

  override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    // Handle location provider status changes if needed
  }
}


class HelloArActivity : AppCompatActivity(), TapInterface {
  companion object {
    private const val TAG = "HelloArActivity"
  }

  lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
  lateinit var view: HelloArView
  lateinit var renderer: HelloArRenderer

  val instantPlacementSettings = InstantPlacementSettings()
  val depthSettings = DepthSettings()

  lateinit var playerViewModel: PlayerInventoryViewModel
  lateinit var shopViewModel: PlayerShopViewModel

  private val LOCATION_PERMISSION_REQUEST_CODE = 100
  public lateinit var earth : Earth
  lateinit var cameraGeospatialPose : GeospatialPose

  //For spawning the collectable items at a timer
  private var handler: Handler? = null
  private var addCollectableTask: Runnable? = null
  private var collectableRateSeconds : Long = 2L

  // Number of times the handler called before it ends
  // By default don't run first...
  private var collectableRunNumberMaxCount : Int = 5
  private var currentCollectableRunCount : Int = 0

  private lateinit var musicService: MusicService

  private var isBound = false
  public var currentShapeFarm : String = "Sphere"
  public var startCollecting : Boolean = false

  lateinit var currentShapeColor : Triple<Int, Int, Int>

  lateinit var shapeUI : TextView
  lateinit var currentShapeItem : ShopItem

  private fun checkPermission(permission: String, requestCode: Int) {
    if (ContextCompat.checkSelfPermission(this@HelloArActivity, permission) == PackageManager.PERMISSION_DENIED) {

      // Requesting the permission
      ActivityCompat.requestPermissions(this@HelloArActivity, arrayOf(permission), requestCode)
    } else {
      Toast.makeText(this@HelloArActivity, "Permission already granted for GPS", Toast.LENGTH_SHORT).show()
    }
  }

  public fun displayMinigameEndMessage()
  {
    //I rushing stuff at like 10.47pm on 28/2/2024 I don't have time to make this make sense
    //val message: String = "End of the minigame! Tap again to collect more!"
    //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }


  // Lat = [0], Long = [1] always remember
  private fun getGPSLocation(resultList :MutableList<Double>) {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    resultList.clear()

    // Check if the location provider is enabled
    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//      val locationListener = MyLocationListener(this) { location ->
//        // Handle location updates
//        // Do something with the location
//        resultList.add(location.latitude)
//        resultList.add(location.longitude)
//
//      }
      checkPermission(
        Manifest.permission.ACCESS_FINE_LOCATION,
        LOCATION_PERMISSION_REQUEST_CODE
      )

      checkPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        LOCATION_PERMISSION_REQUEST_CODE
      )

      val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


      if (ContextCompat.checkSelfPermission(
          this@HelloArActivity,
          Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
      ) {
        fusedLocationClient.lastLocation
          .addOnSuccessListener(this@HelloArActivity,
            OnSuccessListener<Location?> { location ->
              if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val altitude = location.altitude

                resultList.add(latitude)
                resultList.add(longitude)

                // Do something with latitude and longitude
                Toast.makeText(
                  this@HelloArActivity,
                  "Latitude: $latitude\nLongitude: $longitude\nAltitude: $altitude", Toast.LENGTH_SHORT
                ).show()

                val newFarm = FarmItem(name = "Test Farm", lat = latitude, long =  longitude, alt = altitude)
                //playerViewModel.insert(newFarm)

                // For testing
                printAllFarmItem(playerViewModel.allFarm)

              } else {
                // Location is null, handle the case
                Toast.makeText(this@HelloArActivity, "Location not available", Toast.LENGTH_SHORT)
                  .show()
              }
            })
      } else {
        // Permission not granted
        Toast.makeText(this@HelloArActivity, "Location permission not granted", Toast.LENGTH_SHORT)
          .show()
      }

      // Request location updates with minTime and minDistance
//      locationManager.requestLocationUpdates(
//        LocationManager.GPS_PROVIDER,
//        10000, // minTime in milliseconds (10 seconds)
//        10f,   // minDistance in meters (10 meters)
//        locationListener
//      )
//    } else {
//      // You may prompt the user to enable the location provider
//      // or provide an option to enable it programmatically
//    }
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d("HelloArActivity", "onCreate")
    Log.d("MusicService", "HelloArActivity->onCreate")
    Intent(this, MusicService::class.java).also { intent ->
      bindService(intent, connection, Context.BIND_AUTO_CREATE)
      //startService(intent)
      Log.d("MusicService", "HelloArActivity->bindService")
      Log.d("HelloArActivity", "bindService")

    }
    // Setup ARCore session lifecycle helper and configuration.
    arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
    // If Session creation or Session.resume() fails, display a message and log detailed
    // information.
    arCoreSessionHelper.exceptionCallback =
      { exception ->
        val message =
          when (exception) {
            is UnavailableUserDeclinedInstallationException ->
              "Please install Google Play Services for AR"
            is UnavailableApkTooOldException -> "Please update ARCore"
            is UnavailableSdkTooOldException -> "Please update this app"
            is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
            is CameraNotAvailableException -> "Camera not available. Try restarting the app."
            else -> "Failed to create AR session: $exception"
          }
        Log.e(TAG, "ARCore threw an exception", exception)
        view.snackbarHelper.showError(this, message)
      }
    // Configure session features, including: Lighting Estimation, Depth mode, Instant Placement.
    arCoreSessionHelper.beforeSessionResume = ::configureSession
    lifecycle.addObserver(arCoreSessionHelper)

    shopViewModel = ViewModelProvider(this)[PlayerShopViewModel::class.java]
    playerViewModel = ViewModelProvider(this)[PlayerInventoryViewModel::class.java]

    checkPermission(
      Manifest.permission.ACCESS_FINE_LOCATION,
      LOCATION_PERMISSION_REQUEST_CODE)

    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
      LOCATION_PERMISSION_REQUEST_CODE)

    // Set up the Hello AR renderer.
    renderer = HelloArRenderer(this,this)
    lifecycle.addObserver(renderer)

    // Set up Hello AR UI.
    view = HelloArView(this)
    lifecycle.addObserver(view)
    setContentView(view.root)

    // Sets up an example renderer using our HelloARRenderer.
    SampleRender(view.surfaceView, renderer, assets)

    depthSettings.onCreate(this)
    instantPlacementSettings.onCreate(this)

    val shopButton = findViewById<Button>(R.id.buttonShop)
    val MapButton = findViewById<Button>(R.id.buttonMap)
    val nfcButton = findViewById<Button>(R.id.NFCButton)

    shapeUI = findViewById<TextView>(R.id.shapeUI)

//    GlobalScope.launch(Dispatchers.IO)
//    {
//      currentShapeItem = shopViewModel.shopRepository.getItemByName(playerViewModel.farmRepository.currentFarmShape)
//      shapeUI.text = currentShapeItem.quantity.toString()
//    }


    val startCollectButton = findViewById<Button>(R.id.buttonCollect)

    nfcButton.setOnClickListener {
      val intent = Intent(this, NFCActivity::class.java)
      startActivity(intent);
    }

    shopButton.setOnClickListener {
      val intent = Intent(this, ShopActivity::class.java)
      startActivity(intent);
    }
    MapButton.setOnClickListener {
     val Intent = Intent(this, MapsActivity::class.java)
     startActivity(Intent);
    }

    startCollectButton.setOnClickListener {
      if (!startCollecting && renderer.isAnchorEmpty())
      {
        startCollecting = true
        startCollectButton.visibility = View.INVISIBLE

        GlobalScope.launch(Dispatchers.IO)
        {
          //Just alternate them
          if (currentShapeFarm == "Pyramid" && shopViewModel.shopRepository.getItemByName("Cube").researched)
          {
            currentShapeFarm = "Cube"
          }
          else if (currentShapeFarm == "Cube" && shopViewModel.shopRepository.getItemByName("Sphere").researched)
          {
            currentShapeFarm = "Sphere"
          }
          else if (currentShapeFarm == "Sphere")
          {
            currentShapeFarm = "Pyramid"
          }

          currentShapeItem = shopViewModel.shopRepository.getItemByName(currentShapeFarm)
          currentShapeColor = shopViewModel.getColorComponents(currentShapeItem.color)
        }

//        runBlocking {
//          val job = launch {
//
//          }
//          job.join()
//        }
      }
    }

    val clearDatabaseDebugButton = findViewById<Button>(R.id.clearFarm)
    clearDatabaseDebugButton.setOnClickListener {

      renderer.clearAnchorGPS()
      playerViewModel.deleteAllFarm()

    }

    val debugFarmPlaceButton = findViewById<Button>(R.id.debugFarm)
    debugFarmPlaceButton.setOnClickListener {
      runOnUiThread {
        var farmList: MutableList<FarmItem> = mutableListOf()
        playerViewModel.allFarm.observe(this, Observer { farmList ->
          // The observer will be notified when the LiveData changes

          // Check if the list is not null and not empty before looping
          if (farmList != null && farmList.isNotEmpty()) {
            // Loop through the FourDigit objects in the list
            for (farm in farmList) {
              println("Farm ID: ${farm.uid}")
              println("Farm Name: ${farm.farmName}")
              println("Farm latitude: ${farm.latitude}")
              println("Farm longtitude: ${farm.longtitude}")
              println("Farm Altitude: ${farm.altitude}")
              println("Farm qx: ${farm.qx}")
              println("Farm qy: ${farm.qy}")
              println("Farm qz: ${farm.qz}")
              println("Farm qw: ${farm.qw}")

              val cameraGeospatialPose = earth.cameraGeospatialPose

              //Use altitude of camera for testing for now.

              // Test only first farm
              // TODO: Test if place all the farms
              val anchor : Anchor = earth.createAnchor(farm.latitude, farm.longtitude, cameraGeospatialPose.altitude,
                farm.qx, farm.qy, farm.qz, farm.qw)

              // TODO: Handle exception if farm is empty
              renderer.addAnchorGPS(anchor)
              //farmList.add(newEntity)
            }
          }
        })
      }
    }
    handler = Handler(Looper.getMainLooper())
    addCollectableTask = object : Runnable {
      override fun run() {
        var random : Random = Random.Default
        lifecycleScope.launch {

          // Don't create it for the last loop iteration (bad user experience)
          if (startCollecting && !renderer.isAnchorEmpty() && currentCollectableRunCount < collectableRunNumberMaxCount - 1)
          {

            var maxX : Float = 1.25f
            var minX : Float = -1.25f

            var maxZ : Float = 1.0f
            var minZ : Float = -1.0f

            var offsetX : Float = random.nextFloat() * (maxX - minX) + minX
            var offsetZ : Float = random.nextFloat() * (maxZ - minZ) + minZ



            renderer.createCollectable(offsetX, 0.0f, offsetZ)
          }
        }
        if (currentCollectableRunCount < collectableRunNumberMaxCount && startCollecting && !renderer.isAnchorEmpty())
        {
          currentCollectableRunCount += 1
          handler?.postDelayed(this, collectableRateSeconds * 1000)
        }
        else
        {
          renderer.showMinigameEndText()

          //TODO: Remove the anchor after the thing stop spawning...
          renderer.removeAnchors()
          renderer.removeCollectables()
          startCollecting = false
          startCollectButton.visibility = View.VISIBLE
        }
      }
    }
    // Start the task immediately
    handler?.post(addCollectableTask!!)


  }


  //Set the current collectable run count back to 0 so the thing will spawn
  public fun setCollectableTaskRun()
  {

    lifecycleScope.launch {
      withContext(Dispatchers.Main) {
        // Perform UI-related operations here, such as showing a Toast
        //Toast.makeText(applicationContext, "COLLECT THE SHAPES!", Toast.LENGTH_SHORT).show()
      }
    }

    currentCollectableRunCount = 0
    handler?.post(addCollectableTask!!)


  }


  // Configure the session, using Lighting Estimation, and Depth mode.
  fun configureSession(session: Session) {
    session.configure(
      session.config.apply {
        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

        // Depth API is used if it is configured in Hello AR's settings.
        depthMode =
          if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            Config.DepthMode.AUTOMATIC
          } else {
            Config.DepthMode.DISABLED
          }

        // Instant Placement is used if it is configured in Hello AR's settings.
        instantPlacementMode =
          if (instantPlacementSettings.isInstantPlacementEnabled) {
            InstantPlacementMode.LOCAL_Y_UP
          } else {
            InstantPlacementMode.DISABLED
          }

        // Enable the Geospatial API to retrieve anchor positions from database using GPS
        geospatialMode = Config.GeospatialMode.ENABLED

      }
    )
  }


  public fun updateShapeCount()
  {
    GlobalScope.launch(Dispatchers.IO)
    {
      currentShapeItem.quantity += 1
      shopViewModel.shopRepository.updateQuantity(currentShapeItem.itemId, currentShapeItem.quantity)
    }
  }

  public fun printAllFarmItem(allEntity: LiveData<List<FarmItem>>) {
    runOnUiThread {
      var farmList: MutableList<FarmItem> = mutableListOf()
      allEntity.observe(this, Observer { farmList ->
        // The observer will be notified when the LiveData changes

        // Check if the list is not null and not empty before looping
        if (farmList != null && farmList.isNotEmpty()) {
          // Loop through the FourDigit objects in the list
          for (farm in farmList) {
            println("Farm ID: ${farm.uid}")
            println("Farm Name: ${farm.farmName}")
            println("Farm latitude: ${farm.latitude}")
            println("Farm longtitude: ${farm.longtitude}")
            println("Farm Altitude: ${farm.altitude}")
            //farmList.add(newEntity)
          }
        }
      })
    }
  }



  public suspend fun addFarm(newFarm : FarmItem) : Long {
    val generatedUid = playerViewModel.insert(newFarm)
    printAllFarmItem(playerViewModel.allFarm)
    return generatedUid
  }


  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    results: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, results)
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      // Use toast instead of snackbar here since the activity will exit.
      Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
        .show()
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this)
      }
      finish()
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
  }

  override fun onObjectTapped(farmId: Int) {
    runOnUiThread {
      val overlayText: TextView = findViewById(R.id.incrementValueText)
      overlayText.text = "+1"
      overlayText.visibility = View.VISIBLE

      GlobalScope.launch(Dispatchers.IO)
      {
        // Add the value to the database when collected
        // TODO: Account for the different shape (it is based off the farm)
        // For now just do Cube

        var collectItem = shopViewModel.shopRepository.getItemByName("Cube")

        //TODO: Increment it here
        //TODO: Increment by formula that takes into account the farm
        //TODO: Set it back
        var updateCount = shopViewModel.shopRepository.getItemQuantity(collectItem.itemId)
        updateCount += 1

        Log.d("FarmService", "collectItem Quantity: ${updateCount}")
        shopViewModel.shopRepository.updateQuantity(collectItem.itemId, updateCount)
        collectItem = shopViewModel.shopRepository.getItemByName("Cube")
      }

      // Handler to post a delayed task
      overlayText.postDelayed({
        overlayText.visibility = View.INVISIBLE // or View.GONE if you want to remove the space it takes up as well
      }, 500) // Delay in milliseconds (1000ms = 1s)
    }

  }


  private val connection = object : ServiceConnection {

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
      val binder = service as MusicService.MusicBinder
      musicService = binder.getService()
      isBound = true
      musicService?.playMusic(R.raw.background_music_3)
      Log.d("MusicService", "HelloArActivity->onServiceConnected")
      Log.d("HelloArActivity", "onServiceConnected")
    }

    override fun onServiceDisconnected(arg0: ComponentName?) {
      Log.d("MusicService", "HelloArActivity->onServiceDisconnected")

      Log.d("HelloArActivity", "onServiceDisconnected")
      isBound = false
      //musicService?.stopService(intent)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d("MusicService", "HelloArActivity->onDestroy")
    Log.d("HelloArActivity", "onDestroy")
    if (isBound) {
      Log.d("MusicService", "HelloArActivity->unbindService")
      Log.d("HelloArActivity", "unbindService")
      //musicService?.stopMusic()
      unbindService(connection)

      isBound = false
    }
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
