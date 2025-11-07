package com.brilliant.movie.common

object Constants {
    const val BASE_URL = "https://movies-app-backend.replit.app/"
    const val TIMEOUT_SECONDS = 30L
    const val CACHE_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB
    const val CACHE_MAX_AGE_MINUTES = 15L
    const val CACHE_MAX_STALE_DAYS = 7L

    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 10
}
