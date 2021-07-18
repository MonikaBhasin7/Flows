package com.hk.flows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.hk.flows.databinding.ActivityFlow1Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Flow1 : AppCompatActivity() {
    val TAG = "Flow1"

    lateinit var flow1: Flow<Int>
    lateinit var dataBinding: ActivityFlow1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,  R.layout.activity_flow1)

        setUpFlow()
        dataBinding.normalFlow.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                flow1.collect { t ->
                    println("$TAG - $t")
                }
            }
        }
    }

    private fun setUpFlow() {
        flow1 = flow {
            for(i in 1..6) {
                emit(i)
            }
        }.flowOn(Dispatchers.IO)
    }
}