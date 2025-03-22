package com.example.greenspace.sharedspace

data class SharedSpace(
    val spaceId: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val owner: String = "",
    val availableSlots: Int = 0,
    val users: List<String> = listOf()
)

