package com.example.characters.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.characters.R
import com.example.characters.presentation.adapters.CharacterAdapter
import com.example.characters.presentation.adapters.LoadingStateAdapter
import com.example.characters.presentation.intents.CharacterListIntent
import com.example.characters.presentation.viewModels.CharacterListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CharactersFragment : Fragment(R.layout.fragment_characters) {

    private val vm: CharacterListViewModel by viewModels()

    private lateinit var adapter: CharacterAdapter
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipe = view.findViewById(R.id.swipe)
        recycler = view.findViewById(R.id.recycler)
        progress = view.findViewById(R.id.progress)

        adapter = CharacterAdapter { character ->
            // navigate to details: findNavController().navigate(...)
        }
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadingStateAdapter { adapter.retry() },
            footer = LoadingStateAdapter { adapter.retry() }
        )

        // add a load-state listener to help debug whether Paging is requesting/receiving data
        adapter.addLoadStateListener { loadState ->
            android.util.Log.d("CharactersFragment", "Paging loadState: $loadState")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { state ->
                    progress.isVisible = state.isLoading

                    state.pagingData.let { pagingData ->
                        Log.d("CharactersFragment", "Submitting new paging data")
                        adapter.submitData(pagingData)
                    }
                }
            }
        }

        swipe.setOnRefreshListener {
            vm.submitIntent(CharacterListIntent.Refresh)
            swipe.isRefreshing = false
        }

        // SearchView example (toolbar) -> vm.submitIntent(CharacterListIntent.Search(query))
    }
}
