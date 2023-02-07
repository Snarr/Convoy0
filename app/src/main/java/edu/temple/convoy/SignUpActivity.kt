package edu.temple.convoy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class SignUpActivity : AppCompatActivity() {
    lateinit var username: TextView
    lateinit var firstName: TextView
    lateinit var lastName: TextView
    lateinit var password: TextView
    lateinit var confirmPassword: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        username = findViewById(R.id.usernameTextField)
        firstName = findViewById(R.id.nameTextField)
        lastName = findViewById(R.id.nameTextField)
        password = findViewById(R.id.passwordTextField)
        confirmPassword = findViewById(R.id.confirmPasswordTextField)

        val submitSignUpButton = findViewById<Button>(R.id.submitSignUpButton)

        submitSignUpButton.setOnClickListener {
            if (!passwordsMatch()) {
                Toast.makeText(this, "Make sure the passwords match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val convoyApi = RetrofitHelper.getInstance().create(Convoy::class.java)
            // Switch to different scope later on?
            GlobalScope.launch {
                val result = convoyApi.signUp(getFormData())
                result.run {
                    Log.d("signUp: ", this.body().toString())
                    if (this.body()?.status == "SUCCESS") {
                        val intent = Intent(this@SignUpActivity, MapsActivity::class.java)
                        intent.putExtra("session_key", this.body()?.session_key)
                        intent.putExtra("username", username.text.toString())
                        startActivity(intent)
                        finish()
                    }
                }
                // Checking the results
            }
        }
    }

    fun passwordsMatch(): Boolean {
        return password.text.toString() == confirmPassword.text.toString()
    }

    fun getFormData(): RequestBody {

        val body: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("action", "REGISTER")
            .addFormDataPart("username", username.text.toString())
            .addFormDataPart("firstname", firstName.text.toString())
            .addFormDataPart("lastname", lastName.text.toString())
            .addFormDataPart("password", password.text.toString())
            .build()

        return body
    }
}