package com.ouattararomuald.statussaver.images.presenters

import com.ouattararomuald.statussaver.Media

class ImagePresenter(
  private val medias: List<Media>,
  private val view: ImageContract.ImageView
) : ImageContract.ImagePresenter {

  override fun start() {
    view.displayMedias(medias)
  }
}