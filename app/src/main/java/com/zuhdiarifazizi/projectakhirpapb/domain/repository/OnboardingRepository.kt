package com.zuhdiarifazizi.projectakhirpapb.domain.repository

interface OnboardingRepository {
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
}
