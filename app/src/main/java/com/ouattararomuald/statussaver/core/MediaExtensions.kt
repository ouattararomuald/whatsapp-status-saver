package com.ouattararomuald.statussaver.core

import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.MediaType

fun Media.mimeType(): String {
  return when (this.mediaType) {
    MediaType.IMAGE -> {
      "image/${file.extension}"
    }
    MediaType.VIDEO -> {
      "video/${file.extension}"
    }
    else -> {
      "document/${file.extension}"
    }
  }
}