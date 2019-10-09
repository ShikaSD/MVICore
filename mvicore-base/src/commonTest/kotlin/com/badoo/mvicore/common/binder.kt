package com.badoo.mvicore.common

import com.badoo.mvicore.common.binder.Binder
import com.badoo.mvicore.common.binder.NotNullConnector
import com.badoo.mvicore.common.binder.using
import com.badoo.mvicore.common.lifecycle.Lifecycle
import kotlin.test.Test

class BinderTest {
    private val source = source<Int>()
    private val sink = TestSink<Int>()

    @Test
    fun binder_without_lifecycle_connects_source_and_sink() {
        Binder().apply {
            bind(source to sink)
        }

        source.invoke(0)
        sink.assertValues(0)
    }

    @Test
    fun binder_without_lifecycle_does_not_connect_source_and_sink_after_cancel() {
        val binder = Binder().apply {
            bind(source to sink)
        }

        binder.cancel()

        source.invoke(0)
        sink.assertNoValues()
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_using_mapper() {
        Binder().apply {
            bind(source to sink using { it + 1 })
        }

        source.invoke(0)
        sink.assertValues(1)
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_skips_nulls_from_mapper() {
        Binder().apply {
            bind(source to sink using { if (it % 2 == 0) null else it })
        }

        source.invoke(0)
        source.invoke(1)
        source.invoke(2)
        sink.assertValues(1)
    }

    @Test
    fun binder_without_lifecycle_connects_source_and_sink_using_connector() {
        val connector = NotNullConnector<Int, Int> { it }
        Binder().apply {
            bind(source to sink using connector)
        }

        source.invoke(0)
        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_connects_source_and_sink_when_active() {
        val lifecycle = Lifecycle.manual()
        Binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.invoke(0)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_does_not_connect_source_and_sink_before_active() {
        val lifecycle = Lifecycle.manual()
        Binder(lifecycle).apply {
            bind(source to sink)
        }

        source.invoke(0)
        lifecycle.begin()

        sink.assertNoValues()
    }

    @Test
    fun binder_with_lifecycle_disconnect_source_and_sink_after_end() {
        val lifecycle = Lifecycle.manual()
        Binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.invoke(0)
        lifecycle.end()
        source.invoke(1)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_reconnect_source_and_sink_after_begin() {
        val lifecycle = Lifecycle.manual()
        Binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.invoke(0)
        lifecycle.end()
        source.invoke(1)
        lifecycle.begin()
        source.invoke(2)

        sink.assertValues(0, 2)
    }

    @Test
    fun binder_with_lifecycle_does_not_reconnect_source_and_sink_after_cancel() {
        val lifecycle = Lifecycle.manual()
        Binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        source.invoke(0)
        lifecycle.end()
        lifecycle.cancel()
        lifecycle.begin()
        source.invoke(2)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_connects_source_and_sink_if_lifecycle_started() {
        val lifecycle = Lifecycle.manual()
        lifecycle.begin()

        Binder(lifecycle).apply {
            bind(source to sink)
        }

        source.invoke(0)

        sink.assertValues(0)
    }

    @Test
    fun binder_with_lifecycle_does_not_reconnect_on_duplicated_lifecycle_events() {
        val lifecycle = Lifecycle.manual()

        Binder(lifecycle).apply {
            bind(source to sink)
        }

        lifecycle.begin()
        lifecycle.begin()

        source.invoke(0)

        sink.assertValues(0)
    }

    @Test
    fun binder_covariant_endpoints_compile_for_pair() {
        val sink = { it: Any -> /* no-op */ }
        Binder().bind(source to sink)
    }

    @Test
    fun binder_covariant_endpoints_compile_for_connection() {
        val sink = { it: Any -> /* no-op */ }
        val intToString: (Int) -> String = { it.toString() }
        Binder().bind(source to sink using intToString)
    }
}