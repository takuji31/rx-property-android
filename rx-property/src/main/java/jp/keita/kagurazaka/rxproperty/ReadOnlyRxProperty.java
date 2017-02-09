package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import jp.keita.kagurazaka.rxproperty.internal.Helper;

/**
 * One-way bindable and observable property for Android Data Binding.
 *
 * @param <T> the type of the inner property
 */
public class ReadOnlyRxProperty<T>
        extends Observable<T>
        implements android.databinding.Observable, Disposable {
    private final boolean isDistinctUntilChanged;

    // for value emitter
    @NonNull
    private final ReadOnlyRxPropertyValueField<T> valueField;

    @NonNull
    private final Subject<T> valueEmitter;

    @NonNull
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);

    @Nullable
    private Cancellable cancellable = null;

    @NonNull
    private final Disposable sourceDisposable;

    /**
     * Creates {@code ReadOnlyRxProperty} from the specified {@link Observable}.
     *
     * @param source a source {@link Observable} of this {@code ReadOnlyRxProperty}
     */
    public ReadOnlyRxProperty(@NonNull Observable<T> source) {
        this(source, Maybe.<T>empty(), RxProperty.Mode.DEFAULT);
    }

    /**
     * Creates {@code ReadOnlyRxProperty} from the specified {@link Observable} with the initial
     * value.
     *
     * @param source       a source {@link Observable} of this {@code ReadOnlyRxProperty}
     * @param initialValue the initial value of this {@code ReadOnlyRxProperty}
     */
    public ReadOnlyRxProperty(@NonNull Observable<T> source, @NonNull T initialValue) {
        this(source, Helper.createInitialMaybe(initialValue), RxProperty.Mode.DEFAULT);
    }

    /**
     * Creates {@code ReadOnlyRxProperty} from the specified {@link Observable} with the specified
     * mode.
     *
     * @param source a source {@link Observable} of this {@code ReadOnlyRxProperty}
     * @param mode   mode of this {@code ReadOnlyRxProperty}
     */
    public ReadOnlyRxProperty(@NonNull Observable<T> source,
                              @NonNull EnumSet<RxProperty.Mode> mode) {
        this(source, Maybe.<T>empty(), mode);
    }

    /**
     * Creates {@code ReadOnlyRxProperty} from the specified {@link Observable} with the initial
     * value and the specified mode.
     *
     * @param source       a source {@link Observable} of this {@code ReadOnlyRxProperty}
     * @param initialValue the initial value of this {@code ReadOnlyRxProperty}
     * @param mode         mode of this {@code ReadOnlyRxProperty}
     */
    public ReadOnlyRxProperty(@NonNull Observable<T> source, @NonNull T initialValue,
                              @NonNull EnumSet<RxProperty.Mode> mode) {
        this(source, Helper.createInitialMaybe(initialValue), mode);
    }

    private ReadOnlyRxProperty(@NonNull Observable<T> source, @NonNull Maybe<T> initialMaybe,
                               @NonNull EnumSet<RxProperty.Mode> mode) {
        // null check
        Helper.checkNull(source, "source");
        Helper.checkNull(mode, "mode");

        // Initialize an ObservableField.
        T initialValue = initialMaybe.blockingGet();
        valueField = new ReadOnlyRxPropertyValueField<>(this, initialValue);

        // Set modes.
        isDistinctUntilChanged
                = !mode.contains(RxProperty.Mode.NONE)
                && mode.contains(RxProperty.Mode.DISTINCT_UNTIL_CHANGED);
        boolean isRaiseLatestValueOnSubscribe
                = !mode.contains(RxProperty.Mode.NONE)
                && mode.contains(RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE);

        // Create a value emitter.
        if (isRaiseLatestValueOnSubscribe) {
            valueEmitter = (initialValue != null ?
                    BehaviorSubject.createDefault(initialValue) :
                    BehaviorSubject.<T>create()).toSerialized();
        } else {
            valueEmitter = PublishSubject.<T>create().toSerialized();
        }

        // Subscribe the source observable.
        sourceDisposable = source.subscribeWith(new DisposableObserver<T>() {
            @Override
            public void onNext(T value) {
                set(value);
            }

            @Override
            public void onError(Throwable e) {
                valueEmitter.onError(e);
                ReadOnlyRxProperty.this.dispose();
            }

            @Override
            public void onComplete() {
                valueEmitter.onComplete();
                ReadOnlyRxProperty.this.dispose();
            }
        });

        // Register RxJava plugins.
        RxJavaPlugins.onAssembly(this);
    }

    /**
     * Gets the latest value of this {@code ReadOnlyRxProperty}.
     *
     * @return the latest value stored in this {@code ReadOnlyRxProperty}
     */
    @Nullable
    public T get() {
        return valueField.get();
    }

    /**
     * Forcibly notifies the latest value of this {@code ReadOnlyRxProperty} to all observers
     * including the bound view. This method ignores {@link RxProperty.Mode#DISTINCT_UNTIL_CHANGED}.
     */
    public void forceNotify() {
        valueField.setValue(get());
    }

    /**
     * Stops receiving notifications by the source {@link Observable} and send notifications to
     * observers of this {@code ReadOnlyRxProperty}.
     */
    @Override
    public void dispose() {
        if (isDisposed.compareAndSet(false, true)) {
            // Terminate internal subjects.
            Helper.safeComplete(valueEmitter);

            // Dispose the source subscription.
            Helper.safeDispose(sourceDisposable);

            // Unbind a view observer.
            Helper.safeCancel(cancellable);
            cancellable = null;
        }
    }

    /**
     * Indicates whether this {@code ReadOnlyRxProperty} is currently disposed.
     *
     * @return {@code true} if this {@code ReadOnlyRxProperty} has no {@link Observable} as source
     * or is currently disposed, {@code false} otherwise
     */
    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        valueField.addOnPropertyChangedCallback(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        valueField.removeOnPropertyChangedCallback(callback);
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        valueEmitter.subscribe(observer);
    }

    private void set(@NonNull T value) {
        if (isDisposed()) {
            return;
        }

        if (isDistinctUntilChanged && Helper.compare(value, get())) {
            return;
        }
        valueField.setValue(value);
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * latest value of this property, use {@link ReadOnlyRxProperty#get()} instead of this method.
     */
    @Deprecated
    public ObservableField<T> getValue() {
        return valueField;
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code except
     * in {@link android.databinding.BindingAdapter} implementation.
     */
    @Deprecated
    public void setCancellable(@Nullable Cancellable cancellable) {
        Helper.safeCancel(this.cancellable);
        this.cancellable = cancellable;
    }

    /**
     * Specialized {@link ObservableField} to represent a value of {@link ReadOnlyRxProperty},
     * which is used in view binding.
     *
     * @param <T> the type of value stored in this property
     */
    private static class ReadOnlyRxPropertyValueField<T> extends ObservableField<T> {
        private final ReadOnlyRxProperty<T> parent;
        private T value;

        ReadOnlyRxPropertyValueField(ReadOnlyRxProperty<T> parent, T initialValue) {
            this.parent = parent;
            this.value = initialValue;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            throw new UnsupportedOperationException(
                    "ReadOnlyRxProperty doesn't support two-way binding.");
        }

        void setValue(T value) {
            this.value = value;
            parent.valueEmitter.onNext(value);
            notifyChange();
        }
    }
}
