package com.toddler.recordit

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.toddler.recordit.screens.LoginScreen
import com.toddler.recordit.ui.theme.RecordItTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private var isSignedIn = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordItTheme {
                ToggleScreen()
            }
        }
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            Log.i(TAG, "onCreate: result = $result")
            if(result.resultCode == RESULT_OK){
                Log.i(TAG, "onCreate: result is ok")
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleTask(task)
            } else {
                Log.e(TAG, "onCreate: result is not ok")
            }

        }

        val application = application as MyApplication
        firebaseAuth = application.firebaseAuth

        val packageName = application.packageName
        sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        isSignedIn = firebaseAuth.currentUser != null


        if (isSignedIn) {
            val userName = firebaseAuth.currentUser?.displayName
            Toast.makeText(this, "Welcome $userName", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        } else {
            Toast.makeText(this, "Not Signed In", Toast.LENGTH_SHORT).show()
            // request google sign in
            // startActivity(Intent(this, GoogleSignInActivity::class.java))

        }
        Log.d(TAG, "onCreate: isSignedIn = $isSignedIn")

    }

    @Composable
    fun ToggleScreen() {
//        val firebaseAuth = remember { mutableStateOf(FirebaseAuth.getInstance()) }
//        val isSignedIn = remember { mutableStateOf(firebaseAuth.value.currentUser != null) }

        LoginScreen(
            googleSignIn = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        )
//    }
    }


    private fun handleTask(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            Log.i(TAG, "handleTask: task is successful")
            val account = task.result
            if(account != null){
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
                    if(it.isSuccessful){
                        val idToken = account.idToken
                        val accountId = account.id
                        val uid = firebaseAuth.currentUser?.uid // safer than account.id and more simple than account.idToken
//                        Log.i(TAG, "handleTask: idToken = $idToken")
//                        Log.i(TAG, "handleTask: accountId = $accountId")
//                        Log.i(TAG, "handleTask: uid = $uid")
                        sharedPreferences.edit().putString("uid", uid).apply()
                        Toast.makeText(this, "Welcome ${account.displayName}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Log.d(TAG, "firebaseAuthWithGoogle:" + account?.id)
            // firebaseAuthWithGoogle(account?.idToken!!)
        } else {
            Log.e(TAG, "Google sign in failed", task.exception)
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
        const val SERVER_CLIENT_ID =
            "255061443619-r2jbi60k39hoa339hfhuvj49j4g7npto.apps.googleusercontent.com" // web
//            "255061443619-u6iamippqpengf3tlvj0h241nmmc2hav.apps.googleusercontent.com" // android
    }
}