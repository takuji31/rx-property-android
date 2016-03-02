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

/**
 * Convert from [Observable] to [RxCommand] to execute the specified command.
 *
 * @param command a callback called when this {@code RxCommand} is executed
 */
fun <T> Observable<Boolean>.toRxCommand(command: T) = RxCommand<T>(this, command)

/**
 * Convert from [Observable] to [RxCommand] to execute the specified command with the initial state.
 *
 * @param initialValue whether this {@code RxCommand} is enabled at first
 * @param command a callback called when this {@code RxCommand} is executed
 */
fun <T> Observable<Boolean>.toRxCommand(initialValue: Boolean, command: T)
        = RxCommand<T>(this, initialValue, command)
