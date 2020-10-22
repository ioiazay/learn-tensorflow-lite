package com.odlsoon.basic_tensorflow_lite

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.button.MaterialButton
import com.odlsoon.basic_tensorflow_lite.digit_classifier.DigitClassifierActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMenu()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initMenu(){
        val components = mutableMapOf(
            "Digit Clasifier" to DigitClassifierActivity::class.java
        )

        components.forEach {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            val button = MaterialButton(this, null, R.attr.materialButtonStyle)

            params.setMargins(16, 16, 16, 16)
            button.layoutParams = params
            button.text = it.key
            button.setOnClickListener {_ ->
                startActivity(Intent(this, it.value))
            }

            ll_main.addView(button)
        }
    }
}
