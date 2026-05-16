package com.mediatracker.data.firestore

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthProvider @Inject constructor(
    private val auth: FirebaseAuth?,
) {
    val userId: String?
        get() = auth?.currentUser?.uid
}
