package com.example.characters.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
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
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CharactersFragment : Fragment(R.layout.fragment_characters), View.OnClickListener {

    private val vm: CharacterListViewModel by viewModels()

    private lateinit var adapter: CharacterAdapter
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var imageSearch: ImageView
    private lateinit var linearSearch: LinearLayout
    private lateinit var editText: EditText
    private lateinit var hideSearchButton: ImageButton
    private lateinit var toolbar: MaterialToolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipe = view.findViewById(R.id.swipe)
        recycler = view.findViewById(R.id.recycler)
        progress = view.findViewById(R.id.progress)
        imageSearch = view.findViewById(R.id.imageViewSearch)
        linearSearch = view.findViewById(R.id.linearLayoutSearch)
        editText = view.findViewById(R.id.editText)
        hideSearchButton = view.findViewById(R.id.hideSearchButton)
        toolbar = view.findViewById(R.id.toolbar)

        imageSearch.setOnClickListener(this)
        hideSearchButton.setOnClickListener(this)

        swipe.setOnRefreshListener {
            vm.submitIntent(CharacterListIntent.Refresh)
            swipe.isRefreshing = false
        }

        adapter = CharacterAdapter { character ->
            Toast.makeText(requireContext(), "Clicked: ${character.name}", Toast.LENGTH_SHORT).show()
        }

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadingStateAdapter { adapter.retry() },
            footer = LoadingStateAdapter { adapter.retry() }
        )

        // Подписка на состояние
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { state ->
                    progress.isVisible = state.isLoading
                    state.pagingData?.let { pagingData ->
                        Log.d("CharactersFragment", "Submitting new paging data")
                        adapter.submitData(pagingData)
                    }
                }
            }
        }

        // Отправляем поиск при вводе текста (Enter)
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString()
                Log.d("CharactersFragment", "Search triggered with query: $query") // <<< лог
                vm.submitIntent(CharacterListIntent.Search(query))
                true
            } else {
                false
            }
        }

    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.imageViewSearch -> {

                // Показать строку поиска
                imageSearch.visibility = View.GONE
                linearSearch.visibility = View.VISIBLE
                editText.requestFocus()
                toolbar.visibility = View.INVISIBLE

//                showKeyboard()
            }

            R.id.hideSearchButton -> {
                // Скрыть строку поиска
                linearSearch.visibility = View.INVISIBLE
                imageSearch.visibility = View.VISIBLE
                toolbar.visibility = View.VISIBLE

//                hideKeyboard()
            }
        }
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}
