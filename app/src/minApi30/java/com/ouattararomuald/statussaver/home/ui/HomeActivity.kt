package com.ouattararomuald.statussaver.home.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.core.FileHelper
import com.ouattararomuald.statussaver.databinding.ActivityHomeBinding
import com.ouattararomuald.statussaver.home.adapters.HomePagesAdapter
import com.ouattararomuald.statussaver.home.models.Page
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.home.presenters.HomePresenter
import kotlin.math.abs

class HomeActivity : AppCompatActivity(), HomeContract.HomeView {

  companion object {
    private const val MANAGE_ALL_FILES_ACCESS_REQ_CODE = 0xface
  }

  private lateinit var binding: ActivityHomeBinding

  private var tabLayoutMediator: TabLayoutMediator? = null

  lateinit var presenter: HomeContract.HomePresenter

  private var clearOptionMenuItem: MenuItem? = null
  private var saveOptionMenuItem: MenuItem? = null
  private var refreshOptionMenuItem: MenuItem? = null

  private var fileHelper = FileHelper()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.authorizeButton.setOnClickListener {
      requestPermissions()
    }

    presenter = HomePresenter(this, this)

    displayViewBasedOnAuthorizations()
  }

  private fun displayViewBasedOnAuthorizations() {
    if (hasRequiredPermissions()) {
      hideAuthorizationViews()
      presenter.discoverStatuses()
    } else {
      showAuthorizationViews()
    }
  }

  private fun showAuthorizationViews() {
    binding.tabLayout.isVisible = false
    binding.pager.isVisible = false
    binding.authorizationTextView.isVisible = true
    binding.authorizeButton.isVisible = true
    refreshOptionMenuItem?.isEnabled = false
  }

  private fun hideAuthorizationViews() {
    binding.tabLayout.isVisible = true
    binding.pager.isVisible = true
    binding.authorizationTextView.isVisible = false
    binding.authorizeButton.isVisible = false
    refreshOptionMenuItem?.isEnabled = true
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == MANAGE_ALL_FILES_ACCESS_REQ_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      displayViewBasedOnAuthorizations()
    }
  }

  private fun hasRequiredPermissions(): Boolean = Environment.isExternalStorageManager()

  private fun requestPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
      startActivityForResult(intent, MANAGE_ALL_FILES_ACCESS_REQ_CODE)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.home, menu)
    if (menu != null) {
      clearOptionMenuItem = menu.findItem(R.id.clear_item)
      saveOptionMenuItem = menu.findItem(R.id.save_item)
      refreshOptionMenuItem = menu.findItem(R.id.refresh_item)
      refreshOptionMenuItem?.isEnabled = hasRequiredPermissions()
    }
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.clear_item -> {
        presenter.onClearOptionMenuItemClicked()
        true
      }
      R.id.share_item -> {
        presenter.onShareOptionMenuItemClicked()
        true
      }
      R.id.refresh_item -> {
        presenter.refreshData()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun getContext(): Context = this

  override fun displayPages(pages: Array<Page>) {
    binding.pager.adapter = HomePagesAdapter(pages, this)
    tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
      tab.text = pages[position].title
    }
    tabLayoutMediator?.attach()
  }

  override fun openChooserForIntent(shareIntent: Intent) {
    startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
  }

  override fun hideClearOptionMenu() {
    clearOptionMenuItem?.isVisible = false
    saveOptionMenuItem?.isVisible = false
  }

  override fun showClearOptionMenu() {
    clearOptionMenuItem?.isVisible = true
    saveOptionMenuItem?.isVisible = true
  }

  override fun saveFiles(medias: List<Media>) {
    val files = medias.map { it.file }
    fileHelper.writeFiles(files) { numberOfSuccess ->
      val numberOfFails = abs(files.size - numberOfSuccess)

      if (numberOfSuccess == files.size - 1) {
        displaySuccessMessage(numberOfSuccess)
      } else {
        if (numberOfSuccess > 0) {
          displaySuccessMessage(numberOfSuccess)
        }
        if (numberOfFails > 0) {
          displayFailureMessage(numberOfFails)
        }
      }

      presenter.onClearOptionMenuItemClicked()
    }
  }

  private fun displaySuccessMessage(numberOfFilesSaved: Int) {
    Snackbar.make(
      binding.root,
      resources.getQuantityString(R.plurals.numberOfFilesSuccessfullySave, numberOfFilesSaved, numberOfFilesSaved),
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color, theme)).show()
  }

  private fun displayFailureMessage(numberOfFailure: Int) {
    Snackbar.make(
      binding.root,
      resources.getQuantityString(R.plurals.numberOfFilesSaveFailure, numberOfFailure, numberOfFailure),
      Snackbar.LENGTH_LONG
    ).setTextColor(resources.getColor(R.color.snackbar_text_color_failure, theme)).show()
  }
}