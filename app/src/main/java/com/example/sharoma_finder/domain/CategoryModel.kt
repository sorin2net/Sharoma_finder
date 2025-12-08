package com.example.sharoma_finder.domain

import androidx.annotation.Keep

@Keep
data class CategoryModel(
    var Id: Int = 0,
    var ImagePath: String = "",
    var Name: String = ""
)