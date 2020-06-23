package com.ouattararomuald.statussaver.videos.presenters

import com.ouattararomuald.statussaver.Media

interface VideoContract {

  interface VideoView {

    fun displayMedias(medias: List<Media>)
  }

  interface VideoPresenter {
    fun start()
  }
}