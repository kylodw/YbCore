package com.zhilian.base.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.Closeable


interface ActionDispatcher<A> {
    fun dispatchAction(action: A)
}

interface EffectDispatcher<E> {
    fun dispatchEffect(effect: E)
}

interface SuspendingEffectDispatcher<E> {
    suspend fun dispatchEffect(effect: E)
}

/**
 * 普通分发器
 */
interface Dispatcher<A, E> : ActionDispatcher<A>, EffectDispatcher<E>

/**
 * 携程分发器
 */
interface SuspendingDispatcher<A, E> : ActionDispatcher<A>, SuspendingEffectDispatcher<E>


class ScopedDispatcher<A, E>(
    private val dispatcher: Dispatcher<A, E>,
    val scope: CoroutineScope
) : Dispatcher<A, E> {

    override fun dispatchAction(action: A) = dispatcher.dispatchAction(action)
    override fun dispatchEffect(effect: E) {
        scope.launch {
            dispatcher.dispatchEffect(effect)
        }
    }

    @JvmName("invokeAction")
    operator fun invoke(action: A) = dispatchAction(action)

    @JvmName("invokeEffect")
    operator fun invoke(effect: E) = dispatchEffect(effect)

}