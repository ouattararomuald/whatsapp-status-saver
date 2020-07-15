package com.ouattararomuald.statussaver.core.db

import com.ouattararomuald.statussaver.MediaType
import com.squareup.sqldelight.ColumnAdapter

class MediaTypeAdapter : ColumnAdapter<MediaType, String> {
  override fun decode(databaseValue: String): MediaType = MediaType.valueOf(databaseValue)

  override fun encode(value: MediaType): String = value.name
}