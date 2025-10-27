package com.example.details.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.details.R
import com.example.details.presentation.intent.CharacterDetailIntent
import com.example.details.presentation.uiState.CharacterDetailState
import com.example.details.presentation.viewModels.CharacterDetailViewModel
import com.example.domain.model.CharacterModel
import com.example.extention.loadImageWithProgress
import com.example.extention.setStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CharacterDetailFragment : Fragment(R.layout.fragment_character_detail) {

    private val viewModel: CharacterDetailViewModel by viewModels()

    private lateinit var ivImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSpecies: TextView
    private lateinit var tvGender: TextView
    private lateinit var progressBar: ProgressBar

    private var characterId: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivImage = view.findViewById(R.id.ivCharacterImage)
        tvName = view.findViewById(R.id.tvName)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvSpecies = view.findViewById(R.id.tvSpecies)
        tvGender = view.findViewById(R.id.tvGender)
        progressBar = view.findViewById(R.id.progressBarItem)

        characterId = arguments?.getInt(ARG_ID) ?: 0

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.state.collect { state ->
                    when (state) {
                        is CharacterDetailState.Idle -> Unit

                        is CharacterDetailState.Loading -> tvName.text = "Loading..."

                        is CharacterDetailState.Success -> {
                            state.character?.let {
                                bindCharacter(it)
                            }

                        }

                        is CharacterDetailState.Error -> tvName.text = "Error: ${state.message}"
                    }
                }
            }
        }
        viewModel.handleIntent(CharacterDetailIntent.LoadCharacter(characterId))
    }

    private fun bindCharacter(character: CharacterModel) {
        tvName.text = character.name
        tvStatus.text = character.status
        tvSpecies.text = "Species: ${character.species}"
        tvGender.text = "Gender: ${character.gender}"

        tvStatus.setStatus(character.status)

        ivImage.loadImageWithProgress(
            character.image,
            progressBar,
            com.example.common_ui.R.drawable.rick_morty_placeholder
        )
    }

    companion object {
        private const val ARG_ID = "character_id"

        fun newInstance(id: Int): CharacterDetailFragment {
            return CharacterDetailFragment().apply {
                arguments = Bundle().apply { putInt(ARG_ID, id) }
            }
        }
    }
}
