package com.ouattararomuald.statussaver.home.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.ouattararomuald.statussaver.Media
import com.ouattararomuald.statussaver.R
import com.ouattararomuald.statussaver.core.FileHelper
import com.ouattararomuald.statussaver.core.mimeType
import com.ouattararomuald.statussaver.databinding.ActivityHomeBinding
import com.ouattararomuald.statussaver.home.adapters.HomePagesAdapter
import com.ouattararomuald.statussaver.home.models.Page
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.home.presenters.HomePresenter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.OutputStream
import kotlin.math.abs

class HomeActivity : AppCompatActivity(), HomeContract.HomeView,
    EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

  companion object {
    private const val OPEN_DIRECTORY_REQ_CODE = 0xace
    private const val READ_EXTERNAL_STORAGE_REQ_CODE = 0xcafe
  }

  private lateinit var binding: ActivityHomeBinding

  private var tabLayoutMediator: TabLayoutMediator? = null

  lateinit var presenter: HomeContract.HomePresenter

  private var clearOptionMenuItem: MenuItem? = null
  private var saveOptionMenuItem: MenuItem? = null
  private var refreshOptionMenuItem: MenuItem? = null

  private var fileHelper = FileHelper()

  private val mediasToWrite: MutableList<Media> = mutableListOf()

  private val sharedPrefs: SharedPreferences by lazy {
    getSharedPreferences(getString(R.string.shared_preferences_key), Context.MODE_PRIVATE)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.authorizeButton.setOnClickListener {
      requestPermissions()
    }

    saveLastLaunchDate()

    presenter = HomePresenter(this, this)

    displayViewBasedOnAuthorizations()
  }

  private fun saveLastLaunchDate() {
    val defaultDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
    val todayDate = sharedPrefs.getString(
      getString(R.string.today_launch_date_key),
      defaultDate
    )
    sharedPrefs.edit().putString(
      getString(R.string.last_launch_date_key),
      todayDate
    ).apply()
    sharedPrefs.edit().putString(
      getString(R.string.today_launch_date_key),
      defaultDate
    ).apply()
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
    } else if (requestCode == OPEN_DIRECTORY_REQ_CODE) {
      if (resultCode == Activity.RESULT_OK) {
          val uri = data?.data
          if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            sharedPrefs.edit().putString(getString(R.string.save_dir_key),"$uri").apply()

            getDocumentFile(uri)?.let { writeFiles(it) }
        }
      }
    }
  }

  private fun writeFiles(documentFile: DocumentFile) {
    val outputStreams = mutableListOf<OutputStream>()
    val inputFiles = mutableListOf<File>()

    mediasToWrite.forEach { media ->
      val fileToWrite = documentFile.createFile(media.mimeType(), media.file.name)
      fileToWrite?.let {
        val stream = contentResolver.openOutputStream(it.uri)
        stream?.let {
          outputStreams.add(stream)
          inputFiles.add(media.file)
        }
      }
    }

    fileHelper.writeFiles(outputStreams, inputFiles) { numberOfSuccess ->
      val numberOfFails = abs(mediasToWrite.size - numberOfSuccess)

      if (numberOfSuccess == mediasToWrite.size - 1) {
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
      mediasToWrite.clear()
    }
  }

  private fun getDocumentFile(uri: Uri): DocumentFile? {
    return DocumentFile.fromTreeUri(this, uri)
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
  /**
   * Saves the given [medias] into shared storage.
   *
   * @param medias the file to save.
   */
  override fun saveFiles(medias: List<Media>) {
    createFiles(medias)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createFiles(medias: List<Media>) {
    mediasToWrite.clear()
    mediasToWrite.addAll(medias)

    if (sharedPrefs.contains(getString(R.string.save_dir_key))) {
      val uri = Uri.parse(sharedPrefs.getString(getString(R.string.save_dir_key), "")!!)
      if (canWriteInDocumentTree(uri)) {
        val documentFile = getDocumentFile(uri)
        documentFile?.let { writeFiles(it) }
      } else {
        requestDocumentTreePermission()
      }
    } else {
      requestDocumentTreePermission()
    }
  }

  private fun requestDocumentTreePermission() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
      flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
          Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
    startActivityForResult(intent, OPEN_DIRECTORY_REQ_CODE)
  }

  private fun canWriteInDocumentTree(uri: Uri): Boolean {
    val documentFile = getDocumentFile(uri)
    return documentFile?.canWrite() ?: false
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
    }.also { it.attach() }
    //tabLayoutMediator?.attach()
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