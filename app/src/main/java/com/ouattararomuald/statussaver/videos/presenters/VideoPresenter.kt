package com.ouattararomuald.statussaver.videos.presenters

import com.ouattararomuald.statussaver.Media

class VideoPresenter(
  private val medias: List<Media>,
  private val view: VideoContract.VideoView
) : VideoContract.VideoPresenter {

  override fun start() {
    view.displayMedias(medias)
  }
}