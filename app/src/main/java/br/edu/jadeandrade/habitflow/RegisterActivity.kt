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
import br.edu.jadeandrade.habitflow.data.UserEntity
import br.edu.jadeandrade.habitflow.util.PasswordUtils

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        val userDao = AppDatabase.getDatabase(this).userDao()

        tvGoToLogin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty()) {
                etName.error = "Digite seu nome"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Digite um e-mail válido"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "A senha precisa ter pelo menos 6 caracteres"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "As senhas não são iguais"
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            Thread {
                val existingUser = userDao.getByEmail(email)

                if (existingUser != null) {
                    runOnUiThread {
                        btnRegister.isEnabled = true
                        etEmail.error = "Este e-mail já está cadastrado"
                    }
                    return@Thread
                }

                val salt = PasswordUtils.generateSalt()
                val passwordHash = PasswordUtils.hashPassword(password, salt)

                userDao.insert(
                    UserEntity(
                        name = name,
                        email = email,
                        passwordHash = passwordHash,
                        salt = salt
                    )
                )

                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Conta criada! Agora faça login.",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }.start()
        }
    }
}