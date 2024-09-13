package com.zhilian.base.mvi


import kotlinx.coroutines.flow.StateFlow


interface Store<S, A, E> : Dispatcher<A, E> {
    val state: StateFlow<S>

    companion object {
        operator fun <S, A, E> invoke(
            initialState: S,
            actionReducer: ActionReducer<S, A>,
            effectHandler: EffectHandler<S, A, E>
        ): Store<S, A, E> = StoreImpl(initialState, actionReducer, effectHandler)

    }
}