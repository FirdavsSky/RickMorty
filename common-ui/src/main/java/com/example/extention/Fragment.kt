package com.example.extention

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

inline fun <reified T : View> Fragment.findViewById(@IdRes id: Int): T {
    return requireView().findViewById(id)
}


@MainThread
fun <V : View> Fragment.parseViewById(@IdRes viewId: Int) = lazy(LazyThreadSafetyMode.NONE) {

    view?.findViewById<V>(viewId)
}

fun Fragment.transaction(
    containerId: Int,
    fragment: Fragment,
    isReplace: Boolean = true,
    addToBackStack: Boolean = false,
    tag: String? = null,
    hideAndShowPreviousFragment: Boolean = false,
    enableAnimation: Boolean = false
) {
    requireActivity().supportFragmentManager.transaction(
        containerId = containerId,
        fragment = fragment,
        isReplace = isReplace,
        addToBackStack = addToBackStack,
        tag = tag,
        hideAndShowPreviousFragment = hideAndShowPreviousFragment,
        enableAnimation = enableAnimation
    )
}

fun FragmentManager.transaction(
    containerId: Int,
    fragment: Fragment,
    isReplace: Boolean = true,
    addToBackStack: Boolean = false,
    tag: String? = null,
    hideAndShowPreviousFragment: Boolean = false,
    enableAnimation: Boolean = false
) {
    val transaction = beginTransaction()

    if (enableAnimation) {
        transaction.setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    }

    if (hideAndShowPreviousFragment) {
        val currentFragment = findFragmentById(containerId)
        currentFragment?.let { transaction.hide(it) }
    }

    if (isReplace) transaction.replace(containerId, fragment, tag)
    else transaction.add(containerId, fragment, tag)

    if (addToBackStack) transaction.addToBackStack(tag)

    transaction.commitAllowingStateLoss()
}
