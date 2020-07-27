package com.ouattararomuald.statussaver.common

import com.ouattararomuald.statussaver.Media

interface UpdatableOldMedia {
  fun onUpdateData(images: List<Media>, videos: List<Media>)
}