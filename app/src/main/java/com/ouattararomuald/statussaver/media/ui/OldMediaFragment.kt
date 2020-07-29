package com.ouattararomuald.statussaver.media.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.MediaType
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.NUMBER_OF_SPANS_IN_LANDSCAPE
import com.ouattararomuald.statussaver.common.NUMBER_OF_SPANS_IN_PORTRAIT
import com.ouattararomuald.statussaver.common.Shareable
import com.ouattararomuald.statussaver.common.UpdatableOldMedia
import com.ouattararomuald.statussaver.common.ui.EmptyItem
import com.ouattararomuald.statussaver.common.ui.TitleItem
import com.ouattararomuald.statussaver.databinding.FragmentOldMediasBinding
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.images.adapters.ImageItem
import com.ouattararomuald.statussaver.images.ui.FullScreenImageViewerActivity
import com.ouattararomuald.statussaver.media.presenters.MediaContract
import com.ouattararomuald.statussaver.media.presenters.MediaPresenter
import com.ouattararomuald.statussaver.videos.adapters.VideoItem
import com.ouattararomuald.statussaver.videos.ui.VideoPlayerActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.Section

class OldMediaFragment : Fragment(), MediaContract.MediaView, Shareable, UpdatableOldMedia {

  companion object {
    @JvmStatic
    fun newInstance(): OldMediaFragment = OldMediaFragment()
  }

  private lateinit var binding: FragmentOldMediasBinding
  lateinit var presenter: MediaPresenter

  private lateinit var gridLayoutManager: GridLayoutManager
  private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
  private val imagesSection = Section()
  private val videosSection = Section()

  private val imageItems = mutableListOf<ImageItem>()
  private val videoItems = mutableListOf<VideoItem>()

  private var selectedItem: Item<*>? = null
  private var selectedMediaType: MediaType = MediaType.UNKNOWN

  var homeCommand: HomeContract.HomeCommand? = null

  private var spanCount: Int = NUMBER_OF_SPANS_IN_PORTRAIT

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val currentOrientation = resources.configuration.orientation
    spanCount = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
      NUMBER_OF_SPANS_IN_LANDSCAPE
    } else {
      NUMBER_OF_SPANS_IN_PORTRAIT
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentOldMediasBinding.inflate(layoutInflater, container, false)
    val view = binding.root
    presenter = MediaPresenter(
        context!!,
        this
    )
    groupAdapter = GroupAdapter()
    groupAdapter.spanCount = spanCount
    gridLayoutManager = GridLayoutManager(context, groupAdapter.spanCount).apply {
      spanSizeLookup = groupAdapter.spanSizeLookup
    }

    populateAdapter()

    binding.mediasRecyclerView.apply {
      layoutManager = gridLayoutManager
      adapter = groupAdapter
      setHasFixedSize(true)
    }

    groupAdapter.setOnItemClickListener { item, _ ->
      if (item is ImageItem) {
        FullScreenImageViewerActivity.start(context!!, imageItems.map { it.media }, item.position)
      }
      if (item is VideoItem) {
        VideoPlayerActivity.start(context!!, videoItems.map { it.media }, item.position)
      }
    }

    groupAdapter.setOnItemLongClickListener { item, _ ->
      if (!typeSupportLongClick(item)) {
        return@setOnItemLongClickListener false
      }

      if (selectedItem == item) {
        changeCurrentSelectionVisualState()
      } else {
        changeSelectionVisualState(item)
      }

      if (selectedItem == null) {
        homeCommand?.onSelectionCleared()
      } else {
        homeCommand?.onMediaSelected()
      }

      true
    }

    return view
  }

  private fun typeSupportLongClick(item: Item<*>): Boolean {
    return item is ImageItem || item is VideoItem
  }

  private fun changeCurrentSelectionVisualState() {
    selectedItem?.let {
      when (it) {
        is ImageItem -> {
          it.changeSelectionState()
          if (!it.isSelected) {
            selectedItem = null
          }
        }
        is VideoItem -> {
          it.changeSelectionState()
          if (!it.isSelected) {
            selectedItem = null
          }
        }
        else -> {
          selectedItem = null
          selectedMediaType = MediaType.UNKNOWN
        }
      }
    }
  }

  private fun changeSelectionVisualState(newItem: Item<*>) {
    when (newItem) {
      is ImageItem -> {
        selectedItem?.changeSelectionState()
        newItem.toggleSelectionState()
        selectedItem = newItem
        selectedMediaType = MediaType.IMAGE
        if (!newItem.isSelected) {
          selectedItem = null
        }
      }
      is VideoItem -> {
        selectedItem?.changeSelectionState()
        newItem.toggleSelectionState()
        selectedItem = newItem
        selectedMediaType = MediaType.VIDEO
        if (!newItem.isSelected) {
          selectedItem = null
        }
      }
      else -> {
        selectedItem = null
        selectedMediaType = MediaType.UNKNOWN
      }
    }
  }

  private fun populateAdapter() {
    groupAdapter.apply {
      add(Section(TitleItem("Images")).apply {
        add(imagesSection)
        setPlaceholder(EmptyItem(R.drawable.ic_no_data))
      })
      add(Section(TitleItem("Videos")).apply {
        add(videosSection)
        setPlaceholder(EmptyItem(R.drawable.ic_no_data))
      })
    }
  }

  private fun Item<*>.changeSelectionState() {
    if (this is ImageItem) {
      this.toggleSelectionState()
    } else if (this is VideoItem) {
      this.toggleSelectionState()
    }
  }

  override fun onResume() {
    super.onResume()
    homeCommand?.setCurrentView(this)
    presenter.refresh()
  }

  override fun onPause() {
    super.onPause()
    onClearSelection()
  }

  override fun onClearSelection() {
    selectedItem?.changeSelectionState()
    selectedItem = null
    selectedMediaType = MediaType.UNKNOWN
    homeCommand?.onSelectionCleared()
  }

  override fun onShareClicked() {
    if (selectedItem == null) {
      homeCommand?.shareImages(emptyList())
      return
    }
    selectedItem?.also { currentItem ->
      if (currentItem is ImageItem && selectedMediaType == MediaType.IMAGE) {
        homeCommand?.shareImages(listOf(currentItem.media))
      } else if (currentItem is VideoItem && selectedMediaType == MediaType.VIDEO) {
        homeCommand?.shareVideos(listOf(currentItem.media))
      }
    }
  }

  override fun onSaveClicked() {
    selectedItem?.let { item ->
      when(item) {
        is ImageItem -> {
          homeCommand?.saveFiles(listOf(item.media))
        }
        is VideoItem -> {
          homeCommand?.saveFiles(listOf(item.media))
        }
        else -> {} // Nothing to do
      }
    }
  }

  override fun onUpdateData(images: List<Media>, videos: List<Media>) {
    displayMedias(images, videos)
  }

  override fun displayMedias(images: List<Media>, videos: List<Media>) {
    val imageMediaItems = images.mapIndexed { index, media -> media.toImageItem(index) }
    val videoMediaItems = videos.mapIndexed { index, media -> media.toVideoItem(index) }
    imageItems.addAll(imageMediaItems)
    videoItems.addAll(videoMediaItems)
    imagesSection.update(imageMediaItems)
    videosSection.update(videoMediaItems)
  }

  private fun Media.toImageItem(position: Int): ImageItem = ImageItem(this, position, spanCount)

  private fun Media.toVideoItem(position: Int): VideoItem = VideoItem(this, position, spanCount)
}