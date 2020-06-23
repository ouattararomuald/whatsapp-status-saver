package com.ouattararomuald.statussaver.home.presenters

import android.content.Context
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
) : HomeContract.HomePresenter {

  private lateinit var pages: Array<Page>
  private val statusFinder = StatusFinder(context)
  private lateinit var statusesSnapshot: StatusesSnapshot

  override fun start() {
    statusFinder.findStatuses()
    statusesSnapshot = statusFinder.getSnapshot()
    pages = arrayOf(
        Page(title = context.getString(R.string.images_fragment_title), fragment = ImageFragment.newInstance(statusesSnapshot.images)),
        Page(title = context.getString(R.string.videos_fragment_title), fragment = VideoFragment.newInstance(statusesSnapshot.videos))
    )
    view.displayPages(pages)
  }
}