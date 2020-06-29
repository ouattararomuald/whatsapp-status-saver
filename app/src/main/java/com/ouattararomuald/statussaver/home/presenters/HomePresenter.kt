package com.ouattararomuald.statussaver.home.presenters

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.home.models.Page
import com.ouattararomuald.statussaver.images.ui.ImageFragment
import com.ouattararomuald.statussaver.statuses.StatusFinder
import com.ouattararomuald.statussaver.statuses.StatusesSnapshot
import com.ouattararomuald.statussaver.videos.ui.VideoFragment

class HomePresenter(
  private val context: Context,
  private val view: HomeContract.HomeView
) : HomeContract.HomePresenter, HomeContract.HomeCommand {

  private lateinit var pages: Array<Page>
  private val statusFinder = StatusFinder(context)
  private lateinit var statusesSnapshot: StatusesSnapshot

  private var currentFragment: Fragment? = null

  override fun start() {
    statusFinder.findStatuses()
    statusesSnapshot = statusFinder.getSnapshot()
    pages = arrayOf(
        Page(title = context.getString(R.string.images_fragment_title),
            fragment = ImageFragment.newInstance(statusesSnapshot.images)),
        Page(title = context.getString(R.string.videos_fragment_title),
            fragment = VideoFragment.newInstance(statusesSnapshot.videos))
    )
    pages.forEach { page ->
      if (page.fragment is ImageFragment) {
        page.fragment.homeCommand = this
      } else if (page.fragment is VideoFragment) {
        page.fragment.homeCommand = this
      }
    }
    view.displayPages(pages)
  }

  override fun onClearOptionMenuItemClicked() {
    pages.forEach { page ->
      if (currentFragment == page.fragment) {
        page.onClearSelection()
      }
    }
  }

  override fun onShareOptionMenuItemClicked() {
    pages.forEach { page ->
      if (currentFragment == page.fragment) {
        page.onShareClicked()
      }
    }
  }

  override fun setCurrentView(fragment: Fragment) {
    this.currentFragment = fragment
  }

  override fun shareImages(medias: List<Media>) {
    if (medias.isEmpty()) {

    } else {
      view.navigateToActivity(getMediasShareIntent(medias, IMAGE_MIME_TYPE))
    }
  }

  override fun shareVideos(medias: List<Media>) {
    if (medias.isEmpty()) {

    } else {
      view.navigateToActivity(getMediasShareIntent(medias, VIDEO_MIME_TYPE))
    }
  }

  private fun getMediasShareIntent(medias: List<Media>, mimeType: String): Intent {
    val files = mutableListOf<Uri>()
    val shareIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND_MULTIPLE
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
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(files))
    }
    return Intent.createChooser(shareIntent, context.resources.getText(R.string.send_to))
  }

  companion object {
    private const val IMAGE_MIME_TYPE = "image/*"
    private const val VIDEO_MIME_TYPE = "video/*"
  }
}