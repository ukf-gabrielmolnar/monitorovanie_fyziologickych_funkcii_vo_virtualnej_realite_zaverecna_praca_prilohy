package com.example.firebaseheartrate_kotlin.presentation.models

import com.google.firebase.database.IgnoreExtraProperties
import java.sql.Timestamp

@IgnoreExtraProperties
data class HR(var heartRate: Float? = null, var timestamp: Long? = null) {

}
