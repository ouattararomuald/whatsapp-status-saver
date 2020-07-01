package com.ouattararomuald.statussaver.videos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.common.Shareable
import com.ouattararomuald.statussaver.databinding.FragmentVideosBinding
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.videos.adapters.VideoItem
import com.ouattararomuald.statussaver.videos.presenters.VideoContract
import com.ouattararomuald.statussaver.videos.presenters.VideoPresenter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

class VideoFragment : Fragment(), VideoContract.VideoView, Shareable {

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

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    binding = FragmentVideosBinding.inflate(layoutInflater, container, false)
    val view = binding.root
    presenter = VideoPresenter(arguments?.getParcelableArrayList(VideoFragment.VIDEOS_KEY) ?: emptyList(), this)
    groupAdapter = GroupAdapter()

    groupAdapter.add(section)
    binding.imagesRecyclerView.apply {
      layoutManager = GridLayoutManager(context, 2)
      adapter = groupAdapter
      setHasFixedSize(true)
    }

    groupAdapter.setOnItemClickListener { item, _ ->
      if (item is VideoItem) {
        VideoPlayerActivity.start(context!!, videoItems.map { it.media }, item.position)
      }
    }

    groupAdapter.setOnItemLongClickListener { item, _ ->
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

      true
    }

    presenter.start()

    return view
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
    homeCommand?.shareImages(selectedMedia.keys.toList())
  }

  override fun displayMedias(medias: List<Media>) {
    if (videoItems.isEmpty()) { //FIXME: Verify both lists are different.
      videoItems.addAll(medias.mapIndexed { index, media ->  media.toVideoItem(index) })
    }
    section.addAll(videoItems)
  }

  private fun Media.toVideoItem(index: Int): VideoItem = VideoItem(this, index)
}