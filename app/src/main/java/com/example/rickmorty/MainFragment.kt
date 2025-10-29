package com.example.rickmorty

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.characters.presentation.fragments.CharacterNavigator
import com.example.characters.presentation.fragments.CharactersFragment
import com.example.details.presentation.fragments.CharacterDetailFragment
import com.example.extention.transaction

class MainFragment : Fragment(R.layout.fragment_main_fragment),CharacterNavigator {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {

            transaction(
                containerId = R.id.mainFragmentContainerView,
                fragment = CharactersFragment().initCharacterNavigator(this@MainFragment),
                isReplace = true,
                addToBackStack = false
            )
        }
    }

    override fun openCharacterDetails(characterId: Int) {
        transaction(
            containerId = R.id.mainFragmentContainerView,
            fragment = CharacterDetailFragment.newInstance(
                characterId
            ),
            isReplace = true,
            addToBackStack = true
        )
    }
}