package com.ouattararomuald.statussaver.videos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ouattararomuald.statussaver.R

class VideoFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_videos, container, false)
  }

  companion object {
    @JvmStatic
    fun newInstance() = VideoFragment()
  }
}