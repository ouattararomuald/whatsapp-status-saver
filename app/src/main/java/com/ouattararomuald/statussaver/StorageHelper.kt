package com.ouattararomuald.statussaver

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

class StorageHelper(private val context: Context) {

  companion object {
    const val WHATSAPP_STATUSES_FOLDER_PATH = "WhatsApp/Media/.Statuses"
    const val WHATSAPP_BUSINESS_STATUSES_FOLDER_PATH = "WhatsApp Business/Media/.Statuses"

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

    val appStorageRoot = Environment.getExternalStorageDirectory()
    if (appStorageRoot != null) {
      val whatsAppStatusFolder = File("${appStorageRoot}/${if (isWhatsAppBusiness) WHATSAPP_BUSINESS_STATUSES_FOLDER_PATH else WHATSAPP_STATUSES_FOLDER_PATH}")
      if (whatsAppStatusFolder.exists() && whatsAppStatusFolder.isDirectory) {
        return whatsAppStatusFolder.absolutePath
      }
    }

    return null
  }
}