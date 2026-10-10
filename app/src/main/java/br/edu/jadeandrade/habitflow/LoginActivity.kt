package br.edu.jadeandrade.habitflow

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.jadeandrade.habitflow.data.AppDatabase
import br.edu.jadeandrade.habitflow.util.PasswordUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val preferences = getSharedPreferences("habitflow_session", MODE_PRIVATE)
        val lastEmail = preferences.getString("last_email", "")
        etEmail.setText(lastEmail)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        val userDao = AppDatabase.getDatabase(this).userDao()

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val icon = etPassword.compoundDrawablesRelative[2]

                if (icon != null &&
                    event.x >= etPassword.width - etPassword.paddingEnd - icon.intrinsicWidth
                ) {
                    val passwordIsHidden =
                        etPassword.transformationMethod is PasswordTransformationMethod

                    if (passwordIsHidden) {
                        etPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()

                        etPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_visibility_off,
                            0
                        )
                    } else {
                        etPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()

                        etPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_visibility,
                            0
                        )
                    }

                    etPassword.setSelection(etPassword.text.length)
                    return@setOnTouchListener true
                }
            }

            false
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Digite um e-mail válido"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Digite sua senha"
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            Thread {
                val user = userDao.getByEmail(email)

                val isValidLogin = user != null &&
                        PasswordUtils.verifyPassword(
                            password,
                            user.passwordHash,
                            user.salt
                        )

                runOnUiThread {
                    btnLogin.isEnabled = true

                    if (isValidLogin && user != null) {
                        preferences.edit().putString("last_email", user.email).apply()
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("user_id", user.id)
                            putExtra("user_name", user.name)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }

                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "E-mail ou senha incorretos.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.start()
        }
    }
}