package jp.keita.kagurazaka.rxproperty

import rx.Observable

/**
 * Convert from [Observable] to [RxCommand].
 */
fun <T> Observable<Boolean>.toRxCommand() = RxCommand<T>(this)

/**
 * Convert from [Observable] to [RxCommand] with the initial state.
 *
 * @param initialValue whether this {@code RxCommand} is enabled at first
 */
fun <T> Observable<Boolean>.toRxCommand(initialValue: Boolean) = RxCommand<T>(this, initialValue)
