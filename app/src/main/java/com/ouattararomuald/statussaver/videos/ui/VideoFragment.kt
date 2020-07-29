package com.ouattararomuald.statussaver.videos.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.common.NUMBER_OF_SPANS_IN_LANDSCAPE
import com.ouattararomuald.statussaver.common.NUMBER_OF_SPANS_IN_PORTRAIT
import com.ouattararomuald.statussaver.common.Shareable
import com.ouattararomuald.statussaver.common.Updatable
import com.ouattararomuald.statussaver.databinding.FragmentVideosBinding
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.videos.adapters.VideoItem
import com.ouattararomuald.statussaver.videos.presenters.VideoContract
import com.ouattararomuald.statussaver.videos.presenters.VideoPresenter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.Section

class VideoFragment : Fragment(), VideoContract.VideoView, Shareable, Updatable {

  companion object {
    private const val VIDEOS_KEY = "videos_key"

    @JvmStatic
    fun newInstance(medias: List<Media>): VideoFragment {
      val bundle = bundleOf(
          VIDEOS_KEY to medias
      )
      return VideoFragment().apply {
        arguments = bundle
      }
    }
  }

  private lateinit var binding: FragmentVideosBinding
  lateinit var presenter: VideoPresenter
  private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
  private val section = Section()

  private val selectedMedia: MutableMap<Media, VideoItem> = mutableMapOf()
  private val videoItems = mutableListOf<VideoItem>()

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
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    binding = FragmentVideosBinding.inflate(layoutInflater, container, false)
    val view = binding.root
    presenter =
        VideoPresenter(arguments?.getParcelableArrayList(VIDEOS_KEY) ?: emptyList(), this)
    groupAdapter = GroupAdapter()
    groupAdapter.spanCount = spanCount
    //section.setPlaceholder(EmptyItem())

    groupAdapter.add(section)
    binding.imagesRecyclerView.apply {
      layoutManager = GridLayoutManager(context, groupAdapter.spanCount).apply {
        spanSizeLookup = groupAdapter.spanSizeLookup
      }
      adapter = groupAdapter
      setHasFixedSize(true)
    }

    groupAdapter.setOnItemClickListener { item, _ ->
      if (selectedMedia.isNotEmpty()) {
        handleLongClick(item)
      } else if (item is VideoItem) {
        VideoPlayerActivity.start(context!!, videoItems.map { it.media }, item.position)
      }
    }

    groupAdapter.setOnItemLongClickListener { item, _ ->
      handleLongClick(item)

      true
    }

    presenter.start()

    return view
  }

  private fun handleLongClick(item: Item<*>) {
    if (item is VideoItem) {
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
  }

  override fun onResume() {
    super.onResume()
    homeCommand?.setCurrentView(this)
  }

  override fun onPause() {
    super.onPause()
    onClearSelection()
  }

  override fun onClearSelection() {
    selectedMedia.forEach { (_, videoItem) ->
      videoItem.toggleSelectionState()
    }
    selectedMedia.clear()
    homeCommand?.onSelectionCleared()
  }

  override fun onShareClicked() {
    homeCommand?.shareVideos(selectedMedia.keys.toList())
  }

  override fun onSaveClicked() {
    homeCommand?.saveFiles(selectedMedia.keys.toList())
  }

  override fun displayMedias(medias: List<Media>) {
    val items = medias.mapIndexed { index, media -> media.toVideoItem(index) }
    videoItems.clear()
    videoItems.addAll(items)
    section.update(items)
  }

  override fun onUpdateData(medias: List<Media>) {
    displayMedias(medias)
  }

  private fun Media.toVideoItem(position: Int): VideoItem = VideoItem(this, position, spanCount)
}