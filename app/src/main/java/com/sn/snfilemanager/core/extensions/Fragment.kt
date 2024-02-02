package com.sn.snfilemanager.core.extensions

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

fun Fragment.getNavigationResult(key: String = "result") =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(key)

fun Fragment.setNavigationResult(
    result: String,
    key: String = "result",
) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

fun Fragment.removeKey(key: String) {
    findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>(key)
}
