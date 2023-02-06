package edu.temple.convoy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class LoginActivity : AppCompatActivity() {
    lateinit var username: TextView
    lateinit var password: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.usernameTextField)
        password = findViewById(R.id.passwordTextField)

        val loginButton = findViewById<Button>(R.id.submitLoginButton)
        loginButton.setOnClickListener {
            val convoyApi = RetrofitHelper.getInstance().create(Convoy::class.java)
            
            GlobalScope.launch {
                val result = convoyApi.login(getFormData())
                result?.run {
                    Log.d("login: ", this.body().toString())
                }
            }
        }
    }

    fun getFormData(): RequestBody {

        val body: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("action", "LOGIN")
            .addFormDataPart("username", username.text.toString())
            .addFormDataPart("password", password.text.toString())
            .build()

        return body
    }
}