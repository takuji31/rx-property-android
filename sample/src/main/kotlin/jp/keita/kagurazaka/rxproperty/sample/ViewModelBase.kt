package jp.keita.kagurazaka.rxproperty.sample

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import jp.keita.kagurazaka.rxproperty.RxProperty
import java.util.*

abstract class ViewModelBase : Disposable {
    protected val disposables = CompositeDisposable()

    override fun dispose() {
        if (!isDisposed) {
            disposables.clear()
        }
    }

    override fun isDisposed(): Boolean {
        return disposables.isDisposed
    }

    // for JavaBasicsViewModel
    protected fun addDisposables(vararg disposables: Disposable) {
        for (s in disposables) {
            this.disposables.add(s)
        }
    }

    // for KotlinBasicsViewModel
    inline fun <reified T : Disposable> T.asManaged(): T = this.apply {
        disposables.add(this)
    }

    companion object {
        @JvmStatic
        val DISABLE_RAISE_ON_SUBSCRIBE: EnumSet<RxProperty.Mode>
                = EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED)
    }
}
