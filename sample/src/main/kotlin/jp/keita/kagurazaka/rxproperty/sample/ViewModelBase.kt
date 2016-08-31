package jp.keita.kagurazaka.rxproperty.sample

import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*

abstract class ViewModelBase : Subscription {
    private val subscriptions = CompositeSubscription()

    override fun unsubscribe() {
        if (!isUnsubscribed) {
            subscriptions.unsubscribe()
        }
    }

    override fun isUnsubscribed(): Boolean {
        return subscriptions.isUnsubscribed
    }

    // for JavaBasicsViewModel
    protected fun addSubscriptions(vararg subscriptions: Subscription) {
        for (s in subscriptions) {
            this.subscriptions.add(s)
        }
    }

    // for KotlinBasicsViewModel
    protected fun Subscription.asManaged() {
        subscriptions.add(this)
    }

    protected fun <T> RxProperty<T>.asManaged(): RxProperty<T> {
        subscriptions.add(this)
        return this
    }

    protected fun <T> RxCommand<T>.asManaged(): RxCommand<T> {
        subscriptions.add(this)
        return this
    }

    companion object {
        @JvmStatic
        val DISABLE_RAISE_ON_SUBSCRIBE: EnumSet<RxProperty.Mode>
                = EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED)
    }
}
