package com.ouattararomuald.statussaver.home.presenters

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.home.models.Page

interface HomeContract {

  interface HomeView {
    fun getContext(): Context

    fun displayPages(pages: Array<Page>)

    fun navigateToActivity(intent: Intent)
  }

  interface HomePresenter {
    fun start()

    fun onClearOptionMenuItemClicked()

    fun onShareOptionMenuItemClicked()
  }

  interface HomeCommand {
    fun setCurrentView(fragment: Fragment)

    fun shareImages(medias: List<Media>)

    fun shareVideos(medias: List<Media>)
  }
}