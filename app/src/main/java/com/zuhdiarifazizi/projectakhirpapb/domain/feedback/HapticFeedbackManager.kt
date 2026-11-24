package com.zuhdiarifazizi.projectakhirpapb.domain.feedback

interface HapticFeedbackManager {
    fun performButtonClick()
    fun performCategorySelected()
    fun performSuccess()
    fun performError()
}
