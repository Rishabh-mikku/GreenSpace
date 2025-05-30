package com.greenspaceapp.greenspace.sharedspace

data class SharedSpace(
    val spaceId: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val owner: String = "",
    var availableSlots: Int = 0,
    val users: List<Any> = listOf()
)

