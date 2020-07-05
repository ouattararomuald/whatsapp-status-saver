package com.ouattararomuald.statussaver.videos.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.snackbar.Snackbar
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.common.VIDEO_MIME_TYPE
import com.ouattararomuald.statussaver.core.FileHelper
import com.ouattararomuald.statussaver.databinding.ActivityVideoPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class VideoPlayerActivity : AppCompatActivity() {

  companion object {
    private const val SELECTED_VIDEO_INDEX_KEY = "selected_video_index_key"
    private const val VIDEO_KEY = "video_key"
    private const val RQ_CREATE_FILE = 0xbee

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

  private var fileHelper = FileHelper()
  private var player: SimpleExoPlayer? = null

  private var playWhenReady = true
  private var playbackPosition: Long = 0

  @SuppressLint("ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    enableFullScreen()

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
      if (selectedVideoIndex >= 0 && selectedVideoIndex < videos.size) {
        startActivity(Intent.createChooser(
          getMediasShareIntent(videos[selectedVideoIndex]),
          resources.getText(R.string.send_to)
        ))
      }
    }

    binding.saveVideoButton.setOnClickListener {
      val videoMedia = videos[selectedVideoIndex]
      videoToWriteIndex = selectedVideoIndex
      saveFile(videoMedia.file)
    }

    supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      hide()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RQ_CREATE_FILE) {
      if (resultCode == Activity.RESULT_OK) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val uri = data?.data
          if (uri != null) {
            val stream = contentResolver.openOutputStream(uri)
            stream?.let {
              fileHelper.writeFile(it, videos[videoToWriteIndex].file)
            }
            displaySuccessMessage()
          }
        }
      }
    }
  }

  private fun displaySuccessMessage() {
    Snackbar.make(
      binding.root,
      "The file has been successfully saved.",
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color, theme)).show()
  }

  private fun displayFailureMessage() {
    Snackbar.make(
      binding.root,
      "Oups! Something went wrong while saving the file.",
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color_failure, theme)).show()
  }

  private fun saveFile(file: File) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createFile(file)
    } else {
      fileHelper.writeFile(file) {
        withContext(Dispatchers.Main) {
          displaySuccessMessage()
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createFile(file: File) {
    val fileName = file.name
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = VIDEO_MIME_TYPE
      putExtra(Intent.EXTRA_TITLE, fileName)
    }
    startActivityForResult(intent, RQ_CREATE_FILE)
  }

  private fun showSubMenus() {
    binding.shareVideoButton.isVisible = true
    binding.saveVideoButton.isVisible = true
    binding.optionsVideoButton.setImageDrawable(resources.getDrawable(R.drawable.ic_clear, theme))
    isFabExpanded = true
  }

  private fun hideSubMenus() {
    binding.shareVideoButton.isVisible = false
    binding.saveVideoButton.isVisible = false
    binding.optionsVideoButton.setImageDrawable(resources.getDrawable(R.drawable.ic_add, theme))
    isFabExpanded = false
  }

  private fun enableFullScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.setDecorFitsSystemWindows(false)
    } else {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
    }
  }

  override fun onStart() {
    super.onStart()
    initializePlayer()
  }

  private fun initializePlayer() {
    player = ExoPlayerFactory.newSimpleInstance(this)
    binding.videoView.player = player
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

  private fun getMediasShareIntent(media: Media): Intent {
    val shareIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_SUBJECT, resources.getText(R.string.send_title))
      type = VIDEO_MIME_TYPE

      val authority = "${applicationContext.packageName}.fileprovider"
      val fileUri = FileProvider.getUriForFile(
        this@VideoPlayerActivity,
        authority,
        media.file
      )

      addCategory(Intent.CATEGORY_OPENABLE)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      putExtra(Intent.EXTRA_STREAM, fileUri)
    }

    return shareIntent
  }
}