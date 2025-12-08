package com.example.sharoma_finder.repository

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
                    if (model != null) {
                        lists.add(model)
                    }
                }
                listData.value = Resource.Success(lists)
            }
            override fun onCancelled(error: DatabaseError) {
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
                    model?.let { lists.add(it) }
                }
                listData.value = Resource.Success(lists)
            }
            override fun onCancelled(error: DatabaseError) {
                listData.value = Resource.Error(error.message)
            }
        })
        return listData
    }

    fun loadNearest(id: String, limit: Int? = null): LiveData<Resource<MutableList<StoreModel>>> {
        val listData = MutableLiveData<Resource<MutableList<StoreModel>>>()
        listData.value = Resource.Loading()

        val ref = firebaseDatabase.getReference("Nearest")
        var query: Query = ref.orderByChild("CategoryId").equalTo(id)

        if (limit != null) {
            query = query.limitToFirst(limit)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<StoreModel>()
                for (child in snapshot.children) {
                    val model = child.getValue(StoreModel::class.java)
                    model?.let { lists.add(it) }
                }
                listData.value = Resource.Success(lists)
            }
            override fun onCancelled(error: DatabaseError) {
                listData.value = Resource.Error(error.message)
            }
        })
        return listData
    }
}