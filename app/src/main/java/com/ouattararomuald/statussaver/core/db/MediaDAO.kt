package com.ouattararomuald.statussaver.core.db

import com.ouattararomuald.statussaver.Media

interface MediaDAO {
  fun isCacheEmpty(): Boolean

  fun getImages(): List<Media>

  fun getVideos(): List<Media>

  fun saveMedias(medias: List<Media>)
}
