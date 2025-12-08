package com.example.sharoma_finder.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ResultsRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadAllStores(): LiveData<Resource<MutableList<StoreModel>>> {
        val listData = MutableLiveData<Resource<MutableList<StoreModel>>>()
        listData.value = Resource.Loading()

        val ref = firebaseDatabase.getReference("Stores")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<StoreModel>()
                var invalidCount = 0

                for (child in snapshot.children) {
                    val model = child.getValue(StoreModel::class.java)

                    // ✅ ADĂUGAT: Validare date
                    if (model != null && model.isValid()) {
                        model.firebaseKey = child.key ?: ""
                        lists.add(model)
                    } else {
                        invalidCount++
                        Log.w("ResultsRepository", "Invalid store data: ${child.key}")
                    }
                }

                Log.d("ResultsRepository", "Loaded ${lists.size} valid stores ($invalidCount invalid)")
                listData.value = Resource.Success(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ResultsRepository", "Error loading stores: ${error.message}")
                listData.value = Resource.Error("Failed to load stores: ${error.message}")
            }
        })
        return listData
    }

    fun loadSubCategory(id: String): LiveData<Resource<MutableList<CategoryModel>>> {
        val listData = MutableLiveData<Resource<MutableList<CategoryModel>>>()
        listData.value = Resource.Loading()
        val ref = firebaseDatabase.getReference("SubCategory")
        val query: Query = ref.orderByChild("CategoryId").equalTo(id)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                for (child in snapshot.children) {
                    val model = child.getValue(CategoryModel::class.java)
                    if (model != null) lists.add(model)
                }
                listData.value = Resource.Success(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ResultsRepository", "Error loading subcategories: ${error.message}")
                listData.value = Resource.Error(error.message)
            }
        })
        return listData
    }

    fun loadPopular(id: String, limit: Int? = null): LiveData<Resource<MutableList<StoreModel>>> {
        val listData = MutableLiveData<Resource<MutableList<StoreModel>>>()
        listData.value = Resource.Loading()

        val ref = firebaseDatabase.getReference("Stores")
        var query: Query = ref.orderByChild("CategoryId").equalTo(id)

        if (limit != null) {
            query = query.limitToFirst(limit)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<StoreModel>()
                for (child in snapshot.children) {
                    val model = child.getValue(StoreModel::class.java)

                    // ✅ ADĂUGAT: Validare
                    if (model != null && model.isValid()) {
                        model.firebaseKey = child.key ?: ""
                        lists.add(model)
                    }
                }
                listData.value = Resource.Success(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ResultsRepository", "Error loading popular stores: ${error.message}")
                listData.value = Resource.Error(error.message)
            }
        })
        return listData
    }

    fun loadNearest(id: String, limit: Int? = null): LiveData<Resource<MutableList<StoreModel>>> {
        return loadPopular(id, limit)
    }
}