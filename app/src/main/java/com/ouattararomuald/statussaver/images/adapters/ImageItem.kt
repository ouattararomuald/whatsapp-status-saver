package com.ouattararomuald.statussaver.images.adapters

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import coil.api.load
import coil.size.Scale
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewImageBinding
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class ImageItem(val media: Media, val position: Int) : BindableItem<ViewImageBinding>() {

  private lateinit var selectorFrameLayout: FrameLayout

  var isSelected = false
    private set

  override fun getLayout(): Int = R.layout.view_image

  override fun getSpanSize(spanCount: Int, position: Int): Int = spanCount / 2

  override fun bind(viewBinding: ViewImageBinding, position: Int) {
    selectorFrameLayout = viewBinding.selectorFrameLayout
    viewBinding.imageView.load(media.file) {
      scale(Scale.FILL)
    }
    selectorFrameLayout.isVisible = isSelected
  }

  override fun initializeViewBinding(view: View): ViewImageBinding {
    return ViewImageBinding.bind(view)
  }

  fun toggleSelectionState() {
    isSelected = !isSelected
    selectorFrameLayout.isVisible = isSelected
  }

  override fun isSameAs(other: Item<*>): Boolean {
    val otherImageItem = other as ImageItem
    return media.file == otherImageItem.media.file && position == otherImageItem.position
  }

  override fun hasSameContentAs(other: Item<*>): Boolean {
    return media.file == (other as ImageItem).media.file
  }
}