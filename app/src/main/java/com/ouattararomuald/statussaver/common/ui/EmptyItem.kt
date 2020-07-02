package com.ouattararomuald.statussaver.common.ui

import android.view.View
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewEmptyBinding
import com.xwray.groupie.viewbinding.BindableItem

class EmptyItem : BindableItem<ViewEmptyBinding>() {
  override fun getLayout(): Int = R.layout.view_empty

  override fun bind(viewBinding: ViewEmptyBinding, position: Int) {
  }

  override fun initializeViewBinding(view: View): ViewEmptyBinding = ViewEmptyBinding.bind(view)
}