package edu.temple.convoy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val submitSignUpButton = findViewById<Button>(R.id.submitSignUpButton)

        val url = "https://kamorris.com/lab/convoy/account.php"

        submitSignUpButton.setOnClickListener {

        }
    }
}