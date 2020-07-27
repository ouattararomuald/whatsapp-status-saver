package com.ouattararomuald.statussaver.core.db

import android.content.Context
import com.ouattararomuald.statussaver.BuildConfig
import com.ouattararomuald.statussaver.Database
import com.ouattararomuald.statussaver.db.Medias
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

class DatabaseProvider(context: Context) {

  private val localDateTimeAdapter = LocalDateTimeAdapter()
  private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, BuildConfig.DB_NAME)

  private val queryWrapper: Database = Database(
    driver = driver,
    mediasAdapter = Medias.Adapter(
      mediaTypeAdapter = MediaTypeAdapter(),
      publishDateAdapter = localDateTimeAdapter,
      saveDateAdapter = localDateTimeAdapter
    )
  )

  val mediaQueries = queryWrapper.mediaQueries
}