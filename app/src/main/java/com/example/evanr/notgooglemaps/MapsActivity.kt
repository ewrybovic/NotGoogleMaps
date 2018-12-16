package com.example.evanr.notgooglemaps

import android.graphics.Color
import android.icu.util.ULocale
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.location.Geocoder
import android.renderscript.ScriptGroup
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.*
import com.google.maps.android.PolyUtil

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.lang.Math.pow
import java.lang.Math.sqrt
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var firstLocation: String = ""
    private var secondLocation: String = ""
    private var directionRequest: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Get the data from the intent
        val intent = intent
        firstLocation = intent.getStringExtra("firstLocation")
        secondLocation = intent.getStringExtra("secondLocation")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Geocoder tanslates an address to a latitude and longitude location
        val geocoder = Geocoder(this, Locale.getDefault())

        // Run the geocoder api in seperate thread
        doAsync{

            uiThread {
                Toast.makeText(this@MapsActivity, "Loading Please Wait.", Toast.LENGTH_SHORT).show()
            }

            // getFromLocation returns in json format
            val firstAddressList = geocoder.getFromLocationName(firstLocation, 1)
            val secondAddressList = geocoder.getFromLocationName(secondLocation, 1)
            if (firstAddressList.size > 0 && secondAddressList.size > 0) {
                // get the first entry...also its the only entry
                val firstAddress = firstAddressList.get(0)
                val secondAddress = secondAddressList.get(0)

                // Extract lat and long coordinates
                val firstLatLng = LatLng(firstAddress.latitude, firstAddress.longitude)
                val secondLatLng = LatLng(secondAddress.latitude, secondAddress.longitude)

                // Create the bounds from the LatLng vars
                val builder: LatLngBounds.Builder = LatLngBounds.Builder()
                builder.include(firstLatLng)
                builder.include(secondLatLng)
                val bounds = builder.build()

                // Threadsafe call to move the map
                uiThread {
                    Toast.makeText(this@MapsActivity, "Zooming to Location", Toast.LENGTH_LONG ).show()

                    // Add Markers
                    mMap.addMarker(MarkerOptions().position(firstLatLng).title(firstLocation))
                    mMap.addMarker(MarkerOptions().position(secondLatLng).title(secondLocation))

                    // margin between end of screen and the bounds
                    val padding = 125

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                }
            }
            else{
                // Threadsafe call to a Toast
                uiThread{
                    Toast.makeText(this@MapsActivity, "Could not parse address", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Creates the url for the directions, not the best way lol
    fun createUrlDirection(originLat: Double, originLng: Double, destLat: Double, destLng: Double): String{
        var urlDirection = "https://maps.googleapis.com/maps/api/directions/json?origin="
        urlDirection += originLat.toString() + ","
        urlDirection += originLng.toString() + "&destination="
        urlDirection += destLat.toString() + ","
        urlDirection += destLng.toString() + "&key="
        urlDirection += getString(R.string.google_maps_key)
        return urlDirection
    }
}
