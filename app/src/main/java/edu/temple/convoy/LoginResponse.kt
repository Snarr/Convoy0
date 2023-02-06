package edu.temple.convoy

data class LoginResponse(
    val status: String,
    val session_key: String,
    val message: String
)
