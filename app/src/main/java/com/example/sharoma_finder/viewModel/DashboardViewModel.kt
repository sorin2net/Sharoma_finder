package com.example.sharoma_finder.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.repository.DashboardRepository

class DashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        return repository.loadCategory()
    }

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        return repository.loadBanner()
    }
}