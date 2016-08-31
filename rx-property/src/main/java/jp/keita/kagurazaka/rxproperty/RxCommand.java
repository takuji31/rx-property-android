package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Command class implementation in "Command pattern" for Android Data Binding. Whether {@code
 * RxCommand} can execute is detected by a boolean source {@link Observable}.
 */
public class RxCommand<T> implements Subscription {
    private final ObservableBoolean canExecuteFlag;
    private final SerializedSubject<T, T> trigger = PublishSubject.<T>create().toSerialized();

    private Subscription triggerSourceSubscription = null;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    private boolean isUnsubscribed = false;

    private Action0 unbindView = null;

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
    public RxCommand(Observable<Boolean> canExecuteSource) {
        this(canExecuteSource, true);
    }

    /**
     * Creates {@code RxCommand} to execute the specified command from the specified {@link
     * Observable} with the specified initial state.
     *
     * @param canExecuteSource an {@link Observable} to emit whether this {@code RxCommand} can
     *                         execute
     * @param initialValue     whether this {@code RxCommand} can execute initially
     */
    public RxCommand(final Observable<Boolean> canExecuteSource, boolean initialValue) {
        canExecuteFlag = new ObservableBoolean(initialValue);

        if (canExecuteSource != null) {
            Subscription subscription = canExecuteSource.distinctUntilChanged()
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            canExecuteFlag.set(value);
                        }

                        @Override
                        public void onError(Throwable e) {
                            canExecuteFlag.set(false);
                            trigger.onError(e);
                        }

                        @Override
                        public void onCompleted() {
                            canExecuteFlag.set(false);
                            trigger.onCompleted();
                        }
                    });
            subscriptions.add(subscription);
        }
    }

    /**
     * Gets a hot {@link Observable} to execute handlers this {@code RxCommand}.
     *
     * @return a hot {@link Observable} to execute handlers of this {@code RxCommand}
     */
    public Observable<T> asObservable() {
        return trigger.asObservable();
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
    public void execute(T parameter) {
        trigger.onNext(parameter);
    }

    /**
     * Bind the specified {@link Observable} as trigger of executing this {@code RxCommand}.
     *
     * @param triggerSource an {@link Observable} to kick this {@code RxCommand}
     */
    public void bindTrigger(Observable<T> triggerSource) {
        if (triggerSourceSubscription != null) {
            triggerSourceSubscription.unsubscribe();
        }
        triggerSourceSubscription = triggerSource.subscribe(new Observer<T>() {
            @Override
            public void onNext(T parameter) {
                if (canExecute()) {
                    execute(parameter);
                }
            }

            @Override
            public void onError(Throwable e) {
                canExecuteFlag.set(false);
                trigger.onError(e);
            }

            @Override
            public void onCompleted() {
                canExecuteFlag.set(false);
                trigger.onCompleted();
            }
        });
    }

    /**
     * Stops receiving notifications by the source {@link Observable}s and sending notifications to
     * observers of this {@code RxCommand}.
     */
    @Override
    public void unsubscribe() {
        if (isUnsubscribed) {
            return;
        }
        isUnsubscribed = true;

        trigger.onCompleted();
        subscriptions.unsubscribe();
        if (triggerSourceSubscription != null) {
            triggerSourceSubscription.unsubscribe();
        }

        if (canExecute()) {
            canExecuteFlag.set(false);
        }
        if (unbindView != null) {
            unbindView.call();
        }
        unbindView = null;
    }

    /**
     * Indicates whether this {@code RxCommand} is currently unsubscribed.
     *
     * @return {@code true} if this {@code RxCommand} has no {@link Observable} as source or is
     * currently unsubscribed, {@code false} otherwise
     */
    @Override
    public boolean isUnsubscribed() {
        return isUnsubscribed;
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
    public void setUnbindView(Action0 action) {
        if (unbindView != null) {
            unbindView.call();
        }
        unbindView = action;
    }
}
