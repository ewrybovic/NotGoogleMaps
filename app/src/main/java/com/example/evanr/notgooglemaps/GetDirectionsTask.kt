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
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

// AsyncTask for requesting the Directions API
class GetDirectionsTask (parentActivity: MapsActivity) : AsyncTask<String, Void, Boolean>() {

    private var path: MutableList<List<LatLng>> = ArrayList()
    private var instructions: MutableList<String> = ArrayList()
    private var distances: MutableList<String> = ArrayList()
    private val parentActivity: WeakReference<MapsActivity>
    private lateinit var steps: JSONArray
    private lateinit var directionsRequest: StringRequest

    init {
        // get the map activity so this class can edit the map
        this.parentActivity = WeakReference<MapsActivity>(parentActivity)
    }

    // IDK how to get this to work without having a return type
    override fun doInBackground(vararg urlDirections: String?): Boolean {
        var didComplete = false
        directionsRequest = object : StringRequest(
            Request.Method.GET, urlDirections[0], Response.Listener<String> { response ->
                //val jsonResponse = JSONObject(response)
                val routes = JSONObject(response).getJSONArray("routes")

                // Check if there was an error with the request
                if (routes.length() > 0) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    steps = legs.getJSONObject(0).getJSONArray("steps")

                    // For every step add them to the path variable
                    for (i in 0 until steps.length()) {
                        // Parse the point to draw on the map
                        val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                        // Parse teh step instructions
                        val step = steps.getJSONObject(i).getString("html_instructions")
                        val dist = steps.getJSONObject(i).getJSONObject("distance").getString("text")

                        // Add the values to the list
                        path.add(PolyUtil.decode(points))
                        instructions.add(step)
                        distances.add(dist)
                    }
                    didComplete = true
                }
            }, Response.ErrorListener { _ ->
                didComplete = false
            }) {}

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
            Log.i("Async", directionsRequest.toString())

            // This should get all values form the instructions list to an array
            activity!!.instructions = Array<String>(instructions.size){
                i: Int -> "Step " +(i+1).toString() + ": " + instructions[i] + " for " + distances[i]
            }

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