package com.example.sharoma_finder.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.Resource
import com.example.sharoma_finder.repository.ResultsRepository

class ResultsViewModel : ViewModel() {
    private val repository = ResultsRepository()

    fun loadSubCategory(id: String): LiveData<Resource<MutableList<CategoryModel>>> {
        return repository.loadSubCategory(id)
    }

    fun loadPopular(id: String, limit: Int? = null): LiveData<Resource<MutableList<StoreModel>>> {
        return repository.loadPopular(id, limit)
    }

    fun loadNearest(id: String, limit: Int? = null): LiveData<Resource<MutableList<StoreModel>>> {
        return repository.loadNearest(id, limit)
    }
}