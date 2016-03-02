package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableField;

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
   * Gets the latest value of this {@code RxProperty}.
   *
   * @return the latest value stored in this {@code RxProperty}
   */
  T getValue();

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
   * @deprecated This is a magic method for Data Binding. Don't call it in your code.
   */
  @Deprecated
  ObservableField<T> getGet();

  /**
   * @deprecated This is a magic method for Data Binding. Don't call it in your code.
   */
  @Deprecated
  void setGet(ObservableField<T> value);
}
