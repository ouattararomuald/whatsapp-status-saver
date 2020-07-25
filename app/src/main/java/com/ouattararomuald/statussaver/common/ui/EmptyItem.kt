package com.ouattararomuald.statussaver.common.ui

import android.view.View
import androidx.annotation.DrawableRes
import coil.api.load
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewEmptyBinding
import com.xwray.groupie.viewbinding.BindableItem

class EmptyItem(@DrawableRes private val imageRes: Int) : BindableItem<ViewEmptyBinding>() {
  override fun getLayout(): Int = R.layout.view_empty

  override fun bind(viewBinding: ViewEmptyBinding, position: Int) {
    //viewBinding.emptyImageView.load(imageRes)
  }

  override fun initializeViewBinding(view: View): ViewEmptyBinding = ViewEmptyBinding.bind(view)
}