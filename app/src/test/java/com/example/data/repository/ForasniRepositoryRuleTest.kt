package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ForasniRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createBooking_and_observeBookings_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = ForasniRepository(firestore)

        val bookingId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBooking(
                providerId = "prov_amman_elec_1",
                providerName = "م. طارق النسور",
                providerSkill = "فني كهرباء منازل",
                providerPhone = "+962795412098",
                serviceTitle = "تركيب لوحة قواطع وإنارة",
                governorate = "عمّان",
                neighborhood = "خلدا",
                scheduledDate = "2026-11-08",
                scheduledTime = "05:00 م",
                escrowAmountJod = 40L,
                useEscrow = true
            ).getOrThrow()
        }
        assertTrue(bookingId.isNotEmpty())

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserBookings(uid).first { list -> list.any { it.id == bookingId } }
        }
        assertTrue(emitted.any { it.id == bookingId && it.escrowStatus == "HELD_IN_ESCROW" })
    }

    @Test
    fun getBookingById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = ForasniRepository(firestore)
        val bookingId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createBooking(
                providerId = "prov_amman_elec_1",
                providerName = "م. طارق النسور",
                providerSkill = "فني كهرباء منازل",
                providerPhone = "+962795412098",
                serviceTitle = "صيانة كهرباء طارئة",
                governorate = "عمّان",
                neighborhood = "عبدون",
                scheduledDate = "2026-11-09",
                scheduledTime = "02:00 م",
                escrowAmountJod = 25L,
                useEscrow = true
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = ForasniRepository(firestore)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getBookingById(bookingId).getOrThrow()
            }
            fail("Expected FirebaseFirestoreException for cross-user booking read")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun observeUserBookings_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = ForasniRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeUserBookings("unauth_uid").first()
            }
            fail("Expected exception when observing bookings while unauthenticated")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@forasni.jo"
        const val BOB_EMAIL = "bob@forasni.jo"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3500L
    }
}
