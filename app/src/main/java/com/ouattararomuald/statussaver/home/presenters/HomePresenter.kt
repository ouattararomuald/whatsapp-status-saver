package com.ouattararomuald.statussaver.home.presenters

import android.content.Context
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.home.models.Page
import com.ouattararomuald.statussaver.images.ui.ImageFragment
import com.ouattararomuald.statussaver.videos.ui.VideoFragment

class HomePresenter(
  private val context: Context,
  private val view: HomeContract.HomeView
) : HomeContract.HomePresenter {

  private lateinit var pages: Array<Page>

  override fun start() {
    pages = arrayOf(
        Page(title = context.getString(R.string.images_fragment_title), fragment = ImageFragment.newInstance()),
        Page(title = context.getString(R.string.videos_fragment_title), fragment = VideoFragment.newInstance())
    )
    view.displayPages(pages)
  }
}