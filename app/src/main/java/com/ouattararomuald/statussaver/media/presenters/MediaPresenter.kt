package com.ouattararomuald.statussaver.media.presenters

import android.content.Context
import com.ouattararomuald.statussaver.core.db.DbMediaDAO
import com.ouattararomuald.statussaver.core.db.MediaDAO

class MediaPresenter(
  context: Context,
  private val view: MediaContract.MediaView
) : MediaContract.MediaPresenter {

  private val mediaDAO: MediaDAO = DbMediaDAO(context)

  override fun start() {
    view.displayMedias(mediaDAO.getImages(), mediaDAO.getVideos())
  }

  override fun refresh() {
    view.displayMedias(mediaDAO.getImages(), mediaDAO.getVideos())
  }
}