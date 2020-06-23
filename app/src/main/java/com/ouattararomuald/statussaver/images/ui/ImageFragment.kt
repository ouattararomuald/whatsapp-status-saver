package com.ouattararomuald.statussaver.images.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ouattararomuald.statussaver.R

class ImageFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_images, container, false)
  }

  companion object {
    @JvmStatic
    fun newInstance() = ImageFragment()
  }
}