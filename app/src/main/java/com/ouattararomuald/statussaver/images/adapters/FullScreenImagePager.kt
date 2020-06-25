package com.ouattararomuald.statussaver.images.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.size.Scale
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.databinding.FullscreenImageViewBinding

class FullScreenImagePager(private val context: Context, private val medias: List<Media>) :
    RecyclerView.Adapter<FullScreenImagePager.ImageViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
    val binding = FullscreenImageViewBinding.inflate(LayoutInflater.from(context), parent, false)
    return ImageViewHolder(binding)
  }

  override fun getItemCount(): Int = medias.size

  override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
    holder.binding.imageView.load(medias[position].file) {
      scale(Scale.FILL)
    }
  }

  class ImageViewHolder(val binding: FullscreenImageViewBinding) : RecyclerView.ViewHolder(
      binding.root)
}