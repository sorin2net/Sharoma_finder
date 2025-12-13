package com.example.sharoma_finder.domain

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "subcategories")
data class SubCategoryModel(
    @PrimaryKey
    var Id: Int = 0,
    var CategoryId: String = "",
    var ImagePath: String = "",
    var Name: String = ""
)