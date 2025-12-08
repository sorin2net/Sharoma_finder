package com.example.sharoma_finder.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class UserManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Salvează numele
    fun saveName(name: String) {
        sharedPreferences.edit { putString("user_name", name) }
    }

    // Citește numele (default "Costi")
    fun getName(): String {
        return sharedPreferences.getString("user_name", "Costi") ?: "Costi"
    }

    // Salvează calea pozei (String)
    fun saveImagePath(path: String) {
        sharedPreferences.edit { putString("user_image_path", path) }
    }

    // Citește calea pozei
    fun getImagePath(): String? {
        return sharedPreferences.getString("user_image_path", null)
    }

    // Funcție critică: Copiază poza din galerie în fișierele aplicației
    // (Altfel, după restart, nu mai ai permisiune să vezi poza din galerie)
    fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileName = "profile_picture.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}