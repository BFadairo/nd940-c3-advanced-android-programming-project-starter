package com.udacity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)


        val intent: Intent = intent
        val fileName = intent.getStringExtra("FILE_NAME")
        val status = intent.getStringExtra("FILE_STATUS")

        val fileStatusTextView = findViewById<TextView>(R.id.status_text)
        val fileNameView = findViewById<TextView>(R.id.file_name)

        when (status) {
            "SUCCESS" -> {
                fileStatusTextView.setTextColor(Color.GREEN)
            }
            "FAILURE" -> {
                fileStatusTextView.setTextColor(Color.RED)
            }
        }

        fileNameView.text = fileName
        fileStatusTextView.text = status

        findViewById<Button>(R.id.ok_button).setOnClickListener { finish() }
    }

}
