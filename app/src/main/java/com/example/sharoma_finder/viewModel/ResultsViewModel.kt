package com.example.sharoma_finder.viewModel

import androidx.lifecycle.LiveData
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.ResultsRepository

class ResultsViewModel {
    private val repository=ResultsRepository()

    fun loadSubCategory(id:String):LiveData<MutableList<CategoryModel>>{
        return repository.loadSubCategory(id)
    }

    fun loadPopular(id:String):LiveData<MutableList<StoreModel>>{
        return repository.loadPopular(id)
    }
    fun loadNearest(id:String):LiveData<MutableList<StoreModel>>{
        return repository.loadNearest(id)
    }
}