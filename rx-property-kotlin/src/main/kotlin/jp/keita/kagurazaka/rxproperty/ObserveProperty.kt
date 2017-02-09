package jp.keita.kagurazaka.rxproperty

import android.databinding.Observable

/**
 * Observe the specified property of this [Observable] changes.
 *
 * @param propertyId the property id of this [Observable] (e.g. BR.some_property)
 */
fun <T : Observable> T.observeProperty(propertyId: Int): io.reactivex.Observable<T>
        = Observe.propertyOf(this, propertyId)

/**
 * Observe the specified property of this [Observable] changes.
 *
 * @param propertyId the property id of this [Observable] (e.g. BR.some_property)
 * @param getter the getter function to get the property from this [Observable]
 */
fun <T : Observable, R> T.observeProperty(propertyId: Int, getter: (T) -> R)
        : io.reactivex.Observable<R> = Observe.propertyOf(this, propertyId) { getter(it) }

/**
 * Observe even one property of this [Observable] changes.
 */
fun <T : Observable> T.observeAllProperties(): io.reactivex.Observable<T>
        = Observe.allPropertiesOf(this)
