package com.ouattararomuald.statussaver

import android.os.FileObserver
import java.io.File
import java.util.LinkedHashMap
import java.util.Stack

/**
 * A [FileObserver] that observes all the files/folders within given directory recursively.
 * It automatically starts/stops monitoring new folders/files created after starting the watch.
 */
class RecursiveFileObserver : FileObserver {

  private val observers: MutableMap<String, FileObserver> = LinkedHashMap()

  private val listener: EventListener
  private var mask: Int = CREATE
  private val path: String

  constructor(path: String, listener: EventListener) : this(path, ALL_EVENTS, listener)

  constructor(path: String, mask: Int, listener: EventListener) : super(path, mask) {
    this.listener = listener
    this.mask = mask or CREATE or DELETE_SELF
    this.path = path
  }

  private fun startWatching(path: String) {
    synchronized(observers) {
      var observer: FileObserver? = observers.remove(path)
      observer?.stopWatching()
      observer = SingleFileObserver(path, mask)
      observer.startWatching()
      observers.put(path, observer)
    }
  }

  override fun startWatching() {
    val stack = Stack<String>()
    stack.push(path)

    // Recursively watch all child directories
    while (!stack.empty()) {
      val parent = stack.pop()
      startWatching(parent)
      val path = File(parent)
      val files = path.listFiles()
      files?.forEach {
        if (watch(it)) {
          stack.push(it.absolutePath)
        }
      }
    }
  }

  private fun watch(file: File): Boolean {
    return file.isDirectory && file.name != "." && file.name != ".."
  }

  private fun stopWatching(path: String) {
    synchronized(observers) {
      val observer = observers.remove(path)
      observer?.stopWatching()
    }
  }

  override fun stopWatching() {
    synchronized(observers) {
      observers.values.forEach { it.stopWatching() }
      observers.clear()
    }
  }

  override fun onEvent(event: Int, path: String?) {
    val file = if (path == null) {
      File(this.path)
    } else {
      File(this.path, path)
    }
    notify(event, file)
  }

  private fun notify(event: Int, file: File) {
    if (this.listener != null) {
      this.listener.onEvent(event and ALL_EVENTS, file)
    }
  }

  interface EventListener {
    fun onEvent(event: Int, file: File)
  }

  private inner class SingleFileObserver(private val filePath: String, mask: Int) :
      FileObserver(filePath, mask) {
    override fun onEvent(event: Int, path: String?) {
      val file: File = if (path == null) {
        File(filePath)
      } else {
        File(filePath, path)
      }
      when (event and ALL_EVENTS) {
        DELETE_SELF -> stopWatching(filePath)
        CREATE -> if (watch(file)) {
          startWatching(file.absolutePath)
        }
      }
      notify(event, file)
    }

  }
}