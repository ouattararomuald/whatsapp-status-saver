package com.ouattararomuald.statussaver.images.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.IMAGE_MIME_TYPE
import com.ouattararomuald.statussaver.common.ui.MediaViewerActivity
import com.ouattararomuald.statussaver.databinding.ActivityFullscreenImageViewerBinding
import com.ouattararomuald.statussaver.images.adapters.FullScreenImagePager

class FullScreenImageViewerActivity : MediaViewerActivity() {

  companion object {
    private const val SELECTED_IMAGE_INDEX_KEY = "selected_image_index_key"
    private const val IMAGES_KEY = "images_key"

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFullscreenImageViewerBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

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
            getShareMediaIntent(images[selectedImageIndex]),
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

  override fun getMedias(): List<Media> = images

  override fun getMediaToWriteIndex(): Int = imageToWriteIndex

  override fun getMediaMimeType(): String = IMAGE_MIME_TYPE

  override fun getRootView(): View = binding.root

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
}