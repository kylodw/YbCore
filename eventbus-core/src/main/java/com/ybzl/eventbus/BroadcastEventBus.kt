/*
 * Copyright 2023-2024 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ybzl.eventbus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * @suppress
 */
object BroadcastEventBus : EventListenable<Event> {
    private val mutableEvents = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE)
    override val events = mutableEvents.asSharedFlow()

    suspend fun postEvent(event: Event) {
        mutableEvents.emit(event)
    }

    fun tryPostEvent(event: Event) {
        mutableEvents.tryEmit(event)
    }

    fun postEvent(event: Event, scope: CoroutineScope): Job {
        return scope.launch { postEvent(event) }
    }

    suspend fun postEvents(eventsToPost: Collection<Event>) {
        eventsToPost.forEach { event ->
            mutableEvents.emit(event)
        }
    }

    fun postEvents(eventsToPost: Collection<Event>, scope: CoroutineScope): Job {
        return scope.launch { postEvents(eventsToPost) }
    }

}