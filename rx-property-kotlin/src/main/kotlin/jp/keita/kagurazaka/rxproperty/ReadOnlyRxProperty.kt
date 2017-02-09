package jp.keita.kagurazaka.rxproperty

import io.reactivex.Observable
import java.util.*

/**
 * Convert from [Observable] to [ReadOnlyRxProperty].
 */
fun <T> Observable<T>.toReadOnlyRxProperty(): ReadOnlyRxProperty<T> = ReadOnlyRxProperty(this)

/**
 * Convert from [Observable] to [ReadOnlyRxProperty] with the initial value.
 *
 * @param initialValue the initial value of [ReadOnlyRxProperty]
 */
fun <T> Observable<T>.toReadOnlyRxProperty(initialValue: T) = ReadOnlyRxProperty(this, initialValue)

/**
 * Convert from [Observable] to [ReadOnlyRxProperty] with the specified mode.
 *
 * @param mode the mode of [ReadOnlyRxProperty]
 */
fun <T> Observable<T>.toReadOnlyRxProperty(mode: EnumSet<RxProperty.Mode>)
        = ReadOnlyRxProperty(this, mode)

/**
 * Convert from [Observable] to [ReadOnlyRxProperty] with the initial value and the specified mode.
 *
 * @param initialValue the initial value of [ReadOnlyRxProperty]
 * @param mode the mode of [ReadOnlyRxProperty]
 */
fun <T> Observable<T>.toReadOnlyRxProperty(initialValue: T, mode: EnumSet<RxProperty.Mode>)
        = ReadOnlyRxProperty(this, initialValue, mode)
