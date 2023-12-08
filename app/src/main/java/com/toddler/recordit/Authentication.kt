package com.toddler.recordit

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


fun ComponentActivity.oneTapGoogleSignIn(
    oneTapClient: SignInClient,
    signInRequest: BeginSignInRequest,
    signUpRequest: BeginSignInRequest,
    firebaseAuth: FirebaseAuth,
    isSignedIn: MutableState<Boolean>
) {
    oneTapClient.beginSignIn(signInRequest)
        .addOnSuccessListener(this) { result ->
            performAuthentication(oneTapClient, result, firebaseAuth, isSignedIn)
        }.addOnFailureListener(this) {
            oneTapSignUp(oneTapClient, signUpRequest, firebaseAuth, isSignedIn)
            Log.e("Authentication", "Error: ${it.message}")
        }
}

private fun ComponentActivity.oneTapSignUp(
    oneTapClient: SignInClient,
    signUpRequest: BeginSignInRequest,
    firebaseAuth: FirebaseAuth,
    isSignedIn: MutableState<Boolean>
) {
    oneTapClient.beginSignIn(signUpRequest)
        .addOnSuccessListener(this) { result ->
            performAuthentication(oneTapClient, result, firebaseAuth, isSignedIn)
        }.addOnFailureListener(this) {
            Log.e("Authentication", "Error: ${it.message}")
        }
}

private fun ComponentActivity.performAuthentication(
    oneTapClient: SignInClient,
    result: com.google.android.gms.auth.api.identity.BeginSignInResult,
    firebaseAuth: FirebaseAuth,
    isSignedIn: MutableState<Boolean>
) {
    try {
        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    try {
                        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                        val idToken = credential.googleIdToken
                        loginWithFirebase(idToken, firebaseAuth, isSignedIn)
                    } catch (apiException: ApiException) {
                        when (apiException.statusCode) {
                            CommonStatusCodes.CANCELED -> {
                                Log.d("Authentication", "One-tap dialog was closed.")
                            }

                            CommonStatusCodes.NETWORK_ERROR -> {
                                Toast.makeText(
                                    this,
                                    "Make sure there's internet connection",
                                    Toast.LENGTH_LONG
                                ).show()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                                } else {
                                    Log.d("Authentication", "One-tap encountered a network error.")
                                }
                                Log.d("Authentication", "One-tap encountered a network error.")
                            }

                            else -> {
                                Log.d(
                                    "Authentication",
                                    "Couldn't get credential from result. ${apiException.message}"
                                )
                            }
                        }

                    }

                }
            }
        // No credential is available; launch sign-in UI.
        startForResult.launch(
            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        )
    } catch (intentSenderException: IntentSender.SendIntentException) {
        Log.e("Authentication", "Couldn't start One-tap UI: ${intentSenderException.message}")
    }
}

private fun loginWithFirebase(
    idToken: String?,
    firebaseAuth: FirebaseAuth,
    isSignedIn: MutableState<Boolean>
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    firebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isSignedIn.value = true
                Log.d("Authentication", "signInWithCredential:success")
            } else {
                Log.e("Authentication", "Error: ${task.exception?.message}")
            }
        }
}