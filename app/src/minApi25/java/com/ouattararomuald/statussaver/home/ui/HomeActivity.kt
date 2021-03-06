package com.ouattararomuald.statussaver.home.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class HomeActivity : AppCompatActivity(), HomeContract.HomeView,
    EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

  companion object {
    private const val READ_EXTERNAL_STORAGE_REQ_CODE = 0xcafe
  }

  private lateinit var binding: ActivityHomeBinding

  private var tabLayoutMediator: TabLayoutMediator? = null

  lateinit var presenter: HomeContract.HomePresenter

  private var clearOptionMenuItem: MenuItem? = null
  private var saveOptionMenuItem: MenuItem? = null
  private var refreshOptionMenuItem: MenuItem? = null

  private var fileHelper = FileHelper()

  override fun onCreate(savedInstanceState: Bundle?) {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
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

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
  }

  override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    presenter.discoverStatuses()
    hideAuthorizationViews()
  }

  override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    showAuthorizationViews()
    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
      AppSettingsDialog.Builder(this).build().show()
    }
  }

  override fun onRationaleAccepted(requestCode: Int) {
    hideAuthorizationViews()
  }

  override fun onRationaleDenied(requestCode: Int) {
    showAuthorizationViews()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
      displayViewBasedOnAuthorizations()
    }
  }

  private fun hasRequiredPermissions(): Boolean {
    val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    return EasyPermissions.hasPermissions(this, *permissions)
  }

  @AfterPermissionGranted(READ_EXTERNAL_STORAGE_REQ_CODE)
  private fun requestPermissions() {
    val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    if (EasyPermissions.hasPermissions(this, *permissions)) {
      presenter.discoverStatuses()
    } else {
      EasyPermissions.requestPermissions(
          this, getString(R.string.read_external_storage_rationale),
          READ_EXTERNAL_STORAGE_REQ_CODE, *permissions
      )
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
      R.id.save_item -> {
        presenter.onSaveOptionMenuItemClicked()
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