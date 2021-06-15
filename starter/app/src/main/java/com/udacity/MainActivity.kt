package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private lateinit var loadingButton: LoadingButton
    private lateinit var radioGroup: RadioGroup
    private var fileName: String? = null
    private val NOTIFICATION_INTENT_FILTER = "ACTION_NOTIFICATION_CLICKED"


    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        createNotificationChannel()
        loadingButton = findViewById(R.id.custom_button)
        radioGroup = findViewById(R.id.radioGroup)

        loadingButton.setOnClickListener {
            Log.v("MainActivity", "Loading Button Clicked")
            if (!fileName.isNullOrEmpty()) {
                download()
//                sendNotification(this, "Retrofit, A Type-safe library for making API calls on Android", "FAILURE")
            } else {
                Toast.makeText(this, "An Item must be checked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_button_glide ->
                    if (checked) {
                        fileName = view.text.toString()
                    }
                R.id.radio_button_load_app ->
                    if (checked) {
                        fileName = view.text.toString()
                    }
                R.id.radio_button_retrofit ->
                    if (checked) {
                        fileName = view.text.toString()
                    }
            }
        }
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Log.v("Receiver", "Received Intent from completed download $id")

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = id?.let { DownloadManager.Query().setFilterById(it) }
            val cursor = downloadManager.query(query)
            var fileStatus: String = ""

            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        fileStatus = "SUCCESS"
                    }
                    DownloadManager.STATUS_FAILED -> {
                        fileStatus = "FAILURE"
                    }
                }
            }
            loadingButton.endLoad()
            sendNotification(context, fileName, fileStatus)
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(context: Context?, fileName: String?, downloadStatus: String) {
        val detailActivityIntent = Intent(context, DetailActivity::class.java)
        detailActivityIntent.putExtra("FILE_NAME", fileName)
        detailActivityIntent.putExtra("FILE_STATUS", downloadStatus)

        val detailPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, detailActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(detailPendingIntent)
            .addAction(R.drawable.baseline_cloud_download_24, getString(R.string.notification_button), detailPendingIntent)
            .build()


        notificationManager.notify(R.string.notification_id, builder)
    }


    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
