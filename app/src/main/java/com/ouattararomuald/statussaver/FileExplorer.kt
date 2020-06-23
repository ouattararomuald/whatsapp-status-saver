package com.ouattararomuald.statussaver

import android.webkit.MimeTypeMap
import java.io.File

object FileExplorer {

  private const val IMAGE = "image"
  private const val VIDEO = "video"

  /**
   * Returns all the files and directories of the given [file] and all its eventual sub folders.
   *
   * @param file root [File].
   */
  tailrec fun getMediasFromFile(file: File): List<Media> {
    val medias: MutableList<Media> = mutableListOf()
    if (file.isDirectory) {
      val files = file.listFiles()
      if (files != null) {
        for (i in files.indices) {
          val currentFile = files[i]
          if (currentFile.isDirectory) {
            return getMediasFromFile(currentFile)
          }
          medias.addFileIfSupported(currentFile)
        }
      }
    } else {
      medias.addFileIfSupported(file)
    }

    return medias
  }

  private fun MutableList<Media>.addFileIfSupported(file: File) {
    val mediaType = file.getMediaType()
    if (mediaType != MediaType.UNKNOWN) {
      this.add(Media(file, mediaType))
    }
  }

  private fun File.getMediaType(): MediaType {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(absolutePath)
    if (extension != null) {
      type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    if (type == null) {
      return MediaType.UNKNOWN
    }

    return when {
      type.contains(IMAGE) -> MediaType.IMAGE
      type.contains(VIDEO) -> MediaType.VIDEO
      else -> MediaType.UNKNOWN
    }
  }
}