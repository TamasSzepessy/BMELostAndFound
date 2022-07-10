package hu.bme.aut.android.bmelostandfound

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.bmelostandfound.databinding.ActivityLoginBinding
import hu.bme.aut.android.bmelostandfound.extensions.validateNonEmpty

class LoginActivity : BaseActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkNetwork()) {
            firebaseAuth = FirebaseAuth.getInstance()

            if (firebaseAuth.currentUser != null) {
                // User is signed in (getCurrentUser() will be null if not signed in)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.btnRegister.setOnClickListener { registerClick() }
            binding.btnLogin.setOnClickListener { loginClick() }
        } else {
            toast(getString(R.string.no_connection))

            binding.btnRegister.setOnClickListener { toast(getString(R.string.no_connection)) }
            binding.btnLogin.setOnClickListener { toast(getString(R.string.no_connection))}
        }

        binding.btnContacts.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }
    }

    private fun validateForm() =
        binding.etEmail.validateNonEmpty() && binding.etPassword.validateNonEmpty()

    private fun registerClick() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun loginClick() {
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        firebaseAuth
            .signInWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
            .addOnSuccessListener {
                hideProgressDialog()

                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()

                toast(exception.localizedMessage)
            }
    }

    private fun checkNetwork(): Boolean {
        var info: NetworkInfo? = null
        val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        info = connectivity.activeNetworkInfo
        return info != null
    }
}