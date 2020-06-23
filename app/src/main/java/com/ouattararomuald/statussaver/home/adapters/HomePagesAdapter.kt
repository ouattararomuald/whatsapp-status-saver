package com.ouattararomuald.statussaver.home.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ouattararomuald.statussaver.home.models.Page

class HomePagesAdapter(
  private val pages: Array<Page>,
  fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
  override fun getItemCount(): Int = pages.size

  override fun createFragment(position: Int): Fragment = pages[position].fragment
}