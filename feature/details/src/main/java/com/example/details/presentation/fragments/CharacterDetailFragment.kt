package com.example.details.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.conts.CharacterColors
import com.conts.CharacterStrings
import com.example.details.R
import com.example.details.presentation.intent.CharacterDetailIntent
import com.example.details.presentation.uiState.CharacterDetailState
import com.example.details.presentation.viewModels.CharacterDetailViewModel
import com.example.domain.model.CharacterModel
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

    private var characterId: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivImage = view.findViewById(R.id.ivCharacterImage)
        tvName = view.findViewById(R.id.tvName)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvSpecies = view.findViewById(R.id.tvSpecies)
        tvGender = view.findViewById(R.id.tvGender)

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

        val color = when (character.status) {
            CharacterStrings.STATUS_ALIVE -> ContextCompat.getColor(requireContext(), CharacterColors.STATUS_ALIVE)
            CharacterStrings.STATUS_DEAD -> ContextCompat.getColor(requireContext(), CharacterColors.STATUS_DEAD)
            else  -> ContextCompat.getColor(requireContext(), CharacterColors.STATUS_UNKNOWN)
        }
        tvStatus.setTextColor(color)

        Glide.with(requireContext())
            .load(character.image)
            .placeholder(com.example.common_ui.R.drawable.rick_morty_placeholder)
            .into(ivImage)
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
