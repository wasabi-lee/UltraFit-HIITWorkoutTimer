package io.incepted.ultrafittimer.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class TestUtils {
    companion object {
        val TEST_OBSERVER = object : LifecycleOwner {

            val mRegistry = init()

            fun init(): LifecycleRegistry {
                val registry = LifecycleRegistry(this)
                registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                return registry
            }

            override fun getLifecycle(): Lifecycle {
                return mRegistry
            }
        }
    }
}