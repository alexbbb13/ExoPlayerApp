package com.alexb.testexoplayerapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.alexb.testexoplayerapp.ui.MainActivityViewModel.Companion.ROTATE_Y_THRESHOLD
import com.alexb.testexoplayerapp.ui.MainActivityViewModel.Companion.ROTATE_Z_THRESHOLD

class ShakeDetector : SensorEventListener {
    private var mShakeListener: OnShakeListener? = null
    private var mShakeCount = 0
    private var mShakeTimestamp = 0L
    private var mRotateZTimestamp = 0L
    private var mRotateYTimestamp = 0L
    override fun onSensorChanged(event: SensorEvent) {
        if (mShakeListener != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            // gForce will be close to 1 when there is no movement.
            val gForce: Double = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble())
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val now = System.currentTimeMillis()
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return
                }
                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0
                }
                mShakeTimestamp = now
                mShakeCount++
                mShakeListener!!.onShake(mShakeCount)
            }
            if (gX > ROTATE_Z_THRESHOLD || gX < -ROTATE_Z_THRESHOLD) {
                //Big enough rotation
                val now = System.currentTimeMillis()
                if (mRotateZTimestamp > 0 && now > mRotateZTimestamp + ROTATE_Z_HOLD_TIME_MS) {
                    mShakeListener!!.onRotateZ(gX)
                    mRotateZTimestamp = now
                }
            } else {
                //reset the timestamp
                mRotateZTimestamp = System.currentTimeMillis()
            }
            if (gY > ROTATE_Y_THRESHOLD || gY < -ROTATE_Y_THRESHOLD) {
                //Big enough rotation
                val now = System.currentTimeMillis()
                if (mRotateYTimestamp > 0 && now > mRotateYTimestamp + ROTATE_Y_HOLD_TIME_MS) {
                    mShakeListener!!.onRotateY(gY)
                    mRotateYTimestamp = now
                }
            } else {
                //reset the timestamp
                mRotateYTimestamp = System.currentTimeMillis()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    interface OnShakeListener {
        fun onShake(count: Int)
        fun onRotateZ(value: Float)
        fun onRotateY(value: Float)
    }

    fun setOnShakeListener(listener: OnShakeListener?) {
        mShakeListener = listener
    }

    fun resume() {
        mSensorManager!!.registerListener(
            this,
            mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun pause() {
        mSensorManager!!.unregisterListener(this)
    }

    fun init(context: Context) {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private var mSensorManager: SensorManager? = null

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        private const val SHAKE_SLOP_TIME_MS = 500
        private const val SHAKE_COUNT_RESET_TIME_MS = 3000
        private const val ROTATE_Z_HOLD_TIME_MS = 500
        private const val ROTATE_Y_HOLD_TIME_MS = 500
    }
}