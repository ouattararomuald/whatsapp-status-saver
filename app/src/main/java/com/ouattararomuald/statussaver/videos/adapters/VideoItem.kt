package com.ouattararomuald.statussaver.videos.adapters

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import coil.api.load
import coil.size.Scale
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewVideoBinding
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

class VideoItem(
  val media: Media,
  val position: Int,
  private val totalSpanCount: Int
) : BindableItem<ViewVideoBinding>(), CoroutineScope {

  private val job = SupervisorJob()

  override val coroutineContext: CoroutineContext
    get() = job + Dispatchers.Main

  private lateinit var selectorFrameLayout: FrameLayout

  var isSelected = false
    private set

  override fun getLayout(): Int = R.layout.view_video

  override fun getSpanSize(spanCount: Int, position: Int): Int = spanCount / totalSpanCount

  override fun bind(viewBinding: ViewVideoBinding, position: Int) {
    selectorFrameLayout = viewBinding.selectorFrameLayout
    launch {
      withContext(Dispatchers.IO) {
        val bitmap = media.file.getVideoThumbnail()
        if (bitmap != null) {
          viewBinding.imageView.load(bitmap) {
            scale(Scale.FILL)
          }
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

  fun toggleSelectionState() {
    isSelected = !isSelected
    selectorFrameLayout.isVisible = isSelected
  }

  override fun isSameAs(other: Item<*>): Boolean {
    val otherVideoItem = other as VideoItem
    return media.file == otherVideoItem.media.file && position == otherVideoItem.position
  }

  override fun hasSameContentAs(other: Item<*>): Boolean {
    return media.file == (other as VideoItem).media.file
  }

  companion object {
    private const val THUMBNAIL_SIZE = 512
  }
}