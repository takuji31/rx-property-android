package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import jp.keita.kagurazaka.rxproperty.internal.Helper;

/**
 * Command class implementation in "Command pattern" for Android Data Binding. Whether {@code
 * RxCommand} can execute is detected by a boolean source {@link Observable}.
 */
public class RxCommand<T> extends Observable<T> implements Disposable {
    @NonNull
    private final ObservableBoolean canExecuteFlag;

    @NonNull
    private final Subject<T> kicker = PublishSubject.<T>create().toSerialized();

    @Nullable
    private Disposable triggerSourceDisposable = null;

    @Nullable
    private Disposable canExecuteSourceDisposable = null;

    @NonNull
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);

    @Nullable
    private Cancellable cancellable = null;

    /**
     * Creates {@code RxCommand} which is always enabled.
     */
    public RxCommand() {
        this(null);
    }

    /**
     * Creates {@code RxCommand} from the specified {@link Observable}.
     *
     * @param canExecuteSource an {@link Observable} representing that can this {@code RxCommand}
     *                         execute
     */
    public RxCommand(@Nullable Observable<Boolean> canExecuteSource) {
        this(canExecuteSource, true);
    }

    /**
     * Creates {@code RxCommand} to execute the specified command from the specified {@link
     * Observable} with the specified initial state.
     *
     * @param canExecuteSource an {@link Observable} to emit whether this {@code RxCommand} can
     *                         execute
     * @param canExecute       whether this {@code RxCommand} can execute initially
     */
    public RxCommand(@Nullable final Observable<Boolean> canExecuteSource, boolean canExecute) {
        canExecuteFlag = new ObservableBoolean(canExecute);

        if (canExecuteSource != null) {
            canExecuteSourceDisposable = canExecuteSource.distinctUntilChanged()
                    .subscribeWith(new DisposableObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            canExecuteFlag.set(value);
                        }

                        @Override
                        public void onError(Throwable e) {
                            kicker.onError(e);
                            RxCommand.this.dispose();
                        }

                        @Override
                        public void onComplete() {
                            kicker.onComplete();
                            RxCommand.this.dispose();
                        }
                    });
        }
    }

    /**
     * Indicates whether this {@code RxCommand} can execute currently.
     *
     * @return {@code true} if this {@code RxCommand} can execute, {@code false} otherwise
     */
    public boolean canExecute() {
        return canExecuteFlag.get();
    }

    /**
     * Execute this {@code RxCommand} with the specified parameter.
     *
     * @param parameter a parameter of this {@code RxCommand}
     */
    public void execute(@NonNull T parameter) {
        kicker.onNext(parameter);
    }

    /**
     * Bind the specified {@link Observable} as trigger of executing this {@code RxCommand}.
     *
     * @param triggerSource an {@link Observable} to kick this {@code RxCommand}
     */
    public RxCommand<T> bindTrigger(@NonNull Observable<T> triggerSource) {
        if (triggerSourceDisposable != null) {
            triggerSourceDisposable.dispose();
        }

        triggerSourceDisposable = triggerSource.subscribeWith(new DisposableObserver<T>() {
            @Override
            public void onNext(T parameter) {
                if (canExecute()) {
                    execute(parameter);
                }
            }

            @Override
            public void onError(Throwable e) {
                kicker.onError(e);
                RxCommand.this.dispose();
            }

            @Override
            public void onComplete() {
                kicker.onComplete();
                RxCommand.this.dispose();
            }
        });

        return this;
    }

    /**
     * Stops receiving notifications by the source {@link Observable}s and sending notifications to
     * observers of this {@code RxCommand}.
     */
    @Override
    public void dispose() {
        if (isDisposed.compareAndSet(false, true)) {
            Helper.safeComplete(kicker);

            Helper.safeDispose(canExecuteSourceDisposable);
            Helper.safeDispose(triggerSourceDisposable);

            if (canExecute()) {
                canExecuteFlag.set(false);
            }

            Helper.safeCancel(cancellable);
            cancellable = null;
        }
    }

    /**
     * Indicates whether this {@code RxCommand} is currently disposed.
     *
     * @return {@code true} if this {@code RxCommand} has no {@link Observable} as source or is
     * currently disposed, {@code false} otherwise
     */
    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get
     * whether this {@code RxCommand} can execute, use {@link RxCommand#canExecute()}
     */
    @Deprecated
    public ObservableBoolean getEnabled() {
        return canExecuteFlag;
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code except
     * in {@link android.databinding.BindingAdapter} implementation.
     */
    @Deprecated
    public void setCancellable(@NonNull Cancellable cancellable) {
        Helper.safeCancel(this.cancellable);
        this.cancellable = cancellable;
    }

    @Override
    protected void subscribeActual(final Observer<? super T> observer) {
        kicker.subscribe(observer);
    }


}
