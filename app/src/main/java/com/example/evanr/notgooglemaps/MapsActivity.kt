package com.example.evanr.notgooglemaps

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.location.Geocoder
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
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

                    // get the directions in an asyncTask, for some reason it won't work in this
                    val task = GetDirectionsTask(this@MapsActivity)
                    task.execute(createUrlDirection(firstLatLng, secondLatLng))
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
    fun createUrlDirection(origin: LatLng, dest: LatLng): String{
        var urlDirection = "https://maps.googleapis.com/maps/api/directions/json?origin="
        urlDirection += origin.latitude.toString() + ","
        urlDirection += origin.longitude.toString() + "&destination="
        urlDirection += dest.latitude.toString() + ","
        urlDirection += dest.longitude.toString() + "&key="
        urlDirection += getString(R.string.google_maps_key)
        return urlDirection
    }
}
