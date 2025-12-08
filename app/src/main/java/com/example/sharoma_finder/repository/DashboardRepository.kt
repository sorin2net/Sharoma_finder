package com.example.sharoma_finder.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.BannerModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    item?.let {
                        list.add(it)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // ✅ FIXED: Gestionăm erorile corect
                Log.e("DashboardRepository", "Error loading categories: ${error.message}")
                listData.value = mutableListOf() // Return empty list instead of crash
            }
        })
        return listData
    }

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()
        val ref = firebaseDatabase.getReference("Banners")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(BannerModel::class.java)
                    item?.let {
                        list.add(it)
                    }
                }
                listData.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // ✅ FIXED: Gestionăm erorile corect
                Log.e("DashboardRepository", "Error loading banners: ${error.message}")
                listData.value = mutableListOf() // Return empty list instead of crash
            }
        })
        return listData
    }
}