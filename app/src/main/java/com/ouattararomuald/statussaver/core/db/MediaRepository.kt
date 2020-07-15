package com.ouattararomuald.statussaver.core.db

import android.content.Context
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.core.MediaDiskCache
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.io.File
import kotlin.coroutines.CoroutineContext

class MediaRepository(private val context: Context): CoroutineScope {
  private val databaseProvider = DatabaseProvider(context)
  private val mediaQueries = databaseProvider.mediaQueries

  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private val handler = CoroutineExceptionHandler { _, exception ->
  }

  fun getAudios(): List<Media> {
    return mediaQueries.getAudios().executeAsList()
      .map { Media(File(it.absolutePath), it.mediaType) }
  }

  fun getVideos(): List<Media> {
    return mediaQueries.getVideos().executeAsList()
      .map { Media(File(it.absolutePath), it.mediaType) }
  }

  fun saveMedias(medias: List<Media>) {
    launch(coroutineContext + handler) {
      medias.forEach { media ->
        val mediaId = "${MediaDiskCache.FILE_CACHE_PREFIX_NAME}.${media.file.name}"
        if (!mediaExists(mediaId)) {
          mediaQueries.insertMedia(
            mediaId,
            media.file.absolutePath,
            media.mediaType,
            LocalDateTime.ofInstant(
              Instant.ofEpochMilli(media.file.lastModified()),
              ZoneId.systemDefault()
            ),
            LocalDateTime.now()
          )
        }
      }
    }
  }

  private fun mediaExists(mediaId: String): Boolean =
    mediaQueries.countMediaById(mediaId).executeAsOne() > 0
}