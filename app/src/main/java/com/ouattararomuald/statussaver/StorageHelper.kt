package com.ouattararomuald.statussaver

import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

class StorageHelper(private val context: Context) {

  companion object {
    const val WHATSAPP_STATUSES_FOLDER_PATH = "WhatsApp/Media/.Statuses"
    const val ANDROID_11_WHATSAPP_STATUSES_FOLDER_PATH = "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
    const val WHATSAPP_BUSINESS_STATUSES_FOLDER_PATH = "WhatsApp Business/Media/.Statuses"
    const val ANDROID_11_WHATSAPP_BUSINESS_STATUSES_FOLDER_PATH = "Android/media/com.whatsapp/WhatsApp Business/Media/.Statuses"

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

  fun getWhatsAppStatusesFolderPath(): String? = getStatusesFolder(isWhatsAppBusiness = false)

  fun getWhatsAppBusinessStatusesFolderPath(): String? = getStatusesFolder(isWhatsAppBusiness = true)

  private fun getStatusesFolder(isWhatsAppBusiness: Boolean = false): String? {
    if (!isExternalStorageReadable()) {
      return null
    }

    return if (Build.VERSION.SDK_INT < 30) {
      getStatusesFolder(isWhatsAppBusiness, whatsAppBusinessPath = WHATSAPP_BUSINESS_STATUSES_FOLDER_PATH, whatsAppPath = WHATSAPP_STATUSES_FOLDER_PATH)
    } else {
      getStatusesFolder(isWhatsAppBusiness, whatsAppBusinessPath = ANDROID_11_WHATSAPP_BUSINESS_STATUSES_FOLDER_PATH, whatsAppPath = ANDROID_11_WHATSAPP_STATUSES_FOLDER_PATH)
    }
  }

  private fun getStatusesFolder(isWhatsAppBusiness: Boolean, whatsAppBusinessPath: String, whatsAppPath: String): String? {
    val appStorageRoot = Environment.getExternalStorageDirectory()
    if (appStorageRoot != null) {
      val whatsAppStatusFolder =
        File("${appStorageRoot}/${if (isWhatsAppBusiness) whatsAppBusinessPath else whatsAppPath}")
      if (whatsAppStatusFolder.exists() && whatsAppStatusFolder.isDirectory) {
        return whatsAppStatusFolder.absolutePath
      }
    }

    return null
  }
}