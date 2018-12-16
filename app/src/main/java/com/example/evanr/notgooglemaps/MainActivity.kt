package com.example.evanr.notgooglemaps

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGo.setOnClickListener{goToMaps()}
    }

    private fun goToMaps(){
        if(!editFirstLocation.text.isNullOrBlank() && !editSecondLocation.text.isNullOrBlank()) {
            val i = Intent(this, MapsActivity::class.java)
            i.putExtra("firstLocation", editFirstLocation.text.toString())
            i.putExtra("secondLocation", editSecondLocation.text.toString())
            startActivity(i)
        }else{
            Toast.makeText(this, "Please enter all data", Toast.LENGTH_LONG).show()
        }
    }
}
