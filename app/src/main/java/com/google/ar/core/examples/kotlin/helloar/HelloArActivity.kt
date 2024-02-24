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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.csd3156.team7.FarmItem
import com.csd3156.team7.PlayerInventoryViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Config.InstantPlacementMode
import com.google.ar.core.Earth
import com.google.ar.core.GeospatialPose
import com.google.ar.core.HitResult
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


class HelloArActivity : AppCompatActivity() {
  companion object {
    private const val TAG = "HelloArActivity"
  }

  lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
  lateinit var view: HelloArView
  lateinit var renderer: HelloArRenderer

  val instantPlacementSettings = InstantPlacementSettings()
  val depthSettings = DepthSettings()

  lateinit var playerViewModel: PlayerInventoryViewModel

  private val LOCATION_PERMISSION_REQUEST_CODE = 100

  public lateinit var earth : Earth

  lateinit var cameraGeospatialPose : GeospatialPose



  private fun checkPermission(permission: String, requestCode: Int) {
    if (ContextCompat.checkSelfPermission(this@HelloArActivity, permission) == PackageManager.PERMISSION_DENIED) {

      // Requesting the permission
      ActivityCompat.requestPermissions(this@HelloArActivity, arrayOf(permission), requestCode)
    } else {
      Toast.makeText(this@HelloArActivity, "Permission already granted for GPS", Toast.LENGTH_SHORT).show()
    }
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
                playerViewModel.insert(newFarm)

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

    playerViewModel = ViewModelProvider(this)[PlayerInventoryViewModel::class.java]

    checkPermission(
      Manifest.permission.ACCESS_FINE_LOCATION,
      LOCATION_PERMISSION_REQUEST_CODE)

    checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
      LOCATION_PERMISSION_REQUEST_CODE)

    // Set up the Hello AR renderer.
    renderer = HelloArRenderer(this)
    lifecycle.addObserver(renderer)

    // Set up Hello AR UI.
    view = HelloArView(this)
    lifecycle.addObserver(view)
    setContentView(view.root)

    // Sets up an example renderer using our HelloARRenderer.
    SampleRender(view.surfaceView, renderer, assets)

    depthSettings.onCreate(this)
    instantPlacementSettings.onCreate(this)

    val nfcButton = findViewById<Button>(R.id.NFCButton)
    nfcButton.setOnClickListener {
      val Intent = Intent(this, NFCActivity::class.java)
      startActivity(Intent);
    }

    val clearDatabaseDebugButton = findViewById<Button>(R.id.clearFarm)
    clearDatabaseDebugButton.setOnClickListener {

      renderer.clearAnchorGPS()
      playerViewModel.deleteAllFarm()

    }

    val debugFarmPlaceButton = findViewById<Button>(R.id.debugFarm)
    debugFarmPlaceButton.setOnClickListener {

      renderer.clearAnchorGPS()

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
              println("Farm longitude: ${farm.longitude}")
              println("Farm Altitude: ${farm.altitude}")
              println("Farm qx: ${farm.qx}")
              println("Farm qy: ${farm.qy}")
              println("Farm qz: ${farm.qz}")
              println("Farm qw: ${farm.qw}")

              // Test only first farm
              // TODO: Test if place all the farms
              val anchor : Anchor = earth.createAnchor(farm.latitude, farm.longitude, farm.altitude,
                farm.qx, farm.qy, farm.qz, farm.qw)

              // TODO: Handle exception if farm is empty
              renderer.addAnchorGPS(anchor)
              //farmList.add(newEntity)
            }
          }
        })


      }


    }





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
            println("Farm longitude: ${farm.longitude}")
            println("Farm Altitude: ${farm.altitude}")
            //farmList.add(newEntity)
          }
        }
      })
    }
  }

  public fun addFarm(result : HitResult) {

    //TODO: Get pose from firstHitResult and send it to do GPS stuff
    var pose : GeospatialPose = earth.getGeospatialPose(result.hitPose)
    Log.d("Hit Result (Geospatial Pose)", "longitude: ${pose.longitude}")
    Log.d("Hit Result (Geospatial Pose)", "latitude: ${pose.latitude}")
    Log.d("Hit Result (Geospatial Pose)", "altitude: ${pose.altitude}")
    Log.d("Hit Result (Geospatial Pose)", "eastUpSouthQuaternion : ${pose.eastUpSouthQuaternion}")

    val newFarm = FarmItem(name = "Test Farm", lat = pose.latitude, long =  pose.longitude, alt = pose.altitude,
      qx_set = pose.eastUpSouthQuaternion[0], qy_set = pose.eastUpSouthQuaternion[1], qz_set = pose.eastUpSouthQuaternion[2], qw_set = pose.eastUpSouthQuaternion[3])
    playerViewModel.insert(newFarm)

    // For testing
    printAllFarmItem(playerViewModel.allFarm)
    //var locationValueList : MutableList<Double> = mutableListOf()
    //getGPSLocation(locationValueList)
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
}
