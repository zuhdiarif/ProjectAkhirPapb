package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OnboardingRepository

class InMemoryOnboardingRepository(
    initialCompleted: Boolean = false
) : OnboardingRepository {

    private var completed = initialCompleted

    override suspend fun isOnboardingCompleted(): Boolean = completed

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        this.completed = completed
    }
}
