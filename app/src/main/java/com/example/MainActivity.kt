package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.ThemeMode
import com.example.data.repository.ForasniRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.ForasniMainScreen
import com.example.ui.theme.ForasniTheme
import com.example.ui.viewmodel.ForasniViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    // Allow immediate entry into the app without a blocking login screen, while keeping optional Google Sign-In available
    var showOptionalSignInScreen by remember { mutableStateOf(false) }

    val isAuthenticated = user != null
    val currentUserId = user?.uid ?: "guest_jordan_preview"
    val initialDisplayName = user?.displayName ?: user?.email?.substringBefore("@") ?: "زائر فرصني (معاينة كاملة)"

    val forasniViewModel: ForasniViewModel = viewModel(
        key = "$currentUserId-$isAuthenticated",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ForasniViewModel(
                    repository = ForasniRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName,
                    isAuthenticated = isAuthenticated
                )
            }
        }
    )

    val themeMode by forasniViewModel.themeMode.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    ForasniTheme(darkTheme = useDarkTheme) {
        if (showOptionalSignInScreen && user == null) {
            AuthScreen(
                onAuthSuccess = { showOptionalSignInScreen = false }
            )
        } else {
            ForasniMainScreen(
                viewModel = forasniViewModel,
                onSignOut = {
                    if (isAuthenticated) {
                        signOut(
                            context = context,
                            credentialManager = credentialManager,
                            onSignOutComplete = {},
                            scope = scope
                        )
                    } else {
                        showOptionalSignInScreen = true
                    }
                }
            )
        }
    }
}
