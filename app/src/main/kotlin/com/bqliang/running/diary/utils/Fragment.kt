package com.bqliang.running.diary.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

val Fragment.navController
    get() = findNavController()