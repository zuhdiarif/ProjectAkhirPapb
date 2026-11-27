package com.zuhdiarifazizi.projectakhirpapb.domain.repository

interface HapticFeedbackManager {
    fun performButtonClick()
    fun performCategorySelected()
    fun performSuccess()
    fun performError()
}
