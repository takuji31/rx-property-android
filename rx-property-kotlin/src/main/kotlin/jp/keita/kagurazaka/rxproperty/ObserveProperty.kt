package jp.keita.kagurazaka.rxproperty

import android.databinding.Observable

/**
 * Observe the specified property of this [Observable] changes.
 */
fun <T : Observable> T.observeProperty(propertyId: Int): rx.Observable<T>
        = Observe.propertyOf(this, propertyId)

/**
 * Observe the specified property of this [Observable] changes.
 */
fun <T : Observable, R> T.observeProperty(propertyId: Int, getter: (T) -> R): rx.Observable<R>
        = Observe.propertyOf(this, propertyId) { getter(it) }

/**
 * Observe even one property of this [Observable] changes.
 */
fun <T : Observable> T.observeAllProperties(): rx.Observable<T>
        = Observe.allPropertiesOf(this)
