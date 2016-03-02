package jp.keita.kagurazaka.rxproperty.sample

import android.view.View
import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty
import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty
import rx.Subscription
import rx.subscriptions.CompositeSubscription

abstract class ViewModel : Subscription {
    abstract val input: RxProperty<String>
    abstract val output: ReadOnlyRxProperty<String>
    abstract val command: RxCommand<View.OnClickListener>

    private val subscriptions = CompositeSubscription()

    override fun unsubscribe() {
        if (!isUnsubscribed) {
            subscriptions.unsubscribe()
        }
    }

    override fun isUnsubscribed(): Boolean {
        return subscriptions.isUnsubscribed
    }

    // for JavaViewModel
    protected fun addSubscriptions(vararg subscriptions: Subscription) {
        for (s in subscriptions) {
            this.subscriptions.add(s)
        }
    }

    // for KotlinViewModel
    protected fun <T> RxProperty<T>.asManaged(): RxProperty<T> {
        subscriptions.add(this)
        return this
    }

    protected fun <T> RxCommand<T>.asManaged(): RxCommand<T> {
        subscriptions.add(this)
        return this
    }
}
