package com.example.adopt_pet_app.data.repository

import android.graphics.Bitmap
import androidx.camera.view.LifecycleCameraController

interface CameraRepository {
    suspend fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap, String) -> Unit
    )

    suspend fun savePhotoToPermanentStorage(bitmap: Bitmap)

    suspend fun recordVideo(
        controller: LifecycleCameraController
    )


}