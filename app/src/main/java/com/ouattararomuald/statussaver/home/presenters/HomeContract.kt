package com.ouattararomuald.statussaver.home.presenters

import android.content.Context
import com.ouattararomuald.statussaver.home.models.Page

interface HomeContract {

  interface HomeView {
    fun getContext(): Context

    fun displayPages(pages: Array<Page>)
  }

  interface HomePresenter {
    fun start()
  }
}