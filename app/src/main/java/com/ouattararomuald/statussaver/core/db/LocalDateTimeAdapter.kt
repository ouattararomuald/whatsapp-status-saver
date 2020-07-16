package com.ouattararomuald.statussaver.core.db

import com.squareup.sqldelight.ColumnAdapter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class LocalDateTimeAdapter : ColumnAdapter<LocalDateTime, String> {
  override fun decode(databaseValue: String): LocalDateTime =
    LocalDateTime.parse(databaseValue, DateTimeFormatter.ISO_LOCAL_DATE)

  override fun encode(value: LocalDateTime): String =
    value.format(DateTimeFormatter.ISO_LOCAL_DATE)
}