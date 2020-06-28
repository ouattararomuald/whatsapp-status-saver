package com.ouattararomuald.statussaver.images.ui

import android.os.Bundle
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.util.SparseIntArray
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
import java.io.File

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

  private val selectedMedia: MutableMap<Media, Boolean> = mutableMapOf()
  private val imageItems = mutableListOf<ImageItem>()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    binding = FragmentImagesBinding.inflate(layoutInflater, container, false)
    val view = binding.root
    presenter = ImagePresenter(arguments?.getParcelableArrayList(IMAGES_KEY) ?: emptyList(), this)
    groupAdapter = GroupAdapter()

    groupAdapter.add(section)
    binding.imagesRecyclerView.apply {
      layoutManager = GridLayoutManager(context, 2)
      adapter = groupAdapter
      setHasFixedSize(true)
    }

    groupAdapter.setOnItemClickListener { item, view ->
      if (item is ImageItem) {
        FullScreenImageViewerActivity.start(context!!, imageItems.map { it.media })
      }
    }

    groupAdapter.setOnItemLongClickListener { item, view ->
      if (item is ImageItem) {
        item.toggleSelectionState()
        if (item.isSelected) {
          selectedMedia[item.media] = item.isSelected
        } else {
          selectedMedia.remove(item.media)
        }
      }
      true
    }

    presenter.start()

    return view
  }

  override fun displayMedias(medias: List<Media>) {
    if (imageItems.isEmpty()) { //FIXME: Verify both lists are different.
      imageItems.addAll(medias.mapIndexed { index, media ->  media.toImageItem(index) })
    }
    section.addAll(imageItems)
  }

  private fun Media.toImageItem(position: Int): ImageItem = ImageItem(this, position)
}