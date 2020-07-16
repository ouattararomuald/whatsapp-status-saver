package com.ouattararomuald.statussaver.home.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ouattararomuald.statussaver.home.models.Page

class HomePagesAdapter(
  pages: Array<Page>,
  fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
  private val pages = mutableListOf(*pages)

  override fun getItemCount(): Int = pages.size

  override fun createFragment(position: Int): Fragment = pages[position].fragment

  fun addPage(page: Page) {
    pages.add(page)
  }

  fun refresh(pages: Array<Page>) {
    this.pages.clear()
    this.pages.addAll(pages)
  }
}