package com.example.characters.presentation.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
import com.example.extention.findViewById
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CharactersFragment : Fragment(R.layout.fragment_characters), View.OnClickListener {

    private val vm: CharacterListViewModel by viewModels()

    private var characterNavigator: CharacterNavigator? = null

    private val adapter: CharacterAdapter by lazy {
        CharacterAdapter()
    }
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var progressBarMain: ProgressBar
    private lateinit var imageSearch: ImageView
    private lateinit var linearSearch: LinearLayout
    private lateinit var editText: EditText
    private lateinit var hideSearchButton: ImageButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var informationView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initView()
        initListener()
        initAdapter()
        initSearchEditText()
        initObservers()

    }

    private fun initSearchEditText(){

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

    private fun initView(){

        swipe = findViewById(R.id.swipe)
        recycler = findViewById(R.id.recycler)
        progressBarMain = findViewById(R.id.progressBarMain)
        imageSearch = findViewById(R.id.imageViewSearch)
        linearSearch = findViewById(R.id.linearLayoutSearch)
        editText = findViewById(R.id.editText)
        hideSearchButton = findViewById(R.id.hideSearchButton)
        toolbar = findViewById(R.id.toolbar)
        informationView = findViewById(R.id.informationView)
    }

    private fun initAdapter(){

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadingStateAdapter { adapter.retry() },
            footer = LoadingStateAdapter { adapter.retry() }
        )
    }

    private fun initListener(){

        imageSearch.setOnClickListener(this)
        hideSearchButton.setOnClickListener(this)

        swipe.setOnRefreshListener {
            vm.submitIntent(CharacterListIntent.Refresh)
            swipe.isRefreshing = false
        }

        adapter.setListener {
            characterNavigator?.openCharacterDetails(it.id)
        }
    }

    private fun initObservers(){

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                vm.state.collectLatest { state ->

                    progressBarMain.isVisible = state.isLoading
                    recycler.isVisible = !state.isLoading

                    state.pagingData.let { pagingData ->
                        Log.d("CharactersFragment", "Submitting new paging data")
                        adapter.submitData(pagingData)
                    }

                    val isEmpty = adapter.itemCount == 0
                    informationView.isVisible = !state.isLoading && isEmpty
                    recycler.isVisible = !isEmpty

                    showErrorDialog(state.error)

                }
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

            }

            R.id.hideSearchButton -> {
                // Скрыть строку поиска
                linearSearch.visibility = View.INVISIBLE
                imageSearch.visibility = View.VISIBLE
                toolbar.visibility = View.VISIBLE

            }
        }
    }

    private fun showErrorDialog(message: String?) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun initCharacterNavigator(characterNavigator: CharacterNavigator): CharactersFragment {
        this.characterNavigator = characterNavigator
        return this@CharactersFragment
    }
}

interface CharacterNavigator {
    fun openCharacterDetails(characterId: Int)
}