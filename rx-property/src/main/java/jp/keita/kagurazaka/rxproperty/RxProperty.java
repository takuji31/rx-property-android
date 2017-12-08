package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import jp.keita.kagurazaka.rxproperty.internal.Helper;

/**
 * Two-way bindable and observable property for Android Data Binding.
 *
 * @param <T> the type of the inner property
 */
public class RxProperty<T>
        extends Observable<T>
        implements android.databinding.Observable, Disposable {
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
        RAISE_LATEST_VALUE_ON_SUBSCRIBE;

        /**
         * Default mode set of {@link RxProperty}.
         */
        public static EnumSet<Mode> DEFAULT =
                EnumSet.of(Mode.DISTINCT_UNTIL_CHANGED, Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE);
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
        @Nullable
        List<String> validate(@NonNull final T value);

        /**
         * Summarize error messages.
         *
         * @param errorMessages a list of all error messages
         * @return a summarized error message if the specified error messages has an element;
         * otherwise null
         */
        @Nullable
        String summarizeErrorMessages(@NonNull final List<String> errorMessages);
    }

    /**
     * Interface representing a simple validator to test the value of {@link RxProperty}.
     *
     * @param <T> the type of {@link RxProperty}
     */
    public interface SimpleValidator<T> {
        /**
         * Validates the specified value.
         *
         * @param value a value to be tested
         * @return an error message if validation failed; otherwise null
         */
        @Nullable
        String validate(@NonNull final T value);
    }

    private final boolean isDistinctUntilChanged;

    // for value emitter
    @NonNull
    private final RxPropertyField<T> propertyField;

    @NonNull
    private final RxPropertyValueField<T> valueField;

    @NonNull
    private final Subject<T> valueEmitter;

    // for validator
    @NonNull
    private final RxPropertyErrorField errorField;

    @NonNull
    private final ObservableBoolean hasErrorField = new ObservableBoolean(false);

    @NonNull
    private final Subject<T> validationTrigger = PublishSubject.<T>create().toSerialized();

    @NonNull
    private final Subject<List<String>> errorEmitter;

    @NonNull
    private final Observable<List<String>> onErrorsChangedObservable;

    @NonNull
    private final Observable<String> onSummarizedErrorChangedObservable;

    @NonNull
    private final Observable<Boolean> onHasErrorsChangedObservable;

    @NonNull
    private List<String> currentErrors = Collections.emptyList();

    @NonNull
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);

    @Nullable
    private Cancellable cancellable = null;

    @NonNull
    private final Disposable sourceDisposable;

    @Nullable
    private Disposable validationTriggerDisposable = null;

    /**
     * Creates {@code RxProperty} without an initial value.
     */
    public RxProperty() {
        this(Observable.<T>never(), Maybe.<T>empty(), Mode.DEFAULT);
    }

    /**
     * Creates {@code RxProperty} with the initial value.
     *
     * @param initialValue the initial value of this {@code RxProperty}
     */
    public RxProperty(@NonNull T initialValue) {
        this(Observable.<T>never(), Helper.createInitialMaybe(initialValue), Mode.DEFAULT);
    }

    /**
     * Creates {@code RxProperty} with the specified mode.
     *
     * @param mode mode of this {@code RxProperty}
     */
    public RxProperty(@NonNull EnumSet<Mode> mode) {
        this(Observable.<T>never(), Maybe.<T>empty(), mode);
    }

    /**
     * Creates {@code RxProperty} with the initial value and the specified mode.
     *
     * @param initialValue the initial value of this {@code RxProperty}
     * @param mode         mode of this {@code RxProperty}
     */
    public RxProperty(@NonNull T initialValue, @NonNull EnumSet<Mode> mode) {
        this(Observable.<T>never(), Helper.createInitialMaybe(initialValue), mode);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable}.
     *
     * @param source a source {@link Observable} of this {@code RxProperty}
     */
    public RxProperty(@NonNull Observable<T> source) {
        this(source, Maybe.<T>empty(), Mode.DEFAULT);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable} with the initial value.
     *
     * @param source       a source {@link Observable} of this {@code RxProperty}
     * @param initialValue the initial value of this {@code RxProperty}
     */
    public RxProperty(@NonNull Observable<T> source, @NonNull T initialValue) {
        this(source, Helper.createInitialMaybe(initialValue), Mode.DEFAULT);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable} with the specified mode.
     *
     * @param source a source {@link Observable} of this {@code RxProperty}
     * @param mode   mode of this {@code RxProperty}
     */
    public RxProperty(@NonNull Observable<T> source, @NonNull EnumSet<Mode> mode) {
        this(source, Maybe.<T>empty(), mode);
    }

    /**
     * Creates {@code RxProperty} from the specified {@link Observable} with the initial value and the
     * specified mode.
     *
     * @param source       a source {@link Observable} of this {@code RxProperty}
     * @param initialValue the initial value of this {@code RxProperty}
     * @param mode         mode of this {@code RxProperty}
     */
    public RxProperty(@NonNull Observable<T> source, @NonNull T initialValue,
                      @NonNull EnumSet<Mode> mode) {
        this(source, Helper.createInitialMaybe(initialValue), mode);
    }

    private RxProperty(@NonNull Observable<T> source, @NonNull Maybe<T> initialMaybe,
                       @NonNull EnumSet<Mode> mode) {
        // null check
        Helper.checkNull(source, "source");
        Helper.checkNull(mode, "mode");

        // Initialize ObservableFields
        T initialValue = initialMaybe.blockingGet();
        propertyField = new RxPropertyField<>(initialValue);
        valueField = new RxPropertyValueField<>(this, initialValue);
        errorField = new RxPropertyErrorField("");

        // Set modes.
        isDistinctUntilChanged
                = !mode.contains(Mode.NONE) && mode.contains(Mode.DISTINCT_UNTIL_CHANGED);
        boolean isRaiseLatestValueOnSubscribe
                = !mode.contains(Mode.NONE) && mode.contains(Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE);

        // Create emitters.
        if (isRaiseLatestValueOnSubscribe) {
            valueEmitter = (initialValue != null ?
                    BehaviorSubject.createDefault(initialValue) :
                    BehaviorSubject.<T>create()).toSerialized();
        } else {
            valueEmitter = PublishSubject.<T>create().toSerialized();
        }

        errorEmitter = (isRaiseLatestValueOnSubscribe ?
                BehaviorSubject.<List<String>>create() :
                PublishSubject.<List<String>>create()
        ).toSerialized();

        // Create observables for notifying errors.
        onErrorsChangedObservable = errorEmitter.distinctUntilChanged().share();
        onSummarizedErrorChangedObservable = onErrorsChangedObservable
                .map(new Function<List<String>, String>() {
                    @Override
                    public String apply(List<String> strings) {
                        return getSummarizedErrorMessage();
                    }
                }).distinctUntilChanged().share();
        onHasErrorsChangedObservable = onErrorsChangedObservable
                .map(new Function<List<String>, Boolean>() {
                    @Override
                    public Boolean apply(List<String> strings) {
                        return hasErrors();
                    }
                }).distinctUntilChanged().share();

        // Subscribe the source observable.
        sourceDisposable = source.subscribeWith(new DisposableObserver<T>() {
            @Override
            public void onNext(T value) {
                set(value);
            }

            @Override
            public void onError(Throwable e) {
                valueEmitter.onError(e);
                RxProperty.this.dispose();
            }

            @Override
            public void onComplete() {
                valueEmitter.onComplete();
                RxProperty.this.dispose();
            }
        });

        // Register RxJava plugins.
        RxJavaPlugins.onAssembly(this);
    }

    /**
     * Gets the latest value of this {@code RxProperty}.
     *
     * @return the latest value stored in this {@code RxProperty}
     */
    @Nullable
    public T get() {
        return valueField.get();
    }

    /**
     * Sets the specified value to this {@code RxProperty}. The change will be notified to both
     * bound view and observers of this {@code RxProperty}.
     *
     * @param value a value to set
     */
    public void set(@NonNull T value) {
        set(value, true);
    }

    /**
     * Sets the specified value to this {@code RxProperty}. The change will be notified to
     * observers of this {@code RxProperty} but not affect the bound view.
     *
     * @param value a value to set
     */
    public void setWithoutViewUpdate(@NonNull T value) {
        set(value, false);
    }

    /**
     * Gets an event {@link Observable} of validation error messages.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit current validation error messages of this
     * {@code RxProperty}
     */
    public Observable<List<String>> onErrorsChanged() {
        return onErrorsChangedObservable;
    }

    /**
     * Gets an event {@link Observable} of summarized validation error messages.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit summarized validation error messages of this
     * {@code RxProperty}
     */
    public Observable<String> onSummarizedErrorChanged() {
        return onSummarizedErrorChangedObservable;
    }

    /**
     * Gets an event {@link Observable} to emit {@link RxProperty#hasErrors} when it is
     * changed.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit {@link RxProperty#hasErrors} when it is
     * changed
     */
    public Observable<Boolean> onHasErrorsChanged() {
        return onHasErrorsChangedObservable;
    }

    /**
     * Gets the current validation error messages of this {@code RxProperty}.
     *
     * @return the current validation error messages if validation failed; otherwise the empty list
     */
    @NonNull
    public List<String> getErrorMessages() {
        return currentErrors;
    }

    /**
     * Gets the current summarized validation error message of this {@code RxProperty}.
     *
     * @return the current summarized validation error message if validation failed; otherwise
     * the empty string
     */
    @NonNull
    public String getSummarizedErrorMessage() {
        return errorField.get();
    }

    /**
     * Returns whether this {@code RxProperty} has validation errors.
     *
     * @return true if this {@code RxProperty} has validation errors; otherwise else
     */
    public boolean hasErrors() {
        return !currentErrors.isEmpty();
    }

    /**
     * Sets the specified validator to this {@code RxProperty}.
     * <p>
     * Validator should return an error message if the validation failed; otherwise null.
     *
     * @param validator a validator to test the value of this {@code RxProperty}
     * @return this instance
     */
    public RxProperty<T> setValidator(@Nullable final SimpleValidator<T> validator) {
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
            @Nullable final SimpleValidator<T> validator,
            boolean validateNow) {
        if (validator == null) {
            return setValidator((Validator<T>) null, validateNow);
        }

        final Validator<T> wrapped = new Validator<T>() {
            @Nullable
            @Override
            public List<String> validate(@NonNull final T value) {
                String message;
                try {
                    message = validator.validate(value);
                } catch (Exception e) {
                    message = e.getLocalizedMessage();
                }
                if (message == null) {
                    return null;
                }
                return Collections.singletonList(message);
            }

            @Nullable
            @Override
            public String summarizeErrorMessages(@NonNull final List<String> errorMessages) {
                if (errorMessages.isEmpty()) {
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
        if (validationTriggerDisposable != null) {
            validationTriggerDisposable.dispose();
        }

        if (validator == null) {
            clearErrors();
            return this;
        }

        validationTriggerDisposable = validationTrigger.subscribe(new Consumer<T>() {
            @Override
            public void accept(T value) {
                List<String> errors;
                String summarized = null;
                try {
                    errors = validator.validate(value);
                    if (errors != null) {
                        summarized = validator.summarizeErrorMessages(errors);
                    }
                    if (summarized == null) {
                        summarized = "";
                    }
                } catch (Exception e) {
                    summarized = e.getLocalizedMessage();
                    errors = Collections.singletonList(summarized);
                }

                if (errors == null || errors.isEmpty()) {
                    clearErrors();
                } else {
                    currentErrors = errors;
                    errorField.setValue(summarized);
                    hasErrorField.set(true);
                    errorEmitter.onNext(errors);
                }
            }
        });

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
        T latestValue = get();
        if (latestValue != null) {
            validationTrigger.onNext(latestValue);
        }
    }

    /**
     * Stops receiving notifications by the source {@link Observable} and send notifications to
     * observers of this {@code RxProperty}.
     */
    @Override
    public void dispose() {
        if (isDisposed.compareAndSet(false, true)) {
            // Terminate internal subjects.
            Helper.safeComplete(valueEmitter);
            Helper.safeComplete(errorEmitter);
            Helper.safeComplete(validationTrigger);

            // Dispose internal disposables.
            Helper.safeDispose(sourceDisposable);
            Helper.safeDispose(validationTriggerDisposable);

            // Unbind a view observer.
            Helper.safeCancel(cancellable);
            cancellable = null;
        }
    }

    /**
     * Indicates whether this {@code RxProperty} is currently disposed.
     *
     * @return {@code true} if this {@code RxProperty} has no {@link Observable} as source or is
     * currently disposed, {@code false} otherwise
     */
    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        propertyField.addOnPropertyChangedCallback(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        propertyField.removeOnPropertyChangedCallback(callback);
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        valueEmitter.subscribe(observer);
    }

    private void set(@NonNull T value, boolean viewUpdate) {
        if (isDisposed()) {
            return;
        }

        if (isDistinctUntilChanged && Helper.compare(value, get())) {
            return;
        }
        valueField.set(value, viewUpdate);
    }

    private void clearErrors() {
        if (hasErrors()) {
            currentErrors = Collections.emptyList();
            errorField.setValue("");
            hasErrorField.set(false);
            errorEmitter.onNext(currentErrors);
        }
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * latest value of this property, use {@link RxProperty#get()} instead of this method.
     */
    @Deprecated
    public ObservableField<T> getValue() {
        return valueField;
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * current validation error message this property, use
     * {@link RxProperty#getSummarizedErrorMessage()} instead of this method.
     */
    @Deprecated
    public ObservableField<String> getError() {
        return errorField;
    }

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * current validation error message of this property, use {@link RxProperty#hasErrors()}
     * instead of this method.
     */
    @Deprecated
    public ObservableBoolean getHasError() {
        return hasErrorField;
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
     * Reimplementation of {@link ObservableField} for collaborating with {@link RxProperty}.
     *
     * @param <T> the type of value stored in this property
     */
    private static class RxPropertyField<T> extends ObservableField<T> {
        private T value;

        RxPropertyField(T initialValue) {
            this.value = initialValue;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            this.value = value;
            notifyChange();
        }
    }

    /**
     * Specialized {@link ObservableField} to represent a value of {@link RxProperty}, which is used
     * in view binding.
     *
     * @param <T> the type of value stored in this property
     */
    private static class RxPropertyValueField<T> extends ObservableField<T> {
        private final RxProperty<T> parent;
        private T value;

        RxPropertyValueField(RxProperty<T> parent, T initialValue) {
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
            parent.propertyField.set(value);

            if (viewUpdate) {
                notifyChange();
            }

            parent.validationTrigger.onNext(value);
            parent.valueEmitter.onNext(value);
        }
    }

    /**
     * Specialized {@link ObservableField} to represent a summarized validation error message of
     * {@link RxProperty}, which used in view binding.
     */
    private static class RxPropertyErrorField extends ObservableField<String> {
        private String value;

        RxPropertyErrorField(@NonNull String initialValue) {
            this.value = initialValue;
        }

        @Override
        @NonNull
        public String get() {
            return value;
        }

        @Override
        public void set(String value) {
            throw new UnsupportedOperationException("RxProperty#error is read only.");
        }

        void setValue(@NonNull String value) {
            if (!Helper.compare(value, this.value)) {
                this.value = value;
                notifyChange();
            }
        }
    }
}
