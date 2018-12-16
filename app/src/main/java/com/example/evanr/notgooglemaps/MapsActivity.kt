package com.example.evanr.notgooglemaps

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.location.Geocoder
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var firstLocation: String = ""
    private var secondLocation: String = ""

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
            // getFromLocation returns in json format
            val addressList = geocoder.getFromLocationName(firstLocation, 1)
            if (addressList.size > 0) {
                // get the first entry...also its the only entry
                val address = addressList.get(0)
                val latLngOrigin = LatLng(address.latitude, address.longitude)

                // Threadsafe call to move the map
                uiThread {
                    Toast.makeText(this@MapsActivity, "Centering on First Location", Toast.LENGTH_LONG ).show()
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))
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
}
