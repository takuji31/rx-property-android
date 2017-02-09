package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;
import android.support.annotation.NonNull;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;

/**
 * Converter from {@link Observable} to {@link io.reactivex.Observable}.
 */
public final class Observe {
    /**
     * Create an {@link io.reactivex.Observable} that emits the target {@link Observable} when the
     * specified property of the target {@link Observable} changes.
     * <p>
     * The created {@link io.reactivex.Observable} never emits {@code onComplete} notification,
     * so {@link io.reactivex.Observer} must dispose the connection to avoid leak.
     *
     * @param target     the {@link Observable} to be observed
     * @param propertyId the property id of the target {@link Observable} (e.g. BR.some_property)
     * @param <T>        the type of {@code target}
     * @return an {@link io.reactivex.Observable} that emits the target {@link Observable} when
     * the specified property of the target {@link Observable} changes
     */
    public static <T extends Observable> io.reactivex.Observable<T> propertyOf(
            @NonNull T target, int propertyId
    ) {
        Function<T, T> getter = new Function<T, T>() {
            @Override
            public T apply(T t) {
                return t;
            }
        };
        return propertyOf(target, propertyId, getter);
    }

    /**
     * Create an {@link io.reactivex.Observable} that emits a changed property of the target
     * {@link Observable} when the specified property of the target {@link Observable} changes.
     * <p>
     * The created {@link io.reactivex.Observable} never emits {@code onComplete} notification,
     * so {@link io.reactivex.Observer} must dispose the connection to avoid leak.
     *
     * @param target           the {@link Observable} to be observed
     * @param targetPropertyId the property id of the target {@link Observable}
     *                         (e.g. BR.some_property)
     * @param getter           the getter function to get the property from the target
     *                         {@link Observable}
     * @param <T>              the type of {@code target}
     * @param <R>              the type of the property to be observed
     * @return an {@link io.reactivex.Observable} that emits a changed property of the target
     * {@link Observable} when the specified property of the target {@link Observable} changes
     */
    public static <T extends Observable, R> io.reactivex.Observable<R> propertyOf(
            @NonNull final T target,
            final int targetPropertyId,
            @NonNull final Function<T, R> getter
    ) {
        return io.reactivex.Observable.create(
                new ObservableOnSubscribe<R>() {
                    @Override
                    public void subscribe(final ObservableEmitter<R> emitter) throws Exception {
                        final Observable.OnPropertyChangedCallback callback
                                = new Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(Observable sender, int propertyId) {
                                try {
                                    if (propertyId == targetPropertyId) {
                                        emitter.onNext(getter.apply(target));
                                    }
                                } catch (Throwable e) {
                                    emitter.onError(e);
                                }
                            }
                        };

                        target.addOnPropertyChangedCallback(callback);
                        emitter.setCancellable(new Cancellable() {
                            @Override
                            public void cancel() throws Exception {
                                target.removeOnPropertyChangedCallback(callback);
                            }
                        });
                    }
                }
        );
    }

    /**
     * Create an {@link io.reactivex.Observable} that emits the target {@link Observable} when even
     * one property of the target {@link Observable} changes.
     * <p>
     * The created {@link io.reactivex.Observable} never emits {@code onComplete} notification,
     * so {@link io.reactivex.Observer} must dispose the connection to avoid leak.
     *
     * @param target the {@link Observable} to be observed
     * @param <T>    the type of {@code target}
     * @return an {@link io.reactivex.Observable} that emits the target {@link Observable} when even
     * one property of the target {@link Observable} changes
     */
    public static <T extends Observable> io.reactivex.Observable<T> allPropertiesOf(
            @NonNull final T target
    ) {
        return io.reactivex.Observable.create(
                new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(final ObservableEmitter<T> emitter) throws Exception {
                        final Observable.OnPropertyChangedCallback callback
                                = new Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(Observable sender, int propertyId) {
                                try {
                                    emitter.onNext(target);
                                } catch (Throwable e) {
                                    emitter.onError(e);
                                }
                            }
                        };

                        target.addOnPropertyChangedCallback(callback);
                        emitter.setCancellable(new Cancellable() {
                            @Override
                            public void cancel() throws Exception {
                                target.removeOnPropertyChangedCallback(callback);
                            }
                        });
                    }
                }
        );
    }

    private Observe() {
        throw new AssertionError("No instances.");
    }
}
