package DAO

import kotlinx.serialization.Serializable

@Serializable
data class Genre (
    var id: Int = 0,
    var genre: String = ""
)