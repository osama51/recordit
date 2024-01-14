package com.toddler.recordit.screens

enum class LoadingStates {
    NONE,
    DOWNLOADING,
    EXTRACTING,
    LOADING,
    DONE,
    ERROR_DOWNLOADING,
    ERROR_EXTRACTING,
    ERROR_LOADING
}