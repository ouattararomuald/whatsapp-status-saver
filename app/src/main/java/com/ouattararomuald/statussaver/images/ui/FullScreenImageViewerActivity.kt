package com.ouattararomuald.statussaver.images.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.IMAGE_MIME_TYPE
import com.ouattararomuald.statussaver.core.FileHelper
import com.ouattararomuald.statussaver.databinding.ActivityFullscreenImageViewerBinding
import com.ouattararomuald.statussaver.images.adapters.FullScreenImagePager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

class FullScreenImageViewerActivity : AppCompatActivity(), CoroutineScope {

  companion object {
    private const val SELECTED_IMAGE_INDEX_KEY = "selected_image_index_key"
    private const val IMAGES_KEY = "images_key"
    private const val RQ_CREATE_FILE = 0xace

    fun start(context: Context, images: List<Media>, clickedItemPosition: Int) {
      val intent = Intent(context, FullScreenImageViewerActivity::class.java)
      intent.apply {
        putExtra(SELECTED_IMAGE_INDEX_KEY, clickedItemPosition)
        putParcelableArrayListExtra(IMAGES_KEY, ArrayList(images))
      }
      context.startActivity(intent)
    }
  }

  private lateinit var binding: ActivityFullscreenImageViewerBinding
  private lateinit var images: List<Media>
  private var imageToWriteIndex = 0
  private var selectedImageIndex = 0
  private var isFabExpanded = false

  private var fileHelper = FileHelper()

  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = Dispatchers.Main + job

  private val handler = CoroutineExceptionHandler { _, exception ->
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFullscreenImageViewerBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    enableFullScreen()

    if (intent.extras?.containsKey(IMAGES_KEY) == true) {
      images = intent.getParcelableArrayListExtra(IMAGES_KEY)!!
    }

    if (intent.extras?.containsKey(SELECTED_IMAGE_INDEX_KEY) == true) {
      selectedImageIndex = intent.getIntExtra(SELECTED_IMAGE_INDEX_KEY, 0)
    }

    binding.pager.adapter = FullScreenImagePager(this, images)
    binding.pager.currentItem = selectedImageIndex

    hideSubMenus()

    binding.optionsImageButton.setOnClickListener {
      if (isFabExpanded) {
        hideSubMenus()
      } else {
        showSubMenus()
      }
    }

    binding.shareImageButton.setOnClickListener {
      if (selectedImageIndex >= 0 && selectedImageIndex < images.size) {
        startActivity(
          Intent.createChooser(
            getMediasShareIntent(images[selectedImageIndex]),
            resources.getText(R.string.send_to)
          )
        )
      }
    }

    binding.saveImageButton.setOnClickListener {
      val imageMedia = images[selectedImageIndex]
      imageToWriteIndex = selectedImageIndex
      saveFile(imageMedia.file)
    }

    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      hide()
    }
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
              fileHelper.writeFile(it, images[imageToWriteIndex].file)
            }
            displaySuccessMessage()
          }
        }
      }
    }
  }

  private fun displaySuccessMessage() {
    Snackbar.make(
      binding.root,
      "The file has been successfully saved.",
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color, theme)).show()
  }

  private fun displayFailureMessage() {
    Snackbar.make(
      binding.root,
      "Oups! Something went wrong while saving the file.",
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color_failure, theme)).show()
  }

  private fun saveFile(file: File) {
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

  private fun showSubMenus() {
    binding.shareImageButton.isVisible = true
    binding.saveImageButton.isVisible = true
    binding.optionsImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_clear, theme))
    isFabExpanded = true
  }

  private fun hideSubMenus() {
    binding.shareImageButton.isVisible = false
    binding.saveImageButton.isVisible = false
    binding.optionsImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_add, theme))
    isFabExpanded = false
  }

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

  private fun getMediasShareIntent(media: Media): Intent {
    val shareIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.send_title))
      type = IMAGE_MIME_TYPE

      val authority = "${applicationContext.packageName}.fileprovider"
      val fileUri = FileProvider.getUriForFile(
        this@FullScreenImageViewerActivity,
        authority,
        media.file
      )

      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      putExtra(Intent.EXTRA_STREAM, fileUri)
    }

    return shareIntent
  }
}