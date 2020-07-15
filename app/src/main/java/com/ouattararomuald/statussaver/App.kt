package com.ouattararomuald.statussaver

import android.app.Application
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen

class App: Application() {

  override fun onCreate() {
    super.onCreate()
    AndroidThreeTen.init(this)
    Stetho.initializeWithDefaults(this)
  }
}