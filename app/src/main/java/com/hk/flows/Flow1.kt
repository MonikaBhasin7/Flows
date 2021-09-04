package com.hk.flows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.hk.flows.databinding.ActivityFlow1Binding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Flow1 : AppCompatActivity() {
    val TAG = "Flow1"

    var flow: Flow<Int> = flow {  }
    lateinit var flowOf: Flow<Int>
    lateinit var dataBinding: ActivityFlow1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,  R.layout.activity_flow1)

        CoroutineScope(Dispatchers.IO).launch {
            setUpFlow().filter { it % 2 == 0 }.map { it * it * it }.collect { t ->
                println("$TAG - $t")
            }
        }

        setUpFlowOf()
        dataBinding.normalFlow.setOnClickListener {
            setUpFlow()
        }

        dataBinding.flowOf.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                flowOf.collect { t ->
                    println("$TAG - $t")
                }
            }
        }

        dataBinding.cancelNormalFlow.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                withTimeoutOrNull(2000, {
                    flow.collect { t ->
                        println("$TAG - $t")
                    }
                })
            }
        }

        dataBinding.withIntermediateOperators.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                flowWithIntermediatesOperators()
            }
        }
    }

    private fun setUpFlow() =
        flow {
            for(i in 1..6) {
                emit(i)
            }
        }



    private fun setUpFlowOf() {
       flowOf(1, 2, 3, 4, 5, 6).flowOn(Dispatchers.Default)
    }


    private suspend fun flowWithIntermediatesOperators() {
        //        val flow = (1..3).asFlow()
        val flow = flow {
            (1..10).forEach {
                println("emit ${it}")
                emit(it)
            }
        }.map { request ->
            println("map $request")
            (1 * request)
        }.filter { request ->
            println("filter $request")
            request != 0
        }
        flow.collect {
            delay(2000)
            println("flow1 $it")
        }
    }
}