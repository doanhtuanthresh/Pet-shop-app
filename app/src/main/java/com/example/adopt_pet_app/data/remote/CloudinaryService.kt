//CloudinaryService
package com.example.adopt_pet_app.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object CloudinaryService {

    private const val CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/dt4wu7k0s/image/upload"
    private const val UPLOAD_PRESET = "unsigned_upload"

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(Exception("Không đọc được ảnh"))

            val imageBytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", requestBody)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()

            val request = Request.Builder()
                .url(CLOUDINARY_URL)
                .post(multipartBody)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            val responseBody = response.body?.string()
            Log.d("CloudinaryUpload", "Status: ${response.code}")
            Log.d("CloudinaryUpload", "Response: $responseBody")

            if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Upload thất bại: ${response.message}"))
            }

            val json = JSONObject(responseBody)
            val imageUrl = json.getString("secure_url")

            Result.success(imageUrl)

        } catch (e: Exception) {
            Log.e("CloudinaryService", "Lỗi upload: ${e.message}", e)
            Result.failure(e)
        }
    }
}
