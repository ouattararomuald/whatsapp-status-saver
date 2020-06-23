package com.ouattararomuald.statussaver.core.util

import android.content.res.Resources

import android.content.res.TypedArray
import android.graphics.Color
import com.ouattararomuald.statussaver.R

class ColorUtil private constructor() {

  companion object {
    @JvmStatic
    fun getMaterialColor(resources: Resources, index: Int): Int {
      val colors: TypedArray = resources.obtainTypedArray(R.array.mdcolor_300)
      val returnColor = colors.getColor(index % colors.length(), Color.BLACK)
      colors.recycle()
      return returnColor
    }
  }
}