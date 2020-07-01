package com.ouattararomuald.statussaver

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

class StorageHelper(private val context: Context) {

  companion object {
    const val WHATSAPP_STATUSES_FOLDER_PATH = "WhatsApp/Media/.Statuses"
    const val WHATSAPP_BUSINESS_STATUSES_FOLDER_PATH = "WhatsApp Business/Media/.Statuses"
    const val ANDROID = "Android"

    /** Checks if a volume containing external storage is available for read and write. */
    fun isExternalStorageWritable(): Boolean {
      return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /** Checks if a volume containing external storage is available to at least read. */
    fun isExternalStorageReadable(): Boolean {
      return Environment.getExternalStorageState() in
          setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
  }

  fun getWhatsAppStatusesFolderPath(): String? {
    if (!isExternalStorageReadable()) {
      return null
    }

    val appStorageRoot = Environment.getExternalStorageDirectory() //context.getExternalFilesDir(null)
    if (appStorageRoot != null) {
      /*val path = appStorageRoot.path
      if (!path.contains(ANDROID)) {
        return null
      }
      val externalStorageRootPath = path.substring(0 until path.indexOf(ANDROID))*/

      val whatsAppStatusFolder = File( "${appStorageRoot}/$WHATSAPP_STATUSES_FOLDER_PATH")
      if (whatsAppStatusFolder.exists() && whatsAppStatusFolder.isDirectory) {
        return whatsAppStatusFolder.absolutePath
      }
    }

    return null
  }
}