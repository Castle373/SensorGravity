package com.diego.gravitysensor

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Audio
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.getSystemService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var tZ:TextView
    lateinit var tX:TextView
    lateinit var tY:TextView
    lateinit var sensorM:SensorManager
    lateinit var sensor:Sensor
    var sensorActivo:Boolean = false
    lateinit var audio: AudioManager
    lateinit var outputFile: File
    var fos: FileOutputStream? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val directory = File(Environment.getExternalStorageDirectory(), "SensorData")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        outputFile = File(directory, "sensor_data.txt")

        try {
            fos = FileOutputStream(outputFile, true) // Usar true para agregar datos al archivo existente
        } catch (e: IOException) {
            e.printStackTrace()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        tX = findViewById(R.id.textView)
        tY = findViewById(R.id.textView2)
        tZ = findViewById(R.id.textView3)
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sensorM = getSystemService(SENSOR_SERVICE) as SensorManager
        if(sensorM.getDefaultSensor(Sensor.TYPE_GRAVITY)!=null){
            sensor= sensorM.getDefaultSensor(Sensor.TYPE_GRAVITY)!!
            sensorActivo=true
        } else {
            tX.text = "SIN SENSOR DISPONIBLE"
            sensorActivo=false
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if(fos!=null){
                try {
                    fos!!.write(("X" + event.values[0] + " m/s2\n").toByteArray())
                    fos!!.write(("Y" + event.values[1] + " m/s2\n").toByteArray())
                    fos!!.write(("Z" + event.values[2] + " m/s2\n").toByteArray())
                    fos!!.flush()
                    println("Sola")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }


            tX.text = "X" + event.values[0] + " m/s2"
            tY.text = "Y" + event.values[1] + " m/s2"
            tZ.text = "Z" + event.values[2] + " m/s2"

            if (event.values[2]<-9.7){
                window.decorView.setBackgroundColor(Color.GREEN)
                audio.ringerMode=AudioManager.RINGER_MODE_VIBRATE
            }
            else if (event.values[0]>9.7||event.values[0]<-9.7){
                window.decorView.setBackgroundColor(Color.MAGENTA)
                audio.ringerMode=AudioManager.RINGER_MODE_VIBRATE
            }
            else if (event.values[1]>9.7||event.values[1]<-9.7){
                window.decorView.setBackgroundColor(Color.YELLOW)
                audio.ringerMode=AudioManager.RINGER_MODE_VIBRATE
            }
            else if (event.values[2]>9.7){
                window.decorView.setBackgroundColor(Color.CYAN)
                audio.ringerMode=AudioManager.RINGER_MODE_VIBRATE
            }
            else{
                window.decorView.setBackgroundColor(Color.WHITE)
                audio.ringerMode=AudioManager.RINGER_MODE_NORMAL
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        if(sensorM.getDefaultSensor(Sensor.TYPE_GRAVITY)!=null)
            sensorM.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        if(sensorM.getDefaultSensor(Sensor.TYPE_GRAVITY)!=null)
            sensorM.unregisterListener(this,sensor)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fos!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}