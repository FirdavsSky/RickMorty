package com.example.characters.presentation.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.characters.R
import com.example.characters.presentation.adapters.CharacterAdapter
import com.example.characters.presentation.intents.CharacterListIntent
import com.example.characters.presentation.viewModels.CharacterListViewModel
import com.example.extention.findViewById
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var progressBarFooter: ProgressBar
    private lateinit var imageSearch: ImageView
    private lateinit var linearSearch: LinearLayout
    private lateinit var editText: EditText
    private lateinit var hideSearchButton: ImageButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var informationView: TextView
    private lateinit var fabFilter: FloatingActionButton
    private lateinit var linearLayoutFilters: LinearLayout
    private lateinit var statusFilterSpinner: Spinner
    private lateinit var speciesFilterSpinner: Spinner
    private lateinit var genderFilterSpinner: Spinner
    private lateinit var searchLayout: RelativeLayout

    private val statusApiArray by lazy { resources.getStringArray(R.array.status_options_api) }
    private val speciesApiArray by lazy { resources.getStringArray(R.array.species_options_api) }
    private val genderApiArray by lazy { resources.getStringArray(R.array.gender_options_api) }


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
        progressBarFooter = findViewById(R.id.progressBarFooter)
        imageSearch = findViewById(R.id.imageViewSearch)
        linearSearch = findViewById(R.id.linearLayoutSearch)
        editText = findViewById(R.id.editText)
        hideSearchButton = findViewById(R.id.hideSearchButton)
        toolbar = findViewById(R.id.toolbar)
        informationView = findViewById(R.id.informationView)
        fabFilter = findViewById(R.id.fabFilter)
        linearLayoutFilters = findViewById(R.id.filtersLayout)
        statusFilterSpinner = findViewById(R.id.spinnerStatus)
        speciesFilterSpinner = findViewById(R.id.spinnerSpecies)
        genderFilterSpinner = findViewById(R.id.spinnerGender)
        searchLayout = findViewById(R.id.searchLayout)
    }

    private fun initAdapter(){

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter
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

        fabFilter.setOnClickListener {
            if (linearLayoutFilters.isVisible) {
                linearLayoutFilters.visibility = View.GONE
                searchLayout.visibility = View.VISIBLE
                vm.submitIntent(CharacterListIntent.ApplyFilter(status = null, species = null, gender = null))

            } else {
                linearLayoutFilters.visibility = View.VISIBLE
                searchLayout.visibility = View.GONE
            }
        }

        statusFilterSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.status_options_display)
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        speciesFilterSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.species_options_display)
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        genderFilterSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.gender_options_display)
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val status = statusApiArray[statusFilterSpinner.selectedItemPosition]
                val species = speciesApiArray[speciesFilterSpinner.selectedItemPosition]
                val gender = genderApiArray[genderFilterSpinner.selectedItemPosition]

                vm.submitIntent(CharacterListIntent.ApplyFilter(
                    status = if (status == "all") null else status,
                    species = if (species == "all") null else species,
                    gender = if (gender == "all") null else gender
                ))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        statusFilterSpinner.onItemSelectedListener = listener
        speciesFilterSpinner.onItemSelectedListener = listener
        genderFilterSpinner.onItemSelectedListener = listener
    }

    private fun initObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vm.state.collectLatest { state ->
                        state.pagingData.let { pagingData ->
                            Log.d("CharactersFragment", "Submitting new paging data")
                            adapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                        }

                        state.error?.let {
                            showErrorDialog(it)
                        }
                    }
                }

                launch {
                    adapter.loadStateFlow.collectLatest { loadStates ->
                        val isRefreshing = loadStates.refresh is LoadState.Loading
                        val isAppending = loadStates.append is LoadState.Loading
                        val isError = loadStates.refresh is LoadState.Error
                        val isEmpty = loadStates.refresh is LoadState.NotLoading &&
                                adapter.itemCount == 0

                        // 👇 Обновляем видимость элементов
                        progressBarMain.isVisible = isRefreshing
                        recycler.isVisible = !isRefreshing && !isEmpty
                        informationView.isVisible = isEmpty

                        // 👇 Ошибки при загрузке
                        if (isError) {
                            val error = (loadStates.refresh as LoadState.Error).error
                            showErrorDialog(error.message ?: "Ошибка загрузки данных")
                        }

                        // 👇 Если идёт догрузка (нижний loader)
                        progressBarFooter.isVisible = loadStates.append is LoadState.Loading
                    }
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
                editText.text.clear()
                vm.submitIntent(CharacterListIntent.Search(""))
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