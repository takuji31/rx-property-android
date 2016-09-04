package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Two-way bindable and observable property for Android Data Binding.
 */
public class RxProperty<T> implements ReadOnlyRxProperty<T> {
    private static final EnumSet<Mode> DEFAULT_MODE
            = EnumSet.of(Mode.DISTINCT_UNTIL_CHANGED, Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE);

    private final boolean isDistinctUntilChanged;

    // for value emitter
    private final RxPropertyField<T> valueField;
    private final SerializedSubject<T, T> valueEmitter;

    // for validator
    private final RxPropertyErrorField errorField;
    private final ObservableBoolean hasErrorField = new ObservableBoolean(false);
    private final SerializedSubject<T, T> validationTrigger
            = PublishSubject.<T>create().toSerialized();
    private final SerializedSubject<List<String>, List<String>> errorEmitter;
    private Validator<T> validator;
    private List<String> currentErrors;

    private boolean isUnsubscribed = false;
    private Action0 unbindView = null;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * Creates {@code RxProperty} without an initial value.
     */
    public RxProperty() {
        this((T) null);
    }

    /**
     * Creates {@code RxProperty} with the initial value.
     *
     * @param initialValue the initial value of this {@code RxProperty}
     */
    public RxProperty(@Nullable T initialValue) {
        this(initialValue, DEFAULT_MODE);
    }

    /**
     * Creates {@code RxProperty} with the specified mode.
     *
     * @param mode mode of this {@code RxProperty}
     */
    public RxProperty(@NonNull EnumSet<Mode> mode) {
        this((T) null, mode);
    }

    /**
     * Creates {@code RxProperty} with the initial value and the specified mode.
     *
     * @param initialValue the initial value of this {@code RxProperty}
     * @param mode         mode of this {@code RxProperty}
     */
    public RxProperty(@Nullable T initialValue, @NonNull EnumSet<Mode> mode) {
        this(null, initialValue, mode);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable}.
     *
     * @param source a source {@link Observable} of this {@code RxProperty}
     */
    public RxProperty(@NonNull Observable<T> source) {
        this(source, DEFAULT_MODE);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable} with the initial value.
     *
     * @param source       a source {@link Observable} of this {@code RxProperty}
     * @param initialValue the initial value of this {@code RxProperty}
     */
    public RxProperty(@NonNull Observable<T> source, @Nullable T initialValue) {
        this(source, initialValue, DEFAULT_MODE);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable} with the specified mode.
     *
     * @param source a source {@link Observable} of this {@code RxProperty}
     * @param mode   mode of this {@code RxProperty}
     */
    public RxProperty(@NonNull Observable<T> source, @NonNull EnumSet<Mode> mode) {
        this(source, null, mode);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable} with the initial value and the
     * specified mode.
     *
     * @param source       a source {@link Observable} of this {@code RxProperty}
     * @param initialValue the initial value of this {@code RxProperty}
     * @param mode         mode of this {@code RxProperty}
     */
    public RxProperty(@Nullable Observable<T> source, @Nullable T initialValue,
                      @NonNull EnumSet<Mode> mode) {
        valueField = new RxPropertyField<>(this, initialValue);
        errorField = new RxPropertyErrorField(null);

        // Set modes.
        isDistinctUntilChanged
                = !mode.contains(Mode.NONE) && mode.contains(Mode.DISTINCT_UNTIL_CHANGED);
        boolean isRaiseLatestValueOnSubscribe
                = !mode.contains(Mode.NONE) && mode.contains(Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE);

        // Create emitters.
        valueEmitter = new SerializedSubject<>(
                isRaiseLatestValueOnSubscribe ?
                        BehaviorSubject.create(initialValue) :
                        PublishSubject.<T>create()
        );
        errorEmitter = new SerializedSubject<>(
                isRaiseLatestValueOnSubscribe ?
                        BehaviorSubject.create((List<String>) null) :
                        PublishSubject.<List<String>>create()
        );

        if (source != null) {
            final Subscription s1 = source.subscribe(new Observer<T>() {
                @Override
                public void onNext(T value) {
                    set(value);
                }

                @Override
                public void onError(Throwable e) {
                    valueEmitter.onError(e);
                }

                @Override
                public void onCompleted() {
                    valueEmitter.onCompleted();
                }
            });
            subscriptions.add(s1);
        }

        final Subscription s2 = validationTrigger.subscribe(new Action1<T>() {
            @Override
            public void call(T value) {
                if (validator != null) {
                    final List<String> errors = validator.validate(value);
                    final String summarized = validator.summarizeErrorMessages(errors);

                    if (errors == null || errors.isEmpty()) {
                        currentErrors = null;
                    } else {
                        currentErrors = errors;
                    }
                    errorField.setValue(summarized);
                    hasErrorField.set(hasErrors());
                    errorEmitter.onNext(currentErrors);
                } else {
                    if (hasErrors()) {
                        currentErrors = null;
                        errorField.setValue(null);
                        hasErrorField.set(false);
                        errorEmitter.onNext(null);
                    }
                }
            }
        });
        subscriptions.add(s2);
    }

    /**
     * Gets a hot {@link Observable} to emit values of this {@code RxProperty}.
     *
     * @return a hot {@link Observable} to emit values of this {@code RxProperty}
     */
    @Override
    public Observable<T> asObservable() {
        return valueEmitter.asObservable();
    }

    /**
     * Gets an event {@link Observable} of validation error messages.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit current validation error messages of this
     * {@code RxProperty}
     */
    @Override
    public Observable<List<String>> onErrorsChanged() {
        return errorEmitter.asObservable();
    }

    /**
     * Gets an event {@link Observable} of summarized validation error messages.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit summarized validation error messages of this
     * {@code RxProperty}
     */
    @Override
    public Observable<String> onSummarizedErrorChanged() {
        return onErrorsChanged().map(new Func1<List<String>, String>() {
            @Override
            public String call(List<String> strings) {
                return getSummarizedErrorMessage();
            }
        });
    }

    /**
     * Gets an event {@link Observable} to emit {@link ReadOnlyRxProperty#hasErrors} when it is
     * changed.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit {@link ReadOnlyRxProperty#hasErrors} when it is
     * changed
     */
    @Override
    public Observable<Boolean> onHasErrorsChanged() {
        return onErrorsChanged().map(new Func1<List<String>, Boolean>() {
            @Override
            public Boolean call(List<String> strings) {
                return hasErrors();
            }
        });
    }

    /**
     * Gets the latest value of this {@code RxProperty}.
     *
     * @return the latest value stored in this {@code RxProperty}
     */
    @Override
    public T get() {
        return valueField.get();
    }

    /**
     * Sets the specified value to this {@code RxProperty}. The change will be notified to both
     * bound view and observers of this {@code RxProperty}.
     *
     * @param value a value to set
     */
    public void set(T value) {
        set(value, true);
    }

    /**
     * Sets the specified value to this {@code RxProperty}. The change will be notified to
     * observers of this {@code RxProperty} but not affect the bound view.
     *
     * @param value a value to set
     */
    public void setWithoutViewUpdate(T value) {
        set(value, false);
    }

    /**
     * Returns whether this {@code RxProperty} has validation errors.
     *
     * @return true if this {@code RxProperty} has validation errors; otherwise else
     */
    @Override
    public boolean hasErrors() {
        return currentErrors != null;
    }

    /**
     * Gets the current validation error messages of this {@code RxProperty}.
     *
     * @return the current validation error messages if validation failed; otherwise null
     */
    @Override
    public List<String> getErrorMessages() {
        return currentErrors;
    }

    /**
     * Gets the current summarized validation error message of this {@code RxProperty}.
     *
     * @return the current summarized validation error message if validation failed; otherwise null
     */
    @Override
    public String getSummarizedErrorMessage() {
        return errorField.get();
    }

    /**
     * Sets the specified validator to this {@code RxProperty}.
     * <p>
     * Validator should return an error message if the validation failed; otherwise null.
     *
     * @param validator a validator to test the value of this {@code RxProperty}
     * @return this instance
     */
    public RxProperty<T> setValidator(@Nullable final Func1<T, String> validator) {
        return setValidator(validator, true);
    }

    /**
     * Sets the specified validator to this {@code RxProperty}.
     * <p>
     * Validator should return an error message if the validation failed; otherwise null.
     *
     * @param validator   a validator to test the value of this {@code RxProperty}
     * @param validateNow if true, the specified validation process will be executed immediately
     * @return this instance
     */
    public RxProperty<T> setValidator(
            @Nullable final Func1<T, String> validator,
            boolean validateNow) {
        if (validator == null) {
            return setValidator((Validator<T>) null, validateNow);
        }

        final Validator<T> wrapped = new Validator<T>() {
            @Override
            public List<String> validate(@Nullable final T value) {
                final String message = validator.call(value);
                if (message == null) {
                    return null;
                }
                return Collections.singletonList(message);
            }

            @Override
            public String summarizeErrorMessages(@Nullable final List<String> errorMessages) {
                if (errorMessages == null || errorMessages.isEmpty()) {
                    return null;
                }
                return errorMessages.get(0);
            }
        };
        return setValidator(wrapped, validateNow);
    }

    /**
     * Sets the specified validator to this {@code RxProperty}.
     * <p>
     * Validator should return a list of error messages if the validation failed; otherwise null or
     * the empty list.
     *
     * @param validator a validator to test the value of this {@code RxProperty}
     * @return this instance
     */
    public RxProperty<T> setValidator(@Nullable final Validator<T> validator) {
        return setValidator(validator, true);
    }

    /**
     * Sets the specified validator to this {@code RxProperty}.
     * <p>
     * Validator should return a list of error messages if the validation failed; otherwise null or
     * the empty list.
     *
     * @param validator   a validator to test the value of this {@code RxProperty}
     * @param validateNow if true, the specified validation process will be executed immediately
     * @return this instance
     */
    public RxProperty<T> setValidator(@Nullable final Validator<T> validator, boolean validateNow) {
        this.validator = validator;
        if (validateNow) {
            forceValidate();
        }
        return this;
    }

    /**
     * Forcibly notifies the latest value of this {@code RxProperty} to all observers including the
     * bound view. This method ignores {@link Mode#DISTINCT_UNTIL_CHANGED}.
     */
    public void forceNotify() {
        valueField.set(get(), true);
    }

    /**
     * Invoke validation process.
     */
    public void forceValidate() {
        validationTrigger.onNext(get());
    }

    /**
     * Stops receiving notifications by the source {@link Observable} and send notifications to
     * observers of this {@code RxProperty}.
     */
    @Override
    public void unsubscribe() {
        if (isUnsubscribed) {
            return;
        }
        isUnsubscribed = true;

        // Terminate internal subjects.
        valueEmitter.onCompleted();
        errorEmitter.onCompleted();
        validationTrigger.onCompleted();

        // Notify default value to view.
        valueField.set(null);
        errorField.setValue(null);
        hasErrorField.set(false);

        // Unsubscribe internal subscriptions.
        subscriptions.unsubscribe();

        // Unbind a view observer.
        if (unbindView != null) {
            unbindView.call();
        }
        unbindView = null;
    }

    /**
     * Indicates whether this {@code RxProperty} is currently unsubscribed.
     *
     * @return {@code true} if this {@code RxProperty} has no {@link Observable} as source or is
     * currently unsubscribed, {@code false} otherwise
     */
    @Override
    public boolean isUnsubscribed() {
        return isUnsubscribed;
    }

    private void set(T value, boolean viewUpdate) {
        if (isDistinctUntilChanged && compare(value, get())) {
            return;
        }
        valueField.set(value, viewUpdate);
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * latest value of this property, use {@link RxProperty#get()} instead of this method.
     */
    @Deprecated
    @Override
    public ObservableField<T> getValue() {
        return valueField;
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * current validation error message this property, use
     * {@link RxProperty#getSummarizedErrorMessage()} instead of this method.
     */
    @Deprecated
    @Override
    public ObservableField<String> getError() {
        return errorField;
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * current validation error message of this property, use {@link RxProperty#hasErrors()}
     * instead of this method.
     */
    @Deprecated
    @Override
    public ObservableBoolean getHasError() {
        return hasErrorField;
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

    /**
     * Mode of the {@link RxProperty}.
     */
    public enum Mode {
        /**
         * All mode is off.
         */
        NONE,
        /**
         * If next value is same as current, not set and not notify.
         */
        DISTINCT_UNTIL_CHANGED,
        /**
         * Sends notification on the instance created and subscribed.
         */
        RAISE_LATEST_VALUE_ON_SUBSCRIBE
    }

    /**
     * Interface representing validator to test the value of {@link RxProperty}.
     *
     * @param <T> the type of {@link RxProperty}
     */
    public interface Validator<T> {
        /**
         * Validates the specified value.
         *
         * @param value a value to be tested
         * @return a list of error messages if validation failed; otherwise null or the empty list
         */
        List<String> validate(@Nullable final T value);

        /**
         * Summarize error messages.
         *
         * @param errorMessages a list of all error messages
         * @return a summarized error message if the specified error messages has an element;
         * otherwise null
         */
        String summarizeErrorMessages(@Nullable final List<String> errorMessages);
    }

    /**
     * Reimplementation of {@link ObservableField} for collaborating with {@link RxProperty}.
     *
     * @param <T> the type of value stored in this property.
     */
    private static class RxPropertyField<T> extends ObservableField<T> {
        private final RxProperty<T> parent;
        private T value;

        RxPropertyField(RxProperty<T> parent, T initialValue) {
            this.parent = parent;
            this.value = initialValue;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            set(value, false);
        }

        void set(T value, boolean viewUpdate) {
            this.value = value;
            if (viewUpdate) {
                notifyChange();
            }
            parent.validationTrigger.onNext(value);
            parent.valueEmitter.onNext(value);
        }
    }

    /**
     * Specialized {@link ObservableField} to represent a summarized validation error message of
     * {@link RxProperty}.
     */
    private static class RxPropertyErrorField extends ObservableField<String> {
        private String value;

        RxPropertyErrorField(String initialValue) {
            this.value = initialValue;
        }

        @Override
        public String get() {
            return value;
        }

        @Override
        public void set(String value) {
            throw new UnsupportedOperationException("RxProperty#error is read only.");
        }

        void setValue(String value) {
            if (!compare(value, this.value)) {
                this.value = value;
                notifyChange();
            }
        }
    }

    private static <T> boolean compare(T value1, T value2) {
        return (value1 == null && value2 == null) || (value1 != null && value1.equals(value2));
    }
}
