package com.ouattararomuald.statussaver.core

import android.os.Environment
import com.ouattararomuald.statussaver.common.SAVED_MEDIA_DESTINATION_FOLDER_NAME
import com.ouattararomuald.statussaver.db.GetOldMedias
import kotlinx.coroutines.*
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

class FileHelper: CoroutineScope {
  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private val handler = CoroutineExceptionHandler { _, exception ->
  }

  fun deleteMedias(oldMedias: List<GetOldMedias>, onFinishBlock: suspend CoroutineScope.(mediaToDeleteId: String) -> Unit) {
    launch(coroutineContext + handler) {
      oldMedias.map { media ->
        val file = File(media.absolutePath)
        if (file.exists() && file.delete()) {
          onFinishBlock(media.id)
        }
      }
    }
  }

  fun writeFile(sourceFile: File, destinationFolder: File, onFinishBlock: suspend CoroutineScope.() -> Unit) {
    writeFile(FileOutputStream(destinationFolder), sourceFile)
    launch(coroutineContext + handler) {
      onFinishBlock()
    }
  }

  /**
   * Writes the given [inputFiles] into the given [outputStreams].
   *
   * @param outputStreams destination stream.
   * @param inputFiles source file.
   * @param onSuccess on success callback.
   */
  fun writeFiles(outputStreams: List<OutputStream>, inputFiles: List<File>, onSuccess: (numberOfSuccess: Int) -> Unit) = runBlocking {
    if (outputStreams.size != inputFiles.size) {
      return@runBlocking
    }
    val writtenFilesCounter = AtomicInteger(0)
    val jobs = mutableListOf<Deferred<Int>>()
    outputStreams.forEachIndexed { index, outputStream ->
      val inputFile = inputFiles[index]
      jobs.add(async(coroutineContext + handler) {
        outputStream.use {
          val sink = it.sink().buffer()
          sink.writeAll(inputFile.source())
          sink.close()
          writtenFilesCounter.getAndIncrement()
        }
      })
    }
    jobs.awaitAll()
    onSuccess(writtenFilesCounter.get())
  }

  /**
   * Writes the given [inputFile] into the given [outputStream].
   *
   * @param outputStream destination stream.
   * @param inputFile source file.
   * @param onSuccess on success callback.
   */
  fun writeFile(outputStream: OutputStream, inputFile: File, onSuccess: () -> Unit) {
    launch(coroutineContext + handler) {
      outputStream.use {
        val sink = it.sink().buffer()
        sink.writeAll(inputFile.source())
        sink.close()
        onSuccess()
      }
    }
  }

  /**
   * Writes the given [inputFile] into the given [outputStream].
   *
   * @param outputStream destination stream.
   * @param inputFile source file.
   */
  fun writeFile(outputStream: OutputStream, inputFile: File) {
    launch(coroutineContext + handler) {
      outputStream.use {
        val sink = it.sink().buffer()
        sink.writeAll(inputFile.source())
        sink.close()
      }
    }
  }

  fun writeFiles(filesToWrite: List<File>, onSuccess: (numberOfSuccess: Int) -> Unit) = runBlocking {
    val writtenFilesCounter = AtomicInteger(0)
    val jobs = mutableListOf<Deferred<Int>>()
    filesToWrite.forEach { fileToWrite ->
      jobs.add(async(coroutineContext + handler) {
        val fileName = fileToWrite.name

        val baseFolder = File(Environment.getExternalStorageDirectory(), SAVED_MEDIA_DESTINATION_FOLDER_NAME)
        if (!baseFolder.exists()) {
          baseFolder.mkdir()
        }

        val destinationFile = File(
          Environment.getExternalStorageDirectory(),
          "$SAVED_MEDIA_DESTINATION_FOLDER_NAME/${fileName}"
        )
        if (!destinationFile.exists()) {
          destinationFile.createNewFile()
        }

        val fileOutputStream = FileOutputStream(destinationFile)
        fileOutputStream.use {
          val sink = it.sink().buffer()
          sink.writeAll(fileToWrite.source())
          sink.close()
          writtenFilesCounter.getAndIncrement()
        }
      })
    }
    jobs.awaitAll()
    onSuccess(writtenFilesCounter.get())
  }

  fun writeFile(fileToWrite: File, onFinishBlock: suspend CoroutineScope.() -> Unit) {
    launch(coroutineContext + handler) {
      val fileName = fileToWrite.name

      val baseFolder = File(Environment.getExternalStorageDirectory(), SAVED_MEDIA_DESTINATION_FOLDER_NAME)
      if (!baseFolder.exists()) {
        baseFolder.mkdir()
      }

      val destinationFile = File(
        Environment.getExternalStorageDirectory(),
        "$SAVED_MEDIA_DESTINATION_FOLDER_NAME/${fileName}"
      )
      if (!destinationFile.exists()) {
        destinationFile.createNewFile()
      }

      val fileOutputStream = FileOutputStream(destinationFile)
      fileOutputStream.use {
        val sink = it.sink().buffer()
        sink.writeAll(fileToWrite.source())
        sink.close()
        onFinishBlock()
      }
    }
  }

  fun cancelTasks() {
    cancel()
  }
}