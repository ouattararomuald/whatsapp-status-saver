package com.ouattararomuald.statussaver.videos.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton.OnVisibilityChangedListener
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.VIDEO_MIME_TYPE
import com.ouattararomuald.statussaver.common.ui.MediaViewerActivity
import com.ouattararomuald.statussaver.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : MediaViewerActivity() {

  companion object {
    private const val SELECTED_VIDEO_INDEX_KEY = "selected_video_index_key"
    private const val VIDEO_KEY = "video_key"

    /**
     * Whether or not the system UI should be auto-hidden after
     * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
     */
    private const val AUTO_HIDE = true

    /**
     * If [AUTO_HIDE] is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private const val AUTO_HIDE_DELAY_MILLIS = 3000

    fun start(context: Context, videos: List<Media>, clickedItemPosition: Int) {
      val intent = Intent(context, VideoPlayerActivity::class.java)
      intent.apply {
        putExtra(SELECTED_VIDEO_INDEX_KEY, clickedItemPosition)
        putParcelableArrayListExtra(VIDEO_KEY, ArrayList(videos))
      }
      context.startActivity(intent)
    }
  }

  private lateinit var binding: ActivityVideoPlayerBinding
  private lateinit var videos: List<Media>
  private var videoToWriteIndex = 0
  private var selectedVideoIndex = 0
  private var isFabExpanded = false

  private var player: SimpleExoPlayer? = null

  private var playWhenReady = true
  private var playbackPosition: Long = 0

  @SuppressLint("ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    if (intent.extras?.containsKey(VIDEO_KEY) == true) {
      videos = intent.getParcelableArrayListExtra(VIDEO_KEY)!!
    }
    if (intent.extras?.containsKey(SELECTED_VIDEO_INDEX_KEY) == true) {
      selectedVideoIndex = intent.getIntExtra(SELECTED_VIDEO_INDEX_KEY, 0)
    }

    binding.optionsVideoButton.setOnClickListener {
      if (isFabExpanded) {
        hideSubMenus()
      } else {
        showSubMenus()
      }
    }

    binding.shareVideoButton.setOnClickListener {
      videoToWriteIndex = player?.currentWindowIndex ?: 0
      if (videoToWriteIndex >= 0 && videoToWriteIndex < videos.size) {
        selectedVideoIndex = videoToWriteIndex
        // videoToWriteIndex = selectedVideoIndex
        startActivity(Intent.createChooser(
          getShareMediaIntent(videos[videoToWriteIndex]),
          resources.getText(R.string.send_to)
        ))
      }
    }

    binding.saveVideoButton.setOnClickListener {
      videoToWriteIndex = player?.currentWindowIndex ?: 0
      if (videoToWriteIndex >= 0 && videoToWriteIndex < videos.size) {
        selectedVideoIndex = videoToWriteIndex
        val videoMedia = videos[videoToWriteIndex]
        saveFile(videoMedia.file)
      }
    }

    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      hide()
    }
  }

  override fun getMedias(): List<Media> = videos

  override fun getMediaToWriteIndex(): Int = videoToWriteIndex

  override fun getMediaMimeType(): String = VIDEO_MIME_TYPE

  override fun getRootView(): View = binding.root

  private fun showSubMenus() {
    binding.optionsVideoButton.setImageDrawable(resources.getDrawable(R.drawable.ic_clear, theme))
    isFabExpanded = true
  }

  private fun hideSubMenus() {
    binding.optionsVideoButton.setImageDrawable(resources.getDrawable(R.drawable.ic_add, theme))
    isFabExpanded = false
  }

  override fun onStart() {
    super.onStart()
    initializePlayer()
  }

  private fun initializePlayer() {
    player = ExoPlayerFactory.newSimpleInstance(this)
    binding.videoView.player = player

    binding.videoView.controllerHideOnTouch = true

    binding.videoView.setControllerVisibilityListener { visibility ->
      when(visibility) {
        View.VISIBLE -> {
          binding.optionsVideoButton.hide()
        }
        else -> {
          binding.optionsVideoButton.show()
        }
      }
    }

    binding.videoView.hideController()

    val mediaSource = buildMediaSource()
    (player as SimpleExoPlayer).run {
      this.playWhenReady = this@VideoPlayerActivity.playWhenReady
      seekTo(selectedVideoIndex, playbackPosition)
      prepare(mediaSource, false, false)
    }
  }

  private fun buildMediaSource(): MediaSource? {
    val dataSourceFactory: DataSource.Factory =
      DefaultDataSourceFactory(this, "exoplayer-status-saver")

    val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

    val mediasSources = videos.map { video ->
      mediaSourceFactory.createMediaSource(Uri.fromFile(video.file))
    }.toTypedArray()

    return ConcatenatingMediaSource(*mediasSources)
  }

  override fun onStop() {
    super.onStop()
    releasePlayer()
  }

  private fun releasePlayer() {
    if (player != null) {
      (player as SimpleExoPlayer).run {
        this@VideoPlayerActivity.playWhenReady = this.playWhenReady
        playbackPosition = currentPosition
        selectedVideoIndex = currentWindowIndex
        release()
      }
      player = null
    }
  }
}