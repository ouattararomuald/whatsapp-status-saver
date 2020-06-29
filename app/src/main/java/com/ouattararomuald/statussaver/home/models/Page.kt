package com.ouattararomuald.statussaver.home.models

import androidx.fragment.app.Fragment
import com.ouattararomuald.statussaver.common.Shareable

class Page(val title: String, val fragment: Fragment) : Shareable {
  override fun onClearSelection() {
    if (fragment is Shareable) {
      fragment.onClearSelection()
    }
  }

  override fun onShareClicked() {
    if (fragment is Shareable) {
      fragment.onShareClicked()
    }
  }
}
