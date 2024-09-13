package com.zhilian.base.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


fun interface ActionReducer<S, A> {
    fun handleAction(state: S, action: A): S
}

fun interface EffectHandler<S, A, E> {
    fun handleEffect(
        state: StateFlow<S>,
        effect: E,
        dispatcher: Dispatcher<A, E>
    )
}

class StoreImpl<S, A, E>(
    initialState: S,
    private val actionReducer: ActionReducer<S, A>,
    private val effectHandler: EffectHandler<S, A, E>,
) : Store<S, A, E> {

    private val _state = MutableStateFlow(initialState)
    override val state = _state.asStateFlow()

    override fun dispatchAction(action: A) {
        _state.update { state -> actionReducer.handleAction(state, action) }
    }

    override fun dispatchEffect(effect: E) {
        effectHandler.handleEffect(state, effect, this@StoreImpl)
    }
}


class StoreScopeImpl<S, A, E>(
    initialState: S,
    private val actionReducer: ActionReducer<S, A>,
    private val effectHandler: EffectHandler<S, A, E>,
    private val scope: CoroutineScope
) : Store<S, A, E> {

    private val _state = MutableStateFlow(initialState)
    override val state = _state.asStateFlow()

    override fun dispatchAction(action: A) {
        _state.update { state -> actionReducer.handleAction(state, action) }
    }

    override fun dispatchEffect(effect: E) {
        scope.launch {
            effectHandler.handleEffect(state, effect, this@StoreScopeImpl)
        }
    }
}
// and to construct it from a Store:
