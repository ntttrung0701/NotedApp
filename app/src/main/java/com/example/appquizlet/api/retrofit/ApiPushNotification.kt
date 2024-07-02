package com.example.appquizlet.api.retrofit

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiPushNotification {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAARXQOT2I:APA91bE2gz0e-e3FkKfk9Usv0GU94XcmWzhATTsAwhw5ykttQUa7boDCiH0I9tz1FSjwmpZT2lHL0z6YO0oX89wYswbJ9mUH2DiHNQusslHzGzLNVqjEXzPElj-ONGchvQQ9bonVWgkF"
    )
    @POST("fcm/send")
    suspend fun sendNotificationToServer(@Body notificationData: NotificationData)
}

data class NotificationData(
    val to: String, // FCM token of the target device
    val notification: Notification,
    val data: Map<String, String> // Additional data payload
)

data class Notification(
    val title: String,
    val body: String
)
