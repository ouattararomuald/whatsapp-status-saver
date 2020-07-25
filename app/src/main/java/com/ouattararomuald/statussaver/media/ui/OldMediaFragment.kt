package com.ouattararomuald.statussaver.media.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.MediaType
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.Shareable
import com.ouattararomuald.statussaver.common.Updatable
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

class OldMediaFragment : Fragment(), MediaContract.MediaView, Shareable, Updatable {

  companion object {
    private const val IMAGES_KEY = "images_key"
    private const val VIDEOS_KEY = "videos_key"

    @JvmStatic
    fun newInstance(images: List<Media>, videos: List<Media>): OldMediaFragment {
      val bundle = bundleOf(
        IMAGES_KEY to images,
        VIDEOS_KEY to videos
      )
      return OldMediaFragment().apply {
        arguments = bundle
      }
    }
  }

  private lateinit var binding: FragmentOldMediasBinding
  lateinit var presenter: MediaPresenter

  private lateinit var gridLayoutManager: GridLayoutManager
  private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
  private val imagesSection = Section().apply {
    this.setPlaceholder(EmptyItem(R.drawable.ic_no_data))
  }
  private val videosSection = Section().apply {
    this.setPlaceholder(EmptyItem(R.drawable.ic_no_data))
  }

  private val imageItems = mutableListOf<ImageItem>()
  private val videoItems = mutableListOf<VideoItem>()
  private val isViewEmpty: Boolean
    get() = imageItems.isEmpty() && videoItems.isEmpty()

  private var selectedItem: Item<*>? = null
  private var selectedMediaType: MediaType = MediaType.UNKNOWN

  var homeCommand: HomeContract.HomeCommand? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentOldMediasBinding.inflate(layoutInflater, container, false)
    val view = binding.root
    presenter = MediaPresenter(
      arguments?.getParcelableArrayList(IMAGES_KEY) ?: emptyList(),
      arguments?.getParcelableArrayList(VIDEOS_KEY) ?: emptyList(),
      this
    )
    groupAdapter = GroupAdapter()
    gridLayoutManager = GridLayoutManager(context, 2)

    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return if (position in getTitleHeaderIndexes() || isViewEmpty) {
          2
        } else {
          1
        }
      }
    }

    groupAdapter.add(TitleItem("Images"))
    groupAdapter.add(imagesSection)
    groupAdapter.add(TitleItem("Videos"))
    groupAdapter.add(videosSection)

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
      when (item) {
        is ImageItem -> {
          selectedItem?.changeSelectionState()
          item.toggleSelectionState()
          selectedItem = item
          selectedMediaType = MediaType.IMAGE
          if (!item.isSelected) {
            selectedItem = null
          }
        }
        is VideoItem -> {
          selectedItem?.changeSelectionState()
          item.toggleSelectionState()
          selectedItem = item
          selectedMediaType = MediaType.VIDEO
          if (!item.isSelected) {
            selectedItem = null
          }
        }
        else -> {
          selectedMediaType = MediaType.UNKNOWN
        }
      }

      if (selectedItem == null) {
        homeCommand?.onSelectionCleared()
      } else {
        homeCommand?.onMediaSelected()
      }

      true
    }

    presenter.start()

    return view
  }

  private fun Item<*>.changeSelectionState() {
    if (this is ImageItem) {
      this.toggleSelectionState()
    } else if (this is VideoItem) {
      this.toggleSelectionState()
    }
  }

  private fun getTitleHeaderIndexes(): Set<Int> {
    return setOf(getImagesSectionTitleIndex(), getVideosSectionTitleIndex())
  }

  private fun getImagesSectionTitleIndex() = 0

  private fun getVideosSectionTitleIndex() = imageItems.size + 1

  override fun onResume() {
    super.onResume()
    homeCommand?.setCurrentView(this)
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

  override fun onUpdateData(medias: List<Media>) {
    TODO("Not yet implemented")
  }

  override fun displayMedias(images: List<Media>, videos: List<Media>) {
    if (imageItems.isEmpty()) { //FIXME: Verify both lists are different.
      imageItems.addAll(images.mapIndexed { index, media -> media.toImageItem(index) })
    }
    imagesSection.addAll(imageItems)

    if (videoItems.isEmpty()) { //FIXME: Verify both lists are different.
      videoItems.addAll(videos.mapIndexed { index, media -> media.toVideoItem(index) })
    }
    videosSection.addAll(videoItems)
  }

  private fun Media.toImageItem(position: Int): ImageItem = ImageItem(this, position)

  private fun Media.toVideoItem(position: Int): VideoItem = VideoItem(this, position)
}