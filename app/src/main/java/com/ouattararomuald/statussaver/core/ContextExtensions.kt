package com.ouattararomuald.statussaver.core

import android.content.Context
import com.ouattararomuald.statussaver.App
import com.ouattararomuald.statussaver.core.db.DatabaseProvider

fun Context.databaseProvider(): DatabaseProvider = (applicationContext as App).getDatabaseProvider()