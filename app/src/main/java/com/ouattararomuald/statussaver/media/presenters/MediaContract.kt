package com.ouattararomuald.statussaver.media.presenters

import com.ouattararomuald.statussaver.Media

interface MediaContract {

  interface MediaView {

    fun displayMedias(images: List<Media>, videos: List<Media>)
  }

  interface MediaPresenter {
    fun start()
  }
}