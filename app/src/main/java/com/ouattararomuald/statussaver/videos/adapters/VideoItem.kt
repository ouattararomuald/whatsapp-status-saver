package com.ouattararomuald.statussaver.videos.adapters

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.View
import coil.api.load
import coil.size.Scale
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewVideoBinding
import com.xwray.groupie.viewbinding.BindableItem
import java.io.File

class VideoItem(private val file: File) : BindableItem<ViewVideoBinding>() {
  override fun getLayout(): Int = R.layout.view_video

  override fun bind(viewBinding: ViewVideoBinding, position: Int) {
    val bitmap = file.getVideoThumbnail()
    if (bitmap != null) {
      viewBinding.imageView.load(bitmap) {
        scale(Scale.FILL)
      }
    }
  }

  override fun initializeViewBinding(view: View): ViewVideoBinding {
    return ViewVideoBinding.bind(view)
  }

  private fun File.getVideoThumbnail(): Bitmap? {
    return ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Images.Thumbnails.MINI_KIND)
  }

  companion object {
    private const val THUMBNAIL_SIZE = 200
  }
}