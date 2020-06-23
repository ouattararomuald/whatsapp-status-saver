package com.ouattararomuald.statussaver.images.ui

import android.media.browse.MediaBrowser
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.databinding.FragmentImagesBinding
import com.ouattararomuald.statussaver.images.adapters.ImageItem
import com.ouattararomuald.statussaver.images.presenters.ImageContract
import com.ouattararomuald.statussaver.images.presenters.ImagePresenter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

class ImageFragment : Fragment(), ImageContract.ImageView {

  companion object {
    private const val IMAGES_KEY = "images_key"

    @JvmStatic
    fun newInstance(medias: List<Media>): ImageFragment {
      val bundle = bundleOf(
          IMAGES_KEY to medias
      )
      return ImageFragment().apply {
        arguments = bundle
      }
    }
  }

  private lateinit var binding: FragmentImagesBinding
  lateinit var presenter: ImagePresenter
  private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
  private val section = Section()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    binding = FragmentImagesBinding.inflate(layoutInflater, container, false)
    val view = binding.root
    presenter = ImagePresenter(arguments?.getParcelableArrayList(IMAGES_KEY) ?: emptyList(), this)
    groupAdapter = GroupAdapter()
    return view
  }

  override fun onResume() {
    super.onResume()
    groupAdapter.add(section)
    binding.imagesRecyclerView.apply {
      layoutManager = GridLayoutManager(context, 2)
      adapter = groupAdapter
    }
    presenter.start()
  }

  override fun onPause() {
    super.onPause()
    groupAdapter.clear()
  }

  override fun displayMedias(medias: List<Media>) {
    section.addAll(medias.map { media -> media.toImageItem() })
  }

  private fun Media.toImageItem(): ImageItem = ImageItem(this.file)
}