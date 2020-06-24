package com.ouattararomuald.statussaver.videos.adapters

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.View
import coil.api.load
import coil.size.Scale
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewVideoBinding
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext


class VideoItem(private val file: File) : BindableItem<ViewVideoBinding>(), CoroutineScope {

  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = job + Dispatchers.Main

  override fun getLayout(): Int = R.layout.view_video

  override fun bind(viewBinding: ViewVideoBinding, position: Int) {
    launch {
      withContext(Dispatchers.IO) {
        val bitmap = file.getVideoThumbnail()
        if (bitmap != null) {
          //withContext(Dispatchers.Main) {
          viewBinding.imageView.load(bitmap) {
            scale(Scale.FILL)
          }
          //}
        }
      }
    }
  }

  override fun initializeViewBinding(view: View): ViewVideoBinding {
    return ViewVideoBinding.bind(view)
  }

  private fun File.getVideoThumbnail(): Bitmap? {
    return if (Build.VERSION.SDK_INT >= 29) {
      ThumbnailUtils.createVideoThumbnail(this, Size(THUMBNAIL_SIZE, THUMBNAIL_SIZE), null)
    } else {
      ThumbnailUtils.createVideoThumbnail(this.absolutePath, MediaStore.Images.Thumbnails.MINI_KIND)
    }
  }

  companion object {
    private const val THUMBNAIL_SIZE = 512
  }
}