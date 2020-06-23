package com.ouattararomuald.statussaver

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class StatusCheckerWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : Worker(appContext, workerParams), RecursiveFileObserver.EventListener {

  private lateinit var whatsAppStatusFileObserver: RecursiveFileObserver

  override fun doWork(): Result {
    val externalStorageRoot = applicationContext.getExternalFilesDir(null)
    externalStorageRoot?.let {
      val whatStatusFolder = File( "${it.path}/WhatsApp/Media/.Statuses")
    }

    observeWhatsAppStatusFolder()
    return Result.success()
  }

  private fun observeWhatsAppStatusFolder() {

    whatsAppStatusFileObserver = RecursiveFileObserver("", this)
  }

  override fun onEvent(event: Int, file: File) {
    TODO("Not yet implemented")
  }
}