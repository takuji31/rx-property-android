package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import java.util.List;

import rx.Observable;
import rx.Subscription;

/**
 * One-way bindable and observable property for Android Data Binding.
 */
public interface ReadOnlyRxProperty<T> extends Subscription {
    /**
     * Gets a hot {@link Observable} to emit values of this {@code RxProperty}.
     *
     * @return a hot {@link Observable} to emit values of this {@code RxProperty}
     */
    Observable<T> asObservable();

    /**
     * Gets an event {@link Observable} of validation error messages.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit current validation error messages of this
     * {@code RxProperty}
     */
    Observable<List<String>> onErrorsChanged();

    /**
     * Gets an event {@link Observable} of summarized validation error messages.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit summarized validation error messages of this
     * {@code RxProperty}
     */
    Observable<String> onSummarizedErrorChanged();

    /**
     * Gets an event {@link Observable} to emit {@link ReadOnlyRxProperty#hasErrors} when it is
     * changed.
     * <p>
     * If no validators is set to this {@code RxProperty}, this event never emits messages.
     *
     * @return a hot {@link Observable} to emit {@link ReadOnlyRxProperty#hasErrors} when it is
     * changed
     */
    Observable<Boolean> onHasErrorsChanged();

    /**
     * Gets the latest value of this {@code RxProperty}.
     *
     * @return the latest value stored in this {@code RxProperty}
     */
    T get();

    /**
     * Gets the current validation error messages of this {@code RxProperty}.
     *
     * @return the current validation error messages if validation failed; otherwise null
     */
    List<String> getErrorMessages();

    /**
     * Gets the current summarized validation error message of this {@code RxProperty}.
     *
     * @return the current summarized validation error message if validation failed; otherwise null
     */
    String getSummarizedErrorMessage();

    /**
     * Returns whether this {@code RxProperty} has validation errors.
     *
     * @return true if this {@code RxProperty} has validation errors; otherwise else
     */
    boolean hasErrors();

    /**
     * Stops receiving notifications by the source {@link Observable} and send notifications to
     * observers of this {@code RxProperty}.
     */
    void unsubscribe();

    /**
     * Indicates whether this {@code RxProperty} is currently unsubscribed.
     *
     * @return {@code true} if this {@code RxProperty} has no {@link Observable} as source or is
     * currently unsubscribed, {@code false} otherwise
     */
    boolean isUnsubscribed();

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * latest value of this property, use {@link ReadOnlyRxProperty#get()} instead of this method.
     */
    @Deprecated
    ObservableField<T> getValue();

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * current validation error message of this property, use
     * {@link ReadOnlyRxProperty#getSummarizedErrorMessage()} instead of this method.
     */
    @Deprecated
    ObservableField<String> getError();

    /**
     * @deprecated This is a magic method for Data Binding. Don't call it in your code. To get the
     * current validation error message of this property, use
     * {@link ReadOnlyRxProperty#hasErrors()} instead of this method.
     */
    @Deprecated
    ObservableBoolean getHasError();
}
