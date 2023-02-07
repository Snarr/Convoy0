package edu.temple.convoy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConvoySession: ViewModel() {
    val convoyId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    fun setConvoyId(id : String) {
        convoyId.value = id
    }
    fun getConvoyId() : LiveData<String> {
        return convoyId
    }

}