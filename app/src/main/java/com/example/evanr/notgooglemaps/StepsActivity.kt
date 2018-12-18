package com.example.evanr.notgooglemaps

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_steps.*

class StepsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        val intent = intent
        val array = intent.getStringArrayExtra("instructions")

        // Loop through the instructions array and add TextView for each step
        for(i in 0 until array.size){
            val textView: TextView = TextView(this)

            // remove the html tags from the string
            textView.text = Html.fromHtml(array[i]).toString().replace("\n", " ").trim()
            textView.textSize = 20.0F
            stepsActivityLayout.addView(textView)

            // create a blank line to seperate the steps
            val view: View = View(this)
            view.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5)
            view.setBackgroundColor(Color.parseColor("#B3B3B3"))
            stepsActivityLayout.addView(view)
        }

        // Inset a blank TextVew because the scrollview will cut off the last TextView
        stepsActivityLayout.addView(TextView(this))
    }
}
