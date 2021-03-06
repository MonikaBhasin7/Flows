package com.hk.flows

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hk.flows.databinding.ActivityFlow1Binding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


class Flow1 : AppCompatActivity() {
    val TAG = "Flow1"

    var flow: Flow<Int> = flow {  }
    lateinit var flowOf: Flow<Int>
    lateinit var dataBinding: ActivityFlow1Binding
    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_flow1)

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

        dataBinding.flowWithBuffer.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                flowUsingBuffer()
            }
        }

        dataBinding.withConflateOperator.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                forTesting().collect {
                    println("forTesting${it}")
                }
            }
        }

        dataBinding.withCollectLatest.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                flowUsingCollectLatest()
            }
        }

        dataBinding.withFlatMapConcat.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                flowUsingFlatMapConcat()
            }
        }

        dataBinding.withFlatMapLatest.setOnClickListener {
//            CoroutineScope(Dispatchers.IO).launch {
//                flowUsingFlatMapLatest()
//            }

        }

        dataBinding.combineFlow.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                combineFlow()
            }
        }

        dataBinding.flowFirstStep.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                flowFirstStep()
            }
        }

        dataBinding.flowCheckBehaviour.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                a()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            searchFunctionality()
        }
    }

    private fun forTesting() = flow {
        withContext(Dispatchers.IO) {
            emit(1)
        }
    }.flowOn(Dispatchers.IO).catch {

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
                emit(request * 2)
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
                delayFun()
                println("flowUsingFlowOn collect-${Thread.currentThread().name}")
            }
    }

    fun delayFun() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
        }
    }

    /**
     * As flows are sequential, it means jab tak collect ka lambda fun execute nahi ho jaata flow ka lambda fun suspend ho jaata hai and next emit nahi kar paata jab tak collect
     * ke lambda fun ki puri execution naa ho jaaye.
     * But In case of buffer, it internally use the channels. It uses buffer, in which the emitter emit the data without waiting for collector lambda fun to complete.
     * Same as channels.
     */
    @ExperimentalCoroutinesApi
    private suspend fun flowUsingBuffer() {
        flow<Int> {
            (1..10).forEach {
                println("flowUsingBuffer emit-$it")
                emit(it)
            }
        }.buffer(2).collect {
            delay(2000)
            println("flowUsingBuffer collect-$it")
        }
    }

    /**
     * If a collection started processing an emission, it continues until completion. When collector is available to work, if there are more
     * than one emission which does not started to process in collector, only the latest emission processed, The old emissions which has not collected yet are dropped.
     */
    @ExperimentalCoroutinesApi
    private suspend fun flowUsingConflate() {
        flow<Int> {
            (1..10).forEach {
                println("flowUsingConflate emit-$it")
                emit(it)
            }
        }.conflate().collect {
            delay(2000)
            println("flowUsingConflate collect-$it")
        }
    }

    /**
     * Assume that a collection started processing an emission. Before completion of the processing of the emission,
     * if a new emission occurs, collector cancels the processing of current work, collects the latest value, and
     * starts to process it.
     */
    @ExperimentalCoroutinesApi
    private suspend fun flowUsingCollectLatest() {
        flow<Int> {
            (1..10).forEach {
                println("flowUsingCollectLatest emit-$it")
                emit(it)
            }
        }.collectLatest {
            delay(2000)
            println("flowUsingCollectLatest collect-$it")
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun searchFunctionality() {
        dataBinding.etSearchUsingFlow.getQueryTextChangeStateFlow().flatMapLatest {
            doWork(it).catch {
                emit("error while getting data.")
            }
        }.collect {
            println("collect ${it}")
        }
    }

    private suspend fun doWork(it: String): Flow<String> {
        return flow<String> {
            delay(2000)
            emit("$it - response")
        }
    }


    /**
     * There are two flows here. The outer flow executes and emit a value, now it waits for the inner flow to emit its values. Actually, the thing is, the outer flow emit
     * the value, flatMapConcat takes the value that is emitted by the outer flow. The flatMapConcat operator executes its lambda and emit the value, now
     * the collector collect the value and now flatMapConcat executes its suspended lambda jab tak puri definition kahatam nahi ho jaati. Now, the outer lambda emit the next value
     * this flow will work like this.
     *
     * Nested loop(We can say this.)
     *
     * collect - 4
     * collect - 5
     * collect - 8
     * collect - 10
     * collect - 12
     * collect - 15
     */
    @FlowPreview
    private suspend fun flowUsingFlatMapConcat() {
        flow<Int> {
            (1..3).forEach {
                emit(it)
            }
        }.flatMapConcat {
            flow {
                emit(it)
            }
//            returnSameValueOfOuterLambda(it)
//            innerFlowOfMapConcat(it)
        }.collect {
            println("collect - $it")
        }
    }

    private suspend fun returnSameValueOfOuterLambda(i: Int): Flow<Int> {
        return flow {
            emit(i)
        }
    }

    private suspend fun innerFlowOfMapConcat(it: Int) : Flow<Int> {
        return flow {
            emit(it*4)
            emit(it*5)
        }
    }

    /**
     * collect - 4
     * collect - 8
     * collect - 12
     * collect - 15
     * outer flow does not wait for the inner flow to complete before starting to emit the next value.
     * At each emission, the processing of old emissions are cancelled. Only the last emission continues execution.
     */
    @FlowPreview
    private suspend fun flowUsingFlatMapLatest() {
        flow<Int> {
            (1..3).forEach {
                delay(1000)
                emit(it)
            }
        }.flatMapLatest {
            innerFlowOfMapLatest(it)
        }.collect {
            println("collect - $it")
        }
    }

    private suspend fun innerFlowOfMapLatest(it: Int) : Flow<Int> {
        return flow {
            emit(it*4)
            delay(2000)
            emit(it*5)
        }
    }

    private suspend fun combineFlow() {
//        flowOf(1,2,3)
//        val a = flow {
//            delay(1000)
//            emit(100)
//            emit(1000)
//        }
//        val b = flow {
//            //delay(2000)
//            emit(200)
//        }
//        a.combine(b) { aInt, bInt ->
//            "$aInt$bInt"
//        }.collect {
//            println(it)
//        }
        val numbersFlow = innerFlowOfMapLatest(1)
        val lettersFlow = innerFlowOfMapLatest(1)
        numbersFlow.combine(lettersFlow) { number, letter ->
            "$number$letter"
        }.collect {
            println(it)
        }
    }

    private suspend fun flowFirstStep() {
        flow {
            emit(1)
            emit(2)
        }.collectLatest {
            println(it)
            delay(3000)
            println("collect complete")
        }
    }


    private suspend fun a() {
        println("a before")
        h()
        println("after h")
        val job = CoroutineScope(Dispatchers.IO).launch {
            b().collect {
                println("a inside")
            }
        }
        job.join()
        println("a after")
    }

    suspend fun h() {
        println("inside h")
    }


    private suspend fun b(): Flow<String> = channelFlow {
        delay(3000)
        d {
            offer("abd")
            cancel()
        }
        awaitClose()
    }

    private fun d(callback : () -> (Unit)) {
        callback()
    }
}

@ExperimentalCoroutinesApi
fun EditText.getQueryTextChangeStateFlow(): StateFlow<String> {

    val query = MutableStateFlow("")

    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            query.value = s.toString()
        }

        override fun afterTextChanged(s: Editable?) {
        }
    })
    return query
}