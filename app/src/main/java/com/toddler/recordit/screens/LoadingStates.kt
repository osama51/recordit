package com.toddler.recordit.screens

enum class LoadingStates {
    NONE,
    DOWNLOADING,
    EXTRACTING,
    LOADING,
    DONE,
    NO_NETWORK,
    ERROR_DOWNLOADING,
    ERROR_EXTRACTING,
    ERROR_LOADING
}