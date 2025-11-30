package com.example.sharoma_finder.viewModel

import androidx.lifecycle.LiveData
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel

class DashboardViewModel {

    private val repository=DashboardViewModel()

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }

    fun loadBanner():LiveData<MutableList<BannerModel>>{
        return repository.loadBanner()
    }
}