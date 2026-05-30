package com.mediatracker.domain.usecase

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.mediatracker.data.review.ReviewPreferencesRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestInAppReviewUseCase @Inject constructor(
    private val reviewPreferences: ReviewPreferencesRepository,
) {
    suspend operator fun invoke(activity: Activity) {
        try {
            if (!reviewPreferences.shouldShowReview()) return
            val manager = ReviewManagerFactory.create(activity)
            val reviewInfo = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, reviewInfo).await()
            reviewPreferences.recordReviewPrompt()
            Timber.i("In-App Review completed")
        } catch (e: Exception) {
            Timber.w(e, "In-App Review failed")
        }
    }

    suspend fun maybeQueueReviewTrigger() {
        reviewPreferences.incrementCompleted()
    }
}
