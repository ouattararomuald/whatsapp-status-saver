package com.ouattararomuald.statussaver.images.presenters

import com.ouattararomuald.statussaver.Media

interface ImageContract {

  interface ImageView {

    fun displayMedias(medias: List<Media>)
  }

  interface ImagePresenter {
    fun start()
  }
}