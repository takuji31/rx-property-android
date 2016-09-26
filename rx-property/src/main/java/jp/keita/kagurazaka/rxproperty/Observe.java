package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;

import rx.functions.Func1;

/**
 * Converter from {@link Observable} to {@link rx.Observable}.
 */
public final class Observe {
    /**
     * Create an {@link rx.Observable} that emits the target {@link Observable} when the specified
     * property of the target {@link Observable} changes.
     *
     * @param target     the {@link Observable} to be observed
     * @param propertyId the property id of the target {@link Observable} (e.g. BR.some_property)
     * @param <T>        the type of {@code target}
     * @return an {@link rx.Observable} that emits the target {@link Observable} when the specified
     * property of the target {@link Observable} changes
     */
    public static <T extends Observable> rx.Observable<T> propertyOf(
            T target, int propertyId
    ) {
        Func1<T, T> getter = new Func1<T, T>() {
            @Override
            public T call(T t) {
                return t;
            }
        };
        return propertyOf(target, propertyId, getter);
    }

    /**
     * Create an {@link rx.Observable} that emits a changed property of the target
     * {@link Observable} when the specified property of the target {@link Observable} changes.
     *
     * @param target     the {@link Observable} to be observed
     * @param propertyId the property id of the target {@link Observable} (e.g. BR.some_property)
     * @param getter     the getter function to get the property from the target {@link Observable}
     * @param <T>        the type of {@code target}
     * @param <R>        the type of the property to be observed
     * @return an {@link rx.Observable} that emits a changed property of the target
     * {@link Observable} when the specified property of the target {@link Observable} changes
     */
    public static <T extends Observable, R> rx.Observable<R> propertyOf(
            T target, int propertyId, Func1<T, R> getter
    ) {
        return rx.Observable.create(new ObservePropertyOnSubscribe<>(target, propertyId, getter));
    }

    /**
     * Create an {@link rx.Observable} that emits the target {@link Observable} when even one
     * property of the target {@link Observable} changes.
     *
     * @param target the {@link Observable} to be observed
     * @param <T>    the type of {@code target}
     * @return an {@link rx.Observable} that emits the target {@link Observable} when even one
     * property of the target {@link Observable} changes
     */
    public static <T extends Observable> rx.Observable<T> allPropertiesOf(T target) {
        return rx.Observable.create(new ObserveAllPropertiesOnSubscribe<>(target));
    }
}
