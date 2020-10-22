package com.odlsoon.basic_tensorflow_lite.digit_classifier

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import com.odlsoon.basic_tensorflow_lite.R
import kotlinx.android.synthetic.main.digit_classifier_act.*

@Suppress("UNREACHABLE_CODE")
class DigitClassifierActivity : AppCompatActivity() {
    private var digitClassifier = DigitClassifier(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.digit_classifier_act)

        initDrawView()
        initListener()
        initDigitClassifier()
    }

    override fun onDestroy() {
        digitClassifier.close()
        super.onDestroy()
    }

    private fun initDrawView(){
        draw_view.setStrokeWidth(70.0f)
        draw_view.setColor(Color.WHITE)
        draw_view.setBackgroundColor(Color.BLACK)

        draw_view.setOnTouchListener { _, event ->
            draw_view.onTouchEvent(event)

            if (event.action == MotionEvent.ACTION_UP) classifyDrawing()
            true
        }
    }

    private fun initListener(){
        clear_button.setOnClickListener {
            draw_view.clearCanvas()
            predicted_text.text = "Please draw a digit"
        }
    }

    private fun initDigitClassifier(){
        digitClassifier
            .initialize()
            .addOnFailureListener { _ ->
                Toast.makeText(this, "Error to setting up digit classifier", Toast.LENGTH_SHORT).show()
            }
    }

    private fun classifyDrawing() {
        val bitmap = draw_view.getBitmap()

        if ((bitmap != null) && (digitClassifier.isInitialized)) {
            digitClassifier
                .classifyAsync(bitmap)
                .addOnSuccessListener { text -> predicted_text.text = text }
                .addOnFailureListener { _ -> predicted_text.text = "Error classifying drawing"}
        }
    }
}
