package com.mediatracker.domain.usecase

import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.model.UserItem
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserItemsUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    fun observeAll(): Flow<List<UserItem>> = userRepository.getUserItemsFlow()

    fun observeByStatus(status: ItemStatus): Flow<List<UserItem>> =
        userRepository.getUserItemsFlow().map { items ->
            items.filter { it.status == status }
        }

    fun observeByType(mediaType: MediaType): Flow<List<UserItem>> =
        userRepository.getUserItemsFlow().map { items ->
            items.filter { it.mediaType == mediaType }
        }

    fun observeByStatusAndType(status: ItemStatus, mediaType: MediaType): Flow<List<UserItem>> =
        userRepository.getUserItemsFlow().map { items ->
            items.filter { it.status == status && it.mediaType == mediaType }
        }
}
