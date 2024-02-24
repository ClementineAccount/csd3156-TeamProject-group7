package com.csd3156.team7

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val farms: MutableList<Location> = mutableListOf()
    private var farmRadius: Double = 100.0 // Default radius
    private val strokeColor: Int = Color.argb(255, 0, 0, 0)
    private val fillColor: Int = Color.argb(255, 0, 255, 0)
    private var selectedFarmIndex: Int = -1 // Variable to track the selected farm

    // I used ChatGPT to get the Location API stuff - Clementine
    // You can see me struggle here: https://chat.openai.com/share/1ca2cba9-00ef-4279-9608-6eb88915beb3
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val DEFAULT_ZOOM = 15f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        mMap.setOnMapClickListener { latLng ->
            for ((index, farmLocation) in farms.withIndex()) {
                val farmLatLng = LatLng(farmLocation.latitude, farmLocation.longitude)
                val distance = calculateDistance(latLng, farmLatLng)

                if (distance <= farmRadius) {
                    deleteFarm(farmLatLng)
                    refreshMap()
                    return@setOnMapClickListener
                }
            }
            addFarm(latLng.latitude, latLng.longitude)
            refreshMap()
        }
        moveCameraToLastKnownLocation()
    }

    // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    private fun addFarm(latitude: Double, longitude: Double) {
        val farmLocation = Location("")
        farmLocation.latitude = latitude
        farmLocation.longitude = longitude
        farms.add(farmLocation)
    }

    private fun deleteFarm(latLng: LatLng) {
        for ((index, farmLocation) in farms.withIndex()) {
            val farmLatLng = LatLng(farmLocation.latitude, farmLocation.longitude)
            if (areLocationsEqual(latLng, farmLatLng)) {
                farms.removeAt(index)
                selectedFarmIndex = -1
                refreshMap()
                return
            }
        }
    }

    private fun refreshMap() {
        mMap.clear()
        for ((index, farmLocation) in farms.withIndex()) {
            val circleOptions = CircleOptions()
                .center(LatLng(farmLocation.latitude, farmLocation.longitude))
                .radius(farmRadius)
                .strokeColor(strokeColor)
                .fillColor(fillColor)

            mMap.addCircle(circleOptions)

            val userLocation = mMap.myLocation
            if (userLocation != null) {
                val distance = calculateDistance(
                    LatLng(userLocation.latitude, userLocation.longitude),
                    LatLng(farmLocation.latitude, farmLocation.longitude)
                )
                if (distance <= farmRadius) {
                    Log.d("FarmLog", "Inside Farm $index")
                }
            }
        }
    }

    private fun moveCameraToLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Got last known location. In some rare situations, this can be null.
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM))
                    }
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, enable the "My Location" layer
            if (::mMap.isInitialized) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mMap.isMyLocationEnabled = true
            }
        } else {
            // Permission denied, handle accordingly (e.g., show a message to the user)
        }
    }
}

private fun calculateDistance(point1: LatLng, point2: LatLng): Float {
    val result = FloatArray(1)
    Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, result)
    return result[0]
}

private fun areLocationsEqual(location1: LatLng, location2: LatLng): Boolean {
    val epsilon = 1e-5 // Adjust this value based on your precision requirement
    return Math.abs(location1.latitude - location2.latitude) < epsilon &&
            Math.abs(location1.longitude - location2.longitude) < epsilon
}