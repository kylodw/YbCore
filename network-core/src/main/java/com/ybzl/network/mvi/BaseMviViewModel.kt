package com.ybzl.network.mvi

import com.zhilian.base.mvi.ActionReducer
import com.zhilian.base.mvi.EffectHandler
import com.zhilian.base.mvi.Store
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseMviViewModel<S, A, E>(
    initialState: S,
    /*    private val actionReducer: ActionReducer<S, A>,
    private val effectHandler: EffectHandler<S, A, E>,*/
) : BaseViewModel(), Store<S, A, E> {
    private val _state = MutableStateFlow(initialState)
    override val state = _state.asStateFlow()

    override fun dispatchAction(action: A) {
        _state.update { state -> getActionReducer().handleAction(state, action) }
    }

    override fun dispatchEffect(effect: E) {
        getEffectHandler()?.handleEffect(state, effect, this)
    }

    abstract fun getActionReducer(): ActionReducer<S, A>
    open fun getEffectHandler(): EffectHandler<S, A, E>?{
        return null
    }


}

class TestViewModel : BaseMviViewModel<String, TestAction, TestEffect>("") {
    override fun getActionReducer(): ActionReducer<String, TestAction> {
        return ActionReducer<String, TestAction> { state, action ->
            when (action) {
                is TestAction.Test -> {
                    state
                }
            }
        }
    }


    override fun getEffectHandler(): EffectHandler<String, TestAction, TestEffect> {
        return EffectHandler<String, TestAction, TestEffect> { state, effect, dispatcher ->

        }
    }


}


sealed interface TestAction {
    data object Test : TestAction
}

sealed interface TestEffect {

}