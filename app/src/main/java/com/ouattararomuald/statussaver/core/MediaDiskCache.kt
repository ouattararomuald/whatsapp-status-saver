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
    private const val FILE_CACHE_PREFIX_NAME = "ci.ogr."
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

  fun add(key: String, file: File) {
    fileHelper.writeFile(file, getCacheDir())
  }

  fun getCacheDir(): File = context.cacheDir

  fun getCacheSnapshot(): StatusesSnapshot {
    if (snapshot == null) {
      snapshot = StatusesSnapshot(images, videos)
    }
    return StatusesSnapshot(images, videos)
  }
}