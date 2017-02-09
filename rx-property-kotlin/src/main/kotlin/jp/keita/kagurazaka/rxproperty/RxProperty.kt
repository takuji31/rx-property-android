package jp.keita.kagurazaka.rxproperty

import io.reactivex.Observable
import java.util.*

/**
 * Convert from [Observable] to [RxProperty].
 */
fun <T> Observable<T>.toRxProperty(): RxProperty<T> = RxProperty(this)

/**
 * Convert from [Observable] to [RxProperty] with the initial value.
 *
 * @param initialValue the initial value of [RxProperty]
 */
fun <T> Observable<T>.toRxProperty(initialValue: T) = RxProperty(this, initialValue)

/**
 * Convert from [Observable] to [RxProperty] with the specified mode.
 *
 * @param mode the mode of [RxProperty]
 */
fun <T> Observable<T>.toRxProperty(mode: EnumSet<RxProperty.Mode>) = RxProperty(this, mode)

/**
 * Convert from [Observable] to [RxProperty] with the initial value and the specified mode.
 *
 * @param initialValue the initial value of [RxProperty]
 * @param mode the mode of [RxProperty]
 */
fun <T> Observable<T>.toRxProperty(initialValue: T, mode: EnumSet<RxProperty.Mode>)
        = RxProperty(this, initialValue, mode)
