package com.ouattararomuald.statussaver.common.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.IMAGE_MIME_TYPE
import com.ouattararomuald.statussaver.core.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

abstract class MediaViewerActivity : AppCompatActivity() {

  companion object {
    private const val RQ_CREATE_FILE = 0xace
  }

  private var fileHelper = FileHelper()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableFullScreen()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RQ_CREATE_FILE) {
      if (resultCode == Activity.RESULT_OK) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val uri = data?.data
          if (uri != null) {
            val stream = contentResolver.openOutputStream(uri)
            stream?.let {
              fileHelper.writeFile(it, getMedias()[getMediaToWriteIndex()].file)
            }
            displaySuccessMessage()
          }
        }
      }
    }
  }

  abstract fun getMedias(): List<Media>

  abstract fun getMediaToWriteIndex(): Int

  abstract fun getMediaMimeType(): String

  abstract fun getRootView(): View

  private fun enableFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.setDecorFitsSystemWindows(false)
    } else {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
    }
  }

  /**
   * Saves the given [file] into shared storage.
   *
   * @param file the file to save.
   */
  protected fun saveFile(file: File) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createFile(file)
    } else {
      fileHelper.writeFile(file) {
        withContext(Dispatchers.Main) {
          displaySuccessMessage()
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createFile(file: File) {
    val fileName = file.name
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = IMAGE_MIME_TYPE
      putExtra(Intent.EXTRA_TITLE, fileName)
    }
    startActivityForResult(intent, RQ_CREATE_FILE)
  }

  private fun displaySuccessMessage() {
    Snackbar.make(
      getRootView(),
      "The file has been successfully saved.",
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color, theme)).show()
  }

  private fun displayFailureMessage() {
    Snackbar.make(
      getRootView(),
      "Oups! Something went wrong while saving the file.",
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color_failure, theme)).show()
  }

  protected fun getShareMediaIntent(media: Media): Intent {
    val shareIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.send_title))
      type = getMediaMimeType()

      val authority = "${applicationContext.packageName}.fileprovider"
      val fileUri = FileProvider.getUriForFile(
        this@MediaViewerActivity,
        authority,
        media.file
      )

      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      putExtra(Intent.EXTRA_STREAM, fileUri)
    }

    return shareIntent
  }
}