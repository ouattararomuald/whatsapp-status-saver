package com.ouattararomuald.statussaver.images.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ActivityFullscreenImageViewerBinding
import com.ouattararomuald.statussaver.images.adapters.FullScreenImagePager
import com.ouattararomuald.statussaver.videos.ui.VideoPlayerActivity

class FullScreenImageViewerActivity : AppCompatActivity() {

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
  private var selectedImageIndex = 0

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

    binding.shareImageButton.setOnClickListener {
      if (selectedImageIndex >= 0 && selectedImageIndex < images.size) {
        startActivity(Intent.createChooser(
          getMediasShareIntent(images[selectedImageIndex]),
          resources.getText(R.string.send_to)
        ))
      }
    }

    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      hide()
    }
  }

  private fun enableFullScreen() {
    if (Build.VERSION.SDK_INT >= 30) {
      window.setDecorFitsSystemWindows(false)
    } else {
      window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
  }

  private fun getMediasShareIntent(media: Media): Intent {
    val shareIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.send_title))
      type = "image/*"

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