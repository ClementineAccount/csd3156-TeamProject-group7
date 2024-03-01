package com.csd3156.team7

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.csd3156.team7.PlayerInventoryDatabase.Companion.getDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.ar.core.examples.kotlin.helloar.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val farms: MutableList<Location> = mutableListOf()
    private lateinit var farmDao: FarmDao
    private lateinit var farmRepository: FarmListRepository
    private var farmRadius: Double = 100.0
    private var selectedFarmIndex: Int = -1
    private var isMapStyleEnabled = true
    private lateinit var scrollView: ScrollView
    private lateinit var farmNamesTextView: TextView
    private var defaultStrokeColor: Int = Color.argb(255, 206, 189, 173)
    private val fillColor: Int = Color.argb(255, 101,254,8)
    private val zoomFarm = 16f
    private val zoomSpeed = 800

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

        val toggleStyleButton = findViewById<Button>(R.id.toggleStyleButton)
        toggleStyleButton.setOnClickListener { onToggleMapStyleClick() }

        scrollView = findViewById(R.id.scrollView)
        farmNamesTextView = findViewById(R.id.farmNamesTextView)
        farmDao = getDatabase(this).farmDao()
        farmRepository = FarmListRepository(getDatabase(this).farmDao())

        lifecycleScope.launchWhenCreated {
            try {
                farmRepository.alLFarms.collect { farmItems ->
                    farms.addAll(farmItems.map { farmItem ->
                        Location("").apply {
                            latitude = farmItem.latitude
                            longitude = farmItem.longtitude
                        }
                    })

                    refreshMap()
                    updateFarmNames()
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error in collecting farm data: ${e.message}")
            }
        }

        val showFarmsButton = findViewById<Button>(R.id.showFarmsButton)
        showFarmsButton.setOnClickListener { onShowFarmsClick() }
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

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        refreshMap()
        updateFarmNames()

        mMap.setOnMapClickListener { latLng ->
            farms.forEach { farmLocation ->
                val farmLatLng = LatLng(farmLocation.latitude, farmLocation.longitude)
                val distance = calculateDistance(latLng, farmLatLng)

                if (distance <= farmRadius) {
                    deleteFarm(farmLatLng)
                    refreshMap()
                    updateFarmNames()
                    return@setOnMapClickListener
                }
            }

            addFarm(latLng.latitude, latLng.longitude)
            refreshMap()
            updateFarmNames()
        }

        moveCameraToLastKnownLocation()
    }

    // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    private fun onToggleMapStyleClick() {
        isMapStyleEnabled = !isMapStyleEnabled

        try {
            if (isMapStyleEnabled) {
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
                )
            } else {
                mMap.setMapStyle(null)
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivity", "Resource not found: ${e.message}")
        }
    }

    private fun refreshMap() {
        try {
            val circleOptionsList = mutableListOf<CircleOptions>()
            val groundOverlayOptionsList = mutableListOf<GroundOverlayOptions>()

            mMap.let { map ->
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    for ((index, farmLocation) in farms.withIndex()) {
                        val location = LatLng(farmLocation.latitude, farmLocation.longitude)

                        val distance = if (map.myLocation != null) {
                            calculateDistance(
                                LatLng(map.myLocation.latitude, map.myLocation.longitude),
                                location
                            )
                        } else {
                            0f
                        }

                        val isWithinRadius = distance <= farmRadius
                        val scale = if (isWithinRadius) 1.5f else 1f
                        val duration = 1000L
                        val valueAnimator = ValueAnimator.ofFloat(1f, scale)
                        valueAnimator.duration = duration
                        valueAnimator.addUpdateListener { animation ->
                            val animatedValue = animation.animatedValue as Float

                            map.addCircle(
                                CircleOptions()
                                    .center(location)
                                    .radius(farmRadius * animatedValue)
                                    .strokeColor(if (isWithinRadius) fillColor else defaultStrokeColor)
                                    .fillColor(fillColor)
                            )
                        }

                        valueAnimator.start()

                        val paint = Paint().apply {
                            color = Color.BLACK
                            textSize = 20f
                        }

                        val textBitmap = createTextBitmap("Farm ${index + 1}", paint)

                        val groundOverlayOptions = GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromBitmap(textBitmap))
                            .position(location, farmRadius.toFloat() * 5)

                        groundOverlayOptionsList.add(groundOverlayOptions)
                    }

                    map.clear()

                    for (groundOverlayOptions in groundOverlayOptionsList) {
                        map.addGroundOverlay(groundOverlayOptions)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MapsActivity", "Error refreshing map: ${e.message}")
        }
    }

    private fun createTextBitmap(text: String, paint: Paint): Bitmap {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val bitmap = Bitmap.createBitmap((farmRadius * 3).toInt(), (farmRadius).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        paint.textSize = 15f
        val xOffset = 125f
        val yOffset = 15f
        canvas.drawText(text, xOffset, yOffset, paint)

        return bitmap
    }

    private fun addFarm(latitude: Double, longitude: Double) {
        val farmLocation = Location("")
        farmLocation.latitude = latitude
        farmLocation.longitude = longitude
        farms.add(farmLocation)
    }

    private fun deleteFarm(latLng: LatLng) {
        for ((index, farmLocation) in farms.withIndex()) {
            val farmLatLng = LatLng(farmLocation.latitude, farmLocation.longitude)
            if (latLng.areEqual(farmLatLng)) {
                farms.removeAt(index)
                selectedFarmIndex = -1
                return
            }
        }
    }

    private fun onShowFarmsClick() {
        scrollView.visibility = if (scrollView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        if (scrollView.visibility == View.VISIBLE) {
            updateFarmNames()
        }
    }

    private fun updateFarmNames() {
        val farmNames = List(farms.size) { index -> "Farm ${index + 1}" }.toTypedArray()
        val builder = SpannableStringBuilder()
        farmNames.forEachIndexed { index, farmName ->
            builder.append(farmName)
            builder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onFarmNameClicked(index)
                }
            }, builder.length - farmName.length, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (index < farmNames.size - 1) {
                builder.append("\n")
            }
        }

        farmNamesTextView.text = builder
        farmNamesTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun onFarmNameClicked(index: Int) {
        if (index in farms.indices) {
            selectedFarmIndex = index
            val selectedFarmLatLng = LatLng(farms[index].latitude, farms[index].longitude)
            moveCameraToSelectedFarm(selectedFarmLatLng, zoomFarm)
        }
    }

    private fun moveCameraToSelectedFarm(selectedFarmLatLng: LatLng, zoomLevel: Float) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(selectedFarmLatLng, zoomLevel),
            zoomSpeed,
            null
        )
    }

    private fun moveCameraToLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM))
                        Log.d("MapsActivity", "User - Latitude: ${it.latitude}, Longitude: ${it.longitude}")
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

    private fun calculateDistance(point1: LatLng, point2: LatLng): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            point1.latitude, point1.longitude,
            point2.latitude, point2.longitude, result
        )
        return result[0]
    }

    fun LatLng.areEqual(other: LatLng, epsilon: Double = 1e-5): Boolean {
        return kotlin.math.abs(this.latitude - other.latitude) < epsilon &&
                kotlin.math.abs(this.longitude - other.longitude) < epsilon
    }
}