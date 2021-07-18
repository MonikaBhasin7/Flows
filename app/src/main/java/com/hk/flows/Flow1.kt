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

    lateinit var flow: Flow<Int>
    lateinit var flowOf: Flow<Int>
    lateinit var dataBinding: ActivityFlow1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,  R.layout.activity_flow1)

        setUpFlow()
        setUpFlowOf()
        dataBinding.normalFlow.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                flow.collect { t ->
                    println("$TAG - $t")
                }
            }
        }

        dataBinding.flowOf.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                flowOf.collect { t ->
                    println("$TAG - $t")
                }
            }
        }
    }

    private fun setUpFlow() {
        flow = flow {
            for(i in 1..6) {
                emit(i)
            }
        }.flowOn(Dispatchers.IO)
    }


    private fun setUpFlowOf() {
       flowOf(1, 2, 3, 4, 5, 6).flowOn(Dispatchers.Default)
    }
}