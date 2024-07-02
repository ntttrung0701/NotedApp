package com.example.appquizlet.model

import com.google.gson.annotations.SerializedName

class UpdateUserResponse(
    @SerializedName("userName") val userName: String? = "",
    @SerializedName("email") val email: String? = "",
    @SerializedName("avatar") val avatar: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = "",
    @SerializedName("setting") val setting: UserSetting? = null
) {
}

data class UserSetting(
    val darkMode: Boolean,
    val notification: Boolean
)