package com.ouattararomuald.statussaver.images.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.common.Shareable
import com.ouattararomuald.statussaver.common.Updatable
import com.ouattararomuald.statussaver.databinding.FragmentImagesBinding
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.images.adapters.ImageItem
import com.ouattararomuald.statussaver.images.presenters.ImageContract
import com.ouattararomuald.statussaver.images.presenters.ImagePresenter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

class ImageFragment : Fragment(), ImageContract.ImageView, Shareable, Updatable {

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
  private lateinit var gridLayoutManager: GridLayoutManager

  private val selectedMedia: MutableMap<Media, ImageItem> = mutableMapOf()
  private val imageItems = mutableListOf<ImageItem>()

  var homeCommand: HomeContract.HomeCommand? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    binding = FragmentImagesBinding.inflate(layoutInflater, container, false)
    val view = binding.root
    presenter = ImagePresenter(arguments?.getParcelableArrayList(IMAGES_KEY) ?: emptyList(), this)
    groupAdapter = GroupAdapter()
    groupAdapter.spanCount = 2
    //section.setPlaceholder(EmptyItem())

    gridLayoutManager = GridLayoutManager(context, groupAdapter.spanCount).apply {
      spanSizeLookup = groupAdapter.spanSizeLookup
    }

    groupAdapter.add(section)

    binding.imagesRecyclerView.apply {
      layoutManager = gridLayoutManager
      adapter = groupAdapter
      setHasFixedSize(true)
    }

    groupAdapter.setOnItemClickListener { item, _ ->
      if (item is ImageItem) {
        FullScreenImageViewerActivity.start(context!!, imageItems.map { it.media }, item.position)
      }
    }

    groupAdapter.setOnItemLongClickListener { item, _ ->
      if (item is ImageItem) {
        item.toggleSelectionState()
        if (item.isSelected) {
          selectedMedia[item.media] = item
        } else {
          selectedMedia.remove(item.media)
        }
      }

      if (selectedMedia.isEmpty()) {
        homeCommand?.onSelectionCleared()
      } else {
        homeCommand?.onMediaSelected()
      }

      true
    }

    presenter.start()

    return view
  }

  override fun onResume() {
    super.onResume()
    homeCommand?.setCurrentView(this)
    onClearSelection()
  }

  override fun onPause() {
    super.onPause()
    onClearSelection()
  }

  override fun onClearSelection() {
    selectedMedia.forEach { (_, imageItem) ->
      imageItem.toggleSelectionState()
    }
    selectedMedia.clear()
    homeCommand?.onSelectionCleared()
  }

  override fun onShareClicked() {
    homeCommand?.shareImages(selectedMedia.keys.toList())
  }

  override fun onSaveClicked() {
    homeCommand?.saveFiles(selectedMedia.keys.toList())
  }

  override fun displayMedias(medias: List<Media>) {
    val items = medias.mapIndexed { index, media -> media.toImageItem(index) }
    imageItems.clear()
    imageItems.addAll(items)
    section.update(items)
  }

  override fun onUpdateData(medias: List<Media>) {
    displayMedias(medias)
  }

  private fun Media.toImageItem(position: Int): ImageItem = ImageItem(this, position)
}