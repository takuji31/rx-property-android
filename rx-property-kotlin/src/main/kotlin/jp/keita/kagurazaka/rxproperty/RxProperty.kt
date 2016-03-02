package jp.keita.kagurazaka.rxproperty

import rx.Observable
import java.util.*

/**
 * Convert from [Observable] to [RxProperty].
 */
fun <T> Observable<T>.toRxProperty(): RxProperty<T> = RxProperty(this)


/**
 * Convert from [Observable] to [RxProperty] with the initial value.
 *
 * @param initialValue a value to set.
 */
fun <T> Observable<T>.toRxProperty(initialValue: T?) = RxProperty(this, initialValue)

/**
 * Convert from [Observable] to [RxProperty] with the specified mode.
 *
 * @param mode mode to set.
 */
fun <T> Observable<T>.toRxProperty(mode: EnumSet<RxProperty.Mode>) = RxProperty(this, mode)

/**
 * Convert from [Observable] to [RxProperty] with the initial value and the specified mode.
 *
 * @param initialValue a value to set.
 * @param mode mode to set.
 */
fun <T> Observable<T>.toRxProperty(initialValue: T?, mode: EnumSet<RxProperty.Mode>)
        = RxProperty(this, initialValue, mode)
