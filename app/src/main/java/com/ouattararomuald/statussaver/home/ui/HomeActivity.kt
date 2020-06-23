package com.ouattararomuald.statussaver.home.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.ouattararomuald.statussaver.databinding.ActivityHomeBinding
import com.ouattararomuald.statussaver.home.adapters.HomePagesAdapter
import com.ouattararomuald.statussaver.home.models.Page
import com.ouattararomuald.statussaver.home.presenters.HomeContract
import com.ouattararomuald.statussaver.home.presenters.HomePresenter

class HomeActivity : AppCompatActivity(), HomeContract.HomeView {

  private lateinit var binding: ActivityHomeBinding

  private var tabLayoutMediator: TabLayoutMediator? = null

  lateinit var presenter: HomeContract.HomePresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityHomeBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    presenter = HomePresenter(this, this)
    presenter.start()
  }

  override fun getContext(): Context = this

  override fun displayPages(pages: Array<Page>) {
    binding.pager.adapter = HomePagesAdapter(pages, this)
    tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
      tab.text = pages[position].title
    }
    tabLayoutMediator?.attach()
  }
}