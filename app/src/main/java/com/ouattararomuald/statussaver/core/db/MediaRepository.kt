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

class MediaRepository(context: Context) : CoroutineScope {

  companion object {
    private const val STATUSES_MAX_CONSERVATION_DAYS = 3L
  }

  private val databaseProvider = DatabaseProvider(context)
  private val mediaQueries = databaseProvider.mediaQueries

  private val mediaDiskCache = MediaDiskCache(context)

  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private val handler = CoroutineExceptionHandler { _, exception ->
  }

  fun isCacheEmpty(): Boolean = getNumberOfMediasInCache() <= 0

  private fun getNumberOfMediasInCache(): Long {
    return mediaQueries.countOldMedias(getOldestDate(), getToday()).executeAsOne()
  }

  fun getAudios(): List<Media> {
    return mediaQueries.getAudios(getOldestDate(), getToday()).executeAsList()
      .map { Media(File(it.absolutePath), it.mediaType) }
  }

  fun getVideos(): List<Media> {
    return mediaQueries.getVideos(getOldestDate(), getToday()).executeAsList()
      .map { Media(File(it.absolutePath), it.mediaType) }
  }

  private fun getOldestDate(): LocalDateTime = getToday().minusDays(STATUSES_MAX_CONSERVATION_DAYS)

  private fun getToday(): LocalDateTime = LocalDateTime.now()

  fun saveMedias(medias: List<Media>) {
    launch(coroutineContext + handler) {
      medias.forEach { media ->
        val mediaId = "${MediaDiskCache.FILE_CACHE_PREFIX_NAME}.${media.file.name}"
        if (!mediaExists(mediaId)) {
          mediaDiskCache.add(mediaId, media) {
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
  }

  private fun mediaExists(mediaId: String): Boolean =
    mediaQueries.countMediaById(mediaId).executeAsOne() > 0
}