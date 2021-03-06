package com.ouattararomuald.statussaver.statuses

import android.content.Context
import com.ouattararomuald.statussaver.FileExplorer
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.MediaType
import com.ouattararomuald.statussaver.StorageHelper
import java.io.File

/** Helper to find all WhatsApp statuses that have been published. */
class StatusFinder(private val context: Context) {

  private var images = mutableListOf<Media>()
  private var videos = mutableListOf<Media>()

  private var state: FinderState = FinderState.UNEXPLORED

  private var snapshot: StatusesSnapshot? = null

  /** Explores WhatsApp folders to find images and videos that were published. */
  fun findStatuses() {
    reset()
    val statusesSnapshot = findStatuses(isWhatsAppBusiness = false)
    val businessStatusesSnapshot = findStatuses(isWhatsAppBusiness = true)
    images.addAll(sortMedia(statusesSnapshot.images + businessStatusesSnapshot.images))
    videos.addAll(sortMedia(statusesSnapshot.videos + businessStatusesSnapshot.videos))
  }

  private fun findStatuses(isWhatsAppBusiness: Boolean = false): StatusesSnapshot {
    val whatsAppStatusesFolderPath =
      if (isWhatsAppBusiness) StorageHelper(context).getWhatsAppBusinessStatusesFolderPath() else StorageHelper(
        context
      ).getWhatsAppStatusesFolderPath()
    val images = mutableListOf<Media>()
    val videos = mutableListOf<Media>()

    if (whatsAppStatusesFolderPath != null) {
      val medias = FileExplorer.getMediasFromFile(File(whatsAppStatusesFolderPath))
      val mediasGroups = medias.groupBy { media -> media.mediaType }
      if (mediasGroups.containsKey(MediaType.IMAGE)) {
        images.addAll(mediasGroups[MediaType.IMAGE] ?: error("Key missing in the map"))
      }
      if (mediasGroups.containsKey(MediaType.VIDEO)) {
        videos.addAll(mediasGroups[MediaType.VIDEO] ?: error("Key missing in the map"))
      }
    }

    return StatusesSnapshot(images, videos)
  }

  /**
   * Gets a snapshot of the statuses found in the last search.
   *
   * This method will automatically call [findStatuses] before returning if and only if it was not
   * called before calling this method.
   *
   * Once called, this method will always returns the same result unless you explicitly starts
   * a new exploration by calling [findStatuses].
   */
  fun getSnapshot(): StatusesSnapshot {
    if (snapshot == null) {
      snapshot = StatusesSnapshot(getImages(), getVideos())
    }
    return snapshot!!
  }

  /**
   * Returns the images found in the last exploration.
   *
   * This method will automatically call [findStatuses] before returning if and only if it was never
   * called before.
   *
   * @see getVideos
   * @see findStatuses
   */
  private fun getImages(): List<Media> {
    if (state != FinderState.EXPLORED) {
      findStatuses()
    }
    return images
  }

  /**
   * Returns the videos found in the last exploration.
   *
   * This method will automatically call [findStatuses] before returning if and only if it was never
   * called before.
   *
   * @see getImages
   * @see findStatuses
   */
  private fun getVideos(): List<Media> {
    if (state != FinderState.EXPLORED) {
      findStatuses()
    }
    return videos
  }

  private fun reset() {
    state = FinderState.UNEXPLORED
    snapshot = null
    images.clear()
    videos.clear()
  }

  private fun sortMedia(medias: List<Media>): List<Media> {
    return medias.sortedByDescending { media -> media.file.lastModified() }
  }

  private enum class FinderState {
    UNEXPLORED,
    EXPLORED,
  }
}