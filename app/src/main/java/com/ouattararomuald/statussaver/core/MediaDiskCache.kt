package com.ouattararomuald.statussaver.core

import android.content.Context
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.statuses.StatusesSnapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import kotlin.coroutines.CoroutineContext

class MediaDiskCache(private val context: Context): CoroutineScope {

  companion object {
    const val FILE_CACHE_PREFIX_NAME = "ci.ogr"
  }

  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private val handler = CoroutineExceptionHandler { _, exception ->
  }

  private val fileHelper = FileHelper()
  private var images = mutableListOf<Media>()
  private var videos = mutableListOf<Media>()
  private var snapshot: StatusesSnapshot? = null

  fun add(key: String, media: Media, nextAsyncTaskBlock: () -> Unit) {
    val cacheDir = getCacheDir()
    if (cacheDir != null) {
      val destinationFile = File(cacheDir, key)
      fileHelper.writeFile(media.file, destinationFile) {
        nextAsyncTaskBlock()
      }
    }
  }

  private fun getCacheDir(): File? = context.externalCacheDir

  fun getCacheSnapshot(): StatusesSnapshot {
    if (snapshot == null) {
      snapshot = StatusesSnapshot(images, videos)
    }
    return StatusesSnapshot(images, videos)
  }
}