package com.havrebollsolutions.ttpoademoapp

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.FEATURE_CAMERA_FRONT

data class CameraAvailability(
    val hasRearCamera: Boolean,
    val hasFrontCamera: Boolean
)

object DeviceUtils {


    /**
     * Checks if a camera is available on the device.
     * @param context The application context.
     * @return A [CameraAvailability] object indicating the availability of both rear and front cameras.
     *
     */
    fun getAvailableCameras(context: Context): CameraAvailability {
        val packageManager = context.packageManager

        // 1. Check for the Rear Camera
        // FEATURE_CAMERA_ANY checks for the presence of any camera, usually the rear one.
        val hasRear = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

        // 2. Check for the Front Camera
        val hasFront = packageManager.hasSystemFeature(FEATURE_CAMERA_FRONT)

        // 3. Return the comprehensive result object
        return CameraAvailability(
            hasRearCamera = hasRear,
            hasFrontCamera = hasFront
        )
    }
}