package com.badoo.mvicore.rx.binder

import com.badoo.mvicore.common.Source
import com.badoo.mvicore.rx.toObservable
import com.badoo.mvicore.rx.toSource
import io.reactivex.ObservableSource
import com.badoo.mvicore.common.binder.Connector as CommonConnector

interface Connector<Out, In> {
    operator fun invoke(source: ObservableSource<out Out>): ObservableSource<In>
}

internal fun <Out, In> Connector<Out, In>.toCommonConnector(): CommonConnector<Out, In> =
    CommonConnectorAdapter(this)

internal class CommonConnectorAdapter<Out, In>(private val delegate: Connector<Out, In>) : CommonConnector<Out, In> {
    override fun invoke(source: Source<out Out>): Source<In> =
        delegate.invoke(source.toObservable()).toSource()
}