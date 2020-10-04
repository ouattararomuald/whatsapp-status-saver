package com.ouattararomuald.statussaver.core.db

import android.content.Context
import android.content.SharedPreferences
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.core.MediaDiskCache
import com.ouattararomuald.statussaver.core.databaseProvider
import com.ouattararomuald.statussaver.db.GetOldMedias
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import kotlin.coroutines.CoroutineContext

class DbMediaDAO(context: Context) : MediaDAO, CoroutineScope {

  companion object {
    private const val STATUSES_MAX_CONSERVATION_DAYS = 3L
  }

  private val databaseProvider = context.databaseProvider()
  private val mediaQueries = databaseProvider.mediaQueries

  private val mediaDiskCache = MediaDiskCache(context)

  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private val handler = CoroutineExceptionHandler { _, exception ->
  }

  private val sharedPrefs: SharedPreferences by lazy {
    context.getSharedPreferences(
      context.getString(R.string.shared_preferences_key),
      Context.MODE_PRIVATE
    )
  }

  private val lastLaunchDate: LocalDateTime = LocalDateTime.parse(
    sharedPrefs.getString(
      context.getString(R.string.last_launch_date_key),
      LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
    ),
    DateTimeFormatter.ISO_DATE_TIME
  )

  init {
    if (shouldDeleteOldMedia()) {
      deleteOldMedias()
    }
  }

  private fun shouldDeleteOldMedia(): Boolean {
    val threeDaysBeforeDate = getTodayDate().minusDays(STATUSES_MAX_CONSERVATION_DAYS)
    return !lastLaunchDate.isBefore(threeDaysBeforeDate)
  }

  private fun deleteOldMedias() {
    val oldestData = getOldestDate()
    val oldMedias: List<GetOldMedias> = mediaQueries.getOldMedias(oldestData).executeAsList()
    mediaDiskCache.deleteOldMedias(oldMedias) { mediaToDeleteId ->
      launch(coroutineContext + handler) {
        mediaQueries.deleteMediaById(mediaToDeleteId)
      }
    }
  }

  override fun isCacheEmpty(): Boolean = getNumberOfMediasInCache() <= 0

  private fun getNumberOfMediasInCache(): Long {
    return mediaQueries.countOldMedias(getOldestDate(), getYesterdayDate()).executeAsOne()
  }

  override fun getImages(): List<Media> {
    return mediaQueries.getAudios(getOldestDate(), getYesterdayDate()).executeAsList()
      .map { Media(File(it.absolutePath), it.mediaType) }
  }

  override fun getVideos(): List<Media> {
    return mediaQueries.getVideos(getOldestDate(), getYesterdayDate()).executeAsList()
      .map { Media(File(it.absolutePath), it.mediaType) }
  }

  private fun getOldestDate(): LocalDateTime  {
    val threeDaysBeforeDate = getTodayDate().minusDays(STATUSES_MAX_CONSERVATION_DAYS)
    return if (lastLaunchDate.isBefore(threeDaysBeforeDate)) {
      lastLaunchDate.minusDays(STATUSES_MAX_CONSERVATION_DAYS)
    } else {
      threeDaysBeforeDate
    }
  }

  private fun getYesterdayDate(): LocalDateTime = getTodayDate().minusDays(1)

  private fun getTodayDate(): LocalDateTime = LocalDateTime.now()

  override fun saveMedias(medias: List<Media>) {
    launch(coroutineContext + handler) {
      medias.forEach { media ->
        val mediaId = "${MediaDiskCache.FILE_CACHE_PREFIX_NAME}.${media.file.name}"
        if (!mediaExists(mediaId)) {
          mediaDiskCache.add(mediaId, media) { savedFilePath ->
            mediaQueries.insertMedia(
              mediaId,
              savedFilePath,
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