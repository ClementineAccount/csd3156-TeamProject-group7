package com.csd3156.team7

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class FarmAdapter(private val farms: List<Location>, private val onItemClick: (LatLng) -> Unit) :
    RecyclerView.Adapter<FarmAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val farmNameTextView: TextView = itemView.findViewById(R.id.farmNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.farm_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val farm = farms[position]
        holder.farmNameTextView.text = "Farm $position"
        holder.itemView.setOnClickListener {
            onItemClick(LatLng(farm.latitude, farm.longitude))
        }

        // Log the size of the farms list
        Log.d("FarmAdapter", "Size of farms list: ${farms.size}")
    }


    override fun getItemCount(): Int {
        return farms.size
    }
}

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val farms: MutableList<Location> = mutableListOf()
    private var farmRadius: Double = 100.0 // Default radius
    private val strokeColor: Int = Color.argb(255, 0, 0, 0)
    private val fillColor: Int = Color.argb(255, 0, 255, 0)
    private var selectedFarmIndex: Int = -1 // Variable to track the selected farm
    private var isMapStyleEnabled = true
    private lateinit var farmRecyclerView: RecyclerView
    private lateinit var farmAdapter: FarmAdapter

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

        val toggleStyleButton = findViewById<Button>(R.id.toggleStyleButton)
        toggleStyleButton.setOnClickListener { onToggleMapStyleClick() }

        farmRecyclerView = findViewById(R.id.farmRecyclerView)
        farmAdapter = FarmAdapter(farms) { selectedFarmLatLng ->
            moveCameraToSelectedFarm(selectedFarmLatLng)
        }
        farmRecyclerView.layoutManager = LinearLayoutManager(this)
        farmRecyclerView.adapter = farmAdapter

        val showFarmsButton = findViewById<Button>(R.id.showFarmsButton)
        showFarmsButton.setOnClickListener { onShowFarmsClick() }
    }

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

    private fun addFarm(latitude: Double, longitude: Double) {
        val farmLocation = Location("")
        farmLocation.latitude = latitude
        farmLocation.longitude = longitude
        farms.add(farmLocation)
        farmAdapter.notifyDataSetChanged()
    }

    private fun deleteFarm(latLng: LatLng) {
        for ((index, farmLocation) in farms.withIndex()) {
            val farmLatLng = LatLng(farmLocation.latitude, farmLocation.longitude)
            if (areLocationsEqual(latLng, farmLatLng)) {
                farms.removeAt(index)
                selectedFarmIndex = -1
                farmAdapter.notifyDataSetChanged()
                return
            }
        }
    }

    private fun refreshMap() {
        try {
            mMap.clear()
            for ((index, farmLocation) in farms.withIndex()) {
                val location = LatLng(farmLocation.latitude, farmLocation.longitude)

                val circleOptions = CircleOptions()
                    .center(location)
                    .radius(farmRadius)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor)

                mMap.addCircle(circleOptions)

                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 150f // Sharpness
                }

                val textBitmap = createTextBitmap("Farm $index", paint)
                mMap.addGroundOverlay(
                    GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromBitmap(textBitmap))
                        .position(location, farmRadius.toFloat() * 5)
                )
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
        val xOffset = 125f;
        val yOffset = 15f
        canvas.drawText(text, xOffset, yOffset, paint)

        return bitmap
    }

    private fun onShowFarmsClick() {
        farmRecyclerView.visibility = if (farmRecyclerView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun moveCameraToSelectedFarm(selectedFarmLatLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedFarmLatLng, DEFAULT_ZOOM))
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
            if (::mMap.isInitialized) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
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

    private fun areLocationsEqual(location1: LatLng, location2: LatLng): Boolean {
        val epsilon = 1e-5
        return Math.abs(location1.latitude - location2.latitude) < epsilon &&
                Math.abs(location1.longitude - location2.longitude) < epsilon
    }
}
