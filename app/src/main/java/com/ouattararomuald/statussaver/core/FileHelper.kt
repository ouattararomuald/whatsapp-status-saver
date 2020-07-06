package com.ouattararomuald.statussaver.core

import android.os.Environment
import com.ouattararomuald.statussaver.common.SAVED_MEDIA_DESTINATION_FOLDER_NAME
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.coroutines.CoroutineContext

class FileHelper : CoroutineScope {
  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private val handler = CoroutineExceptionHandler { _, exception ->
  }

  /**
   * Writes the given [inputFile] into the given [outputStream].
   *
   * @param outputStream destination stream.
   * @param inputFile source file.
   */
  fun writeFile(outputStream: OutputStream, inputFile: File) {
    launch(handler) {
      outputStream.use {
        val sink = it.sink().buffer()
        sink.writeAll(inputFile.source())
        sink.close()
      }
    }
  }

  fun writeFile(fileToWrite: File, onFinishBlock: suspend CoroutineScope.() -> Unit) {
    launch(handler) {
      val fileName = fileToWrite.name
      val destinationFile = File(
        Environment.getExternalStorageDirectory(),
        "$SAVED_MEDIA_DESTINATION_FOLDER_NAME/${fileName}"
      )

      val fileOutputStream = FileOutputStream(destinationFile)
      try {
        fileOutputStream.write(android.R.attr.data)
        onFinishBlock()
      } catch (e: IOException) {
      } finally {
        fileOutputStream.close()
      }
    }
  }

  fun cancelTasks() {
    cancel()
  }
}