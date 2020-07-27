package com.ouattararomuald.statussaver.common.ui

import android.view.View
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ViewSectionItemBinding
import com.xwray.groupie.viewbinding.BindableItem

class TitleItem(private val sectionTitle: String) : BindableItem<ViewSectionItemBinding>() {
  override fun getLayout(): Int = R.layout.view_section_item

  override fun bind(viewBinding: ViewSectionItemBinding, position: Int) {
    viewBinding.sectionTitleTextView.text = sectionTitle
  }

  override fun initializeViewBinding(view: View): ViewSectionItemBinding =
    ViewSectionItemBinding.bind(view)
}