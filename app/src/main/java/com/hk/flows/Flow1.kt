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
    @ExperimentalCoroutinesApi
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

        dataBinding.withTransformOperatpr.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                flowWithTransformOperator()
            }
        }

        dataBinding.flowOn.setOnClickListener {
            runBlocking {
                flowUsingFlowOn()
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
            (2 * request)
        }.filter { request ->
            println("filter $request")
            request % 2 != 0
        }
        flow.collect {
            delay(2000)
            println("flow1 $it")
        }
    }

    /**
     * transform() operator is used to imitate simple transformations like map and filter, as well as implement more complex transformations.
     */
    @ExperimentalCoroutinesApi
    private suspend fun flowWithTransformOperator() {
        (1..3).asFlow()
            .transform { request ->
                emit(request*2)
            }
            .collect {
                println("collect flowWithTransformOperator-$it")
            }
    }

    /**
     * By default the flow lambda function will execute on the same coroutine on which collect is called from. If the collect is called from main thread,
     * then the emit will execute on main thread. ButI If we have to preform high operations on lambda fun of flow{}, so we have to call the lambda on background Thread,
     * we can do it using flowOn() operator. Out collector lambda still run on main thread.But the upstream thread change to IO.
     */
    @ExperimentalCoroutinesApi
    private suspend fun flowUsingFlowOn() {
        flow {
            (1..3).forEach {
                println("flowUsingFlowOn emit-${Thread.currentThread().name}")
                emit(it)
            }
        }
            .map { request ->
                println("flowUsingFlowOn map-${Thread.currentThread().name}")
                (request * 2)
            }
            .flowOn(Dispatchers.IO)
            .collect {
                println("flowUsingFlowOn collect-$it")
                println("flowUsingFlowOn collect-${Thread.currentThread().name}")
            }
    }
}