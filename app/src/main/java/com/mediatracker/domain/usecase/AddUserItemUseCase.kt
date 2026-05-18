package com.mediatracker.domain.usecase

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import javax.inject.Inject

class AddUserItemUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        mediaType: MediaType,
        apiId: String,
        title: String,
        posterUrl: String?,
        status: ItemStatus = ItemStatus.WATCHLIST,
    ): Result<UserItem> = userRepository.addUserItem(mediaType, apiId, title, posterUrl, status)
}
