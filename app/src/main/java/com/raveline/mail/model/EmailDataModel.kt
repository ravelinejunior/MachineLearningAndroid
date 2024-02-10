package com.raveline.mail.model

data class EmailDataModel(
    val id: String,
    val subject: String,
    val content: String,
    val time: Long,
    val color: Long,
    val user: User,
)