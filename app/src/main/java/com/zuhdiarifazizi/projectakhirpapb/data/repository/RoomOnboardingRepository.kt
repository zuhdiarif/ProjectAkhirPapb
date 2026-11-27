package com.zuhdiarifazizi.projectakhirpapb.data.repository

import com.zuhdiarifazizi.projectakhirpapb.data.local.AppMetadataEntity
import com.zuhdiarifazizi.projectakhirpapb.data.local.AuraDatabase
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OnboardingRepository

class RoomOnboardingRepository(private val database: AuraDatabase) : OnboardingRepository {

    override suspend fun isOnboardingCompleted(): Boolean =
        database.appMetadataDao().getValue(ONBOARDING_COMPLETED_KEY) == "true"

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        database.appMetadataDao().upsert(
            AppMetadataEntity(ONBOARDING_COMPLETED_KEY, completed.toString())
        )
    }

    private companion object {
        const val ONBOARDING_COMPLETED_KEY = "onboarding_completed"
    }
}
