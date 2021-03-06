package com.example.evanr.notgooglemaps

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.location.Geocoder
import android.os.AsyncTask
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var googleMap: GoogleMap
    lateinit var instructions: Array<String>
    private lateinit var task: GetDirectionsTask
    private var firstLocation: String = ""
    private var secondLocation: String = ""
    private lateinit var firstLatLng: LatLng
    private lateinit var secondLatLng: LatLng

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
        this.googleMap = googleMap
        Toast.makeText(this, "Loading Please Wait.", Toast.LENGTH_SHORT).show()

        // Geocoder translates an address to a latitude and longitude location
        val geocoder = Geocoder(this, Locale.getDefault())

        // Run the geocoder api in separate thread
        doAsync{

            // getFromLocation returns in json format
            val firstAddressList = geocoder.getFromLocationName(firstLocation, 1)
            val secondAddressList = geocoder.getFromLocationName(secondLocation, 1)
            if (firstAddressList.size > 0 && secondAddressList.size > 0) {
                // get the first entry...also its the only entry
                val firstAddress = firstAddressList.get(0)
                val secondAddress = secondAddressList.get(0)

                // Extract lat and long coordinates
                firstLatLng = LatLng(firstAddress.latitude, firstAddress.longitude)
                secondLatLng = LatLng(secondAddress.latitude, secondAddress.longitude)

                // Create the bounds from the LatLng vars
                val bounds = makeBounds()

                // Threadsafe call to move the map
                uiThread {
                    setMap()

                    // Set the onclick listeners only if we get this far
                    googleMap.setOnMapClickListener {
                        onMapClick(it)
                    }

                    googleMap.setOnMapLongClickListener{
                        onMapLongClick(it)
                    }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.steps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId){
            R.id.steps -> {
                if (task.status == AsyncTask.Status.FINISHED) {
                    val i = Intent(this, StepsActivity::class.java)
                    i.putExtra("instructions", instructions)
                    startActivity(i)
                }
                else{
                    Toast.makeText(this, "Please wait for instructions to be ready", Toast.LENGTH_LONG).show()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
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

    private fun makeBounds(): LatLngBounds{
        val builder: LatLngBounds.Builder = LatLngBounds.Builder()
        builder.include(firstLatLng)
        builder.include(secondLatLng)
        return builder.build()
    }

    // Moves the map and zoom in to the correct bounds
    private fun setMap(){
        Toast.makeText(this, "Zooming to Location", Toast.LENGTH_SHORT ).show()

        // Add Markers
        googleMap.addMarker(MarkerOptions().position(firstLatLng).title(firstLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
        googleMap.addMarker(MarkerOptions().position(secondLatLng).title(secondLocation))

        // margin between end of screen and the bounds
        val padding = 155

        // Move the camera to the bounding location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(makeBounds(), padding))

        // Get the directions in an AsyncTask won't work in anko
        task = GetDirectionsTask(this)
        task.execute(createUrlDirection(firstLatLng, secondLatLng))
    }

    private fun onMapClick(newSecondLatLng: LatLng){
        // Clear the map of all markers
        googleMap.clear()
        secondLatLng = newSecondLatLng
        secondLocation = newSecondLatLng.toString()
        setMap()
    }

    private fun onMapLongClick(newFirstLatLng: LatLng){
        // Clear the map of all markers
        googleMap.clear()
        firstLatLng = newFirstLatLng
        firstLocation = newFirstLatLng.toString()
        setMap()
    }
}
