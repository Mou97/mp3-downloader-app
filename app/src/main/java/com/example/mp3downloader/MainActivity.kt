package com.example.mp3downloader

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_CODE: Int =1000
    private var downloadID: Long = 0
    private var mediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadButton.setOnClickListener {
//            check permissions
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
//                request permission
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),STORAGE_PERMISSION_CODE)
            }else{
//                start download
                if(link.text.toString().isNotBlank()){
                    startDownload()
                }
            }

        }

        playPauseBtn.setOnClickListener {

        }

    }

    private fun startDownload() {

        val url = link.text.toString()

        val filename = url.substring(url.lastIndexOf("/")+ 1)
        Toast.makeText(this, filename, Toast.LENGTH_SHORT).show()


        val request = DownloadManager.Request(Uri.parse(url))
        with(request){
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            setAllowedOverRoaming(false)
            setTitle(filename)
            setDescription("")
            setMimeType("audio/MP3")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        }
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadID = manager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadID)

        Thread(Runnable {
            var downloading = true
            while (downloading){
                val cursor:Cursor = manager.query(query)
                cursor.moveToFirst()
                if(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL){
                    this.runOnUiThread {
                        Toast.makeText(this, "done downloading", Toast.LENGTH_SHORT).show()
                        mediaPlayer = MediaPlayer()
                        mediaPlayer?.setDataSource(Environment.DIRECTORY_DOWNLOADS + filename)
//                        TODO: finish this
                    }
                }
                cursor.close()
            }
        }).start()
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            STORAGE_PERMISSION_CODE->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startDownload()
                }else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
