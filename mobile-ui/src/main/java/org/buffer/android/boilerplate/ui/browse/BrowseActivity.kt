package org.buffer.android.boilerplate.ui.browse

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_browse.*
import org.buffer.android.boilerplate.presentation.browse.BrowseBufferoosViewModel
import org.buffer.android.boilerplate.presentation.browse.BrowseBufferoosViewModelFactory
import org.buffer.android.boilerplate.presentation.data.ResourceState
import org.buffer.android.boilerplate.presentation.data.Resource
import org.buffer.android.boilerplate.presentation.model.BufferooView
import org.buffer.android.boilerplate.ui.R
import org.buffer.android.boilerplate.ui.mapper.BufferooMapper
import org.buffer.android.boilerplate.ui.util.gone
import org.buffer.android.boilerplate.ui.util.visible
import org.buffer.android.boilerplate.ui.widget.empty.EmptyListener
import org.buffer.android.boilerplate.ui.widget.error.ErrorListener
import javax.inject.Inject

class BrowseActivity: DaggerAppCompatActivity() {

    @Inject lateinit var browseAdapter: BrowseAdapter
    @Inject lateinit var mapper: BufferooMapper
    @Inject lateinit var viewModelFactory: BrowseBufferoosViewModelFactory
    private lateinit var browseBufferoosViewModel: BrowseBufferoosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        browseBufferoosViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(BrowseBufferoosViewModel::class.java)

        setupBrowseRecycler()
        setupViewListeners()
    }

    override fun onStart() {
        super.onStart()
        browseBufferoosViewModel.getBufferoos().observe(this,
                Observer<Resource<List<BufferooView>>> {
                    if (it != null) this.handleDataState(it.status, it.data, it.message) })
    }

    private fun setupBrowseRecycler() {
        recycler_browse.layoutManager = LinearLayoutManager(this)
        recycler_browse.adapter = browseAdapter
    }

    private fun handleDataState(resourceState: ResourceState, data: List<BufferooView>?,
                                message: String?) {
        when (resourceState) {
            ResourceState.LOADING -> setupScreenForLoadingState()
            ResourceState.SUCCESS -> setupScreenForSuccess(data)
            ResourceState.ERROR -> setupScreenForError(message)
        }
    }

    private fun setupScreenForLoadingState() {
        progress.visible()
        recycler_browse.gone()
        view_empty.gone()
        view_error.gone()
    }

    private fun setupScreenForSuccess(data: List<BufferooView>?) {
        view_error.gone()
        progress.gone()
        if (data!= null && data.isNotEmpty()) {
            updateListView(data)
            recycler_browse.visible()
        } else {
            view_empty.visible()
        }
    }

    private fun updateListView(bufferoos: List<BufferooView>) {
        browseAdapter.bufferoos = bufferoos.map { mapper.mapToViewModel(it) }
        browseAdapter.notifyDataSetChanged()
    }

    private fun setupScreenForError(message: String?) {
        progress.gone()
        recycler_browse.gone()
        view_empty.gone()
        view_error.visible()
    }

    private fun setupViewListeners() {
        view_empty.emptyListener = emptyListener
        view_error.errorListener = errorListener
    }

    private val emptyListener = object : EmptyListener {
        override fun onCheckAgainClicked() {
            browseBufferoosViewModel.fetchBufferoos()
        }
    }

    private val errorListener = object : ErrorListener {
        override fun onTryAgainClicked() {
            browseBufferoosViewModel.fetchBufferoos()
        }
    }

}
