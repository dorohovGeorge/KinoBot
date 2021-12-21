package DAO

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    var id: Int = 0,
    var country: String = ""
)