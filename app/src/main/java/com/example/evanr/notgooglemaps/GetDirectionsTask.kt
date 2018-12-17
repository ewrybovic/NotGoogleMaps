package com.example.evanr.notgooglemaps

import android.graphics.Color
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.lang.ref.WeakReference

// AsyncTask for requesting the Directions API
class GetDirectionsTask (parentActivity: MapsActivity) : AsyncTask<String, Void, Boolean>() {

    private var path: MutableList<List<LatLng>> = ArrayList()
    private var parentActivity: WeakReference<MapsActivity>
    private lateinit var directionsRequest: StringRequest

    init {
        // get the map activity so this class can edit the map
        this.parentActivity = WeakReference<MapsActivity>(parentActivity)
    }

    // IDK how to get this to work without having a return type
    override fun doInBackground(vararg urlDirections: String?): Boolean {
        var didComplete: Boolean = false
       directionsRequest = object : StringRequest(
            Request.Method.GET, urlDirections[0], Response.Listener<String> {
                    response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")

               // For every step add them to the path variable
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
               didComplete = true
            }, Response.ErrorListener {
                    _ ->
               didComplete = false
            }){}

        // Add the request to a Volley request Queue
        val requestQueue = Volley.newRequestQueue(parentActivity.get())
        requestQueue.add(directionsRequest)

        // Sleep the thread because the json request runs in a seperate thread and will not finish in time
        Thread.sleep(1000)
        return didComplete
    }

    // Add the poly lines to the map
    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        val activity = parentActivity.get()

        if (result == true) {
            // Log the request url cause I want to see it
            Log.e("Async", directionsRequest.toString())

            // Add the polylines to the map
            for (i in 0 until path.size) {
                activity!!.googleMap.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }
        else{
            Toast.makeText(activity, "Could not parse directions", Toast.LENGTH_LONG).show()
        }
    }
}