package com.ouattararomuald.statussaver.media.presenters

import com.ouattararomuald.statussaver.Media

class MediaPresenter(
  private val images: List<Media>,
  private val videos: List<Media>,
  private val view: MediaContract.MediaView
) : MediaContract.MediaPresenter {

  override fun start() {
    view.displayMedias(images, videos)
  }
}