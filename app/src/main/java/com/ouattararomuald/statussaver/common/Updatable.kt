package com.ouattararomuald.statussaver.common

import com.ouattararomuald.statussaver.Media

interface Updatable {
  fun onUpdateData(medias: List<Media>)
}