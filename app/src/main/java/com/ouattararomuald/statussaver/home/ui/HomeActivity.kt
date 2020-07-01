package com.ouattararomuald.statussaver.home.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.databinding.ActivityHomeBinding
import com.ouattararomuald.statussaver.home.adapters.HomePagesAdapter
import com.ouattararomuald.statussaver.home.models.Page
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.home.presenters.HomePresenter
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class HomeActivity : AppCompatActivity(), HomeContract.HomeView,
  EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

  companion object {
    private const val RC_READ_EXTERNAL_STORAGE = 0xcafe
  }

  private lateinit var binding: ActivityHomeBinding

  private var tabLayoutMediator: TabLayoutMediator? = null

  lateinit var presenter: HomeContract.HomePresenter

  private var clearOptionMenuItem: MenuItem? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    presenter = HomePresenter(this, this)
    requestPermissions()
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
  }

  override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
      AppSettingsDialog.Builder(this).build().show()
    }
  }

  override fun onRationaleAccepted(requestCode: Int) {
  }

  override fun onRationaleDenied(requestCode: Int) {
    finish()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
      // Do something after user returned from app settings screen, like showing a Toast.
      Toast.makeText(this, "R.string.returned_from_app_settings_to_activity", Toast.LENGTH_SHORT)
        .show()
    }
  }

  @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
  private fun requestPermissions() {
    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    if (EasyPermissions.hasPermissions(this, *permissions)) {
      presenter.discoverStatuses()
    } else {
      EasyPermissions.requestPermissions(
        this, getString(R.string.read_external_storage_rationale),
        RC_READ_EXTERNAL_STORAGE, *permissions
      )
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.home, menu)
    if (menu != null) {
      clearOptionMenuItem = menu.getItem(0)
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
  }

  override fun showClearOptionMenu() {
    clearOptionMenuItem?.isVisible = true
  }
}