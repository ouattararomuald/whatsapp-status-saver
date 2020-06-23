package com.ouattararomuald.statussaver.images.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.view.View
import coil.api.load
import coil.size.Scale
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewImageBinding
import com.xwray.groupie.viewbinding.BindableItem
import java.io.File

class ImageItem(private val file: File) : BindableItem<ViewImageBinding>() {
  override fun getLayout(): Int = R.layout.view_image

  override fun bind(viewBinding: ViewImageBinding, position: Int) {
    viewBinding.imageView.load(file) {
      scale(Scale.FILL)
    }
  }

  override fun initializeViewBinding(view: View): ViewImageBinding {
    return ViewImageBinding.bind(view)
  }

  private fun File.getImageThumbnail(): Bitmap {
    return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(absolutePath),
        THUMBNAIL_SIZE, THUMBNAIL_SIZE)
  }

  companion object {
    private const val THUMBNAIL_SIZE = 64
  }
}