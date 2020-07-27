package com.ouattararomuald.statussaver.home.presenters

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.ouattararomuald.statussaver.BuildConfig
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.Updatable
import com.ouattararomuald.statussaver.common.UpdatableOldMedia
import com.ouattararomuald.statussaver.core.db.DbMediaDAO
import com.ouattararomuald.statussaver.core.db.MediaDAO
import com.ouattararomuald.statussaver.home.models.Page
import com.ouattararomuald.statussaver.images.ui.ImageFragment
import com.ouattararomuald.statussaver.media.ui.OldMediaFragment
import com.ouattararomuald.statussaver.statuses.StatusFinder
import com.ouattararomuald.statussaver.statuses.StatusesSnapshot
import com.ouattararomuald.statussaver.videos.ui.VideoFragment

class HomePresenter(
  private val context: Context,
  private val view: HomeContract.HomeView
) : HomeContract.HomePresenter, HomeContract.HomeCommand {

  private lateinit var pages: Array<Page>
  private val statusFinder = StatusFinder(context)
  private var statusesSnapshot: StatusesSnapshot? = null

  private var currentFragment: Fragment? = null

  private val mediaDAO: MediaDAO = DbMediaDAO(context)

  private fun initializedPages() {
    pages = arrayOf(
      Page(
        title = context.getString(R.string.images_fragment_title),
        fragment = ImageFragment.newInstance(statusesSnapshot?.images ?: emptyList())
      ),
      Page(
        title = context.getString(R.string.videos_fragment_title),
        fragment = VideoFragment.newInstance(statusesSnapshot?.videos ?: emptyList())
      ),
      Page(
        title = context.getString(R.string.old_medias_fragment_title),
        fragment = OldMediaFragment.newInstance()
      )
    )

    pages.forEach { page ->
      when (page.fragment) {
        is ImageFragment -> {
          page.fragment.homeCommand = this
        }
        is VideoFragment -> {
          page.fragment.homeCommand = this
        }
        is OldMediaFragment -> {
          page.fragment.homeCommand = this
        }
      }
    }
  }

  override fun discoverStatuses() {
    statusFinder.findStatuses()
    statusesSnapshot = statusFinder.getSnapshot()

    saveStatuses()

    initializedPages()

    view.displayPages(pages)
  }

  override fun refreshData() {
    statusFinder.findStatuses()
    statusesSnapshot = statusFinder.getSnapshot()

    saveStatuses()

    if (!::pages.isInitialized) {
      return
    }

    pages.forEach { page ->
      val medias = when (page.fragment) {
        is ImageFragment -> {
          statusesSnapshot?.images ?: emptyList()
        }
        is VideoFragment -> {
          statusesSnapshot?.videos ?: emptyList()
        }
        else -> {
          emptyList()
        }
      }

      if (page.fragment is Updatable) {
        page.fragment.onUpdateData(medias)
      }
      if (page.fragment is UpdatableOldMedia) {
        page.fragment.onUpdateData(mediaDAO.getImages(), mediaDAO.getVideos())
      }
    }
  }

  private fun saveStatuses() {
    statusesSnapshot?.let {
      mediaDAO.saveMedias(it.images)
      mediaDAO.saveMedias(it.videos)
    }
  }

  override fun onClearOptionMenuItemClicked() {
    if (!::pages.isInitialized) {
      return
    }
    pages.forEach { page ->
      if (currentFragment == page.fragment) {
        page.onClearSelection()
      }
    }
  }

  override fun onShareOptionMenuItemClicked() {
    if (!::pages.isInitialized) {
      return
    }
    pages.forEach { page ->
      if (currentFragment == page.fragment) {
        page.onShareClicked()
      }
    }
  }

  override fun setCurrentView(fragment: Fragment) {
    this.currentFragment = fragment
  }

  override fun onMediaSelected() {
    view.showClearOptionMenu()
  }

  override fun onSelectionCleared() {
    view.hideClearOptionMenu()
  }

  override fun shareImages(medias: List<Media>) {
    if (medias.isEmpty()) {
      view.openChooserForIntent(getShareAppIntent())
    } else {
      view.openChooserForIntent(getMediasShareIntent(medias, IMAGE_MIME_TYPE))
    }
  }

  override fun shareVideos(medias: List<Media>) {
    if (medias.isEmpty()) {
      view.openChooserForIntent(getShareAppIntent())
    } else {
      //FIXME: Should be VIDEO_MIME_TYPE but it's doesn't allow to share with all apps
      view.openChooserForIntent(getMediasShareIntent(medias, IMAGE_MIME_TYPE))
    }
  }

  private fun getShareAppIntent(): Intent {
    val sendIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.share_app_title))
      putExtra(
        Intent.EXTRA_TEXT,
        context.resources.getString(
          R.string.share_app_message,
          "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        )
      )
      type = "text/plain"
    }

    return Intent.createChooser(sendIntent, context.resources.getString(R.string.share_app_title))
  }

  private fun getMediasShareIntent(medias: List<Media>, mimeType: String): Intent {
    val files = mutableListOf<Uri>()
    val shareIntent: Intent = Intent().apply {
      action = if (medias.size > 1) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND
      putExtra(Intent.EXTRA_SUBJECT, context.resources.getText(R.string.send_title))
      type = mimeType

      medias.forEach { media ->
        val authority = "${context.applicationContext.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(
          context,
          authority,
          media.file
        )
        files.add(uri)
      }

      if (mimeType == VIDEO_MIME_TYPE) {
        addCategory(Intent.CATEGORY_OPENABLE)
      }
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      if (medias.size > 1) {
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(files))
      } else {
        putExtra(Intent.EXTRA_STREAM, files.first())
      }
    }

    return shareIntent
  }

  companion object {
    private const val IMAGE_MIME_TYPE = "image/*"
    private const val VIDEO_MIME_TYPE = "video/*"
  }
}