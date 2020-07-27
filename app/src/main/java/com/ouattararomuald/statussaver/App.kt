package com.ouattararomuald.statussaver

import android.app.Application
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import com.ouattararomuald.statussaver.core.db.DatabaseProvider

class App: Application() {

  private lateinit var databaseProvider: DatabaseProvider

  override fun onCreate() {
    super.onCreate()
    AndroidThreeTen.init(this)
    Stetho.initializeWithDefaults(this)
    databaseProvider = DatabaseProvider(this)
  }

  fun getDatabaseProvider(): DatabaseProvider = databaseProvider
}