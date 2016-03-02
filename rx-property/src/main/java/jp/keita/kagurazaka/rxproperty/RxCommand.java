package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Command class implementation in "Command pattern" for Android Data Binding. Whether {@code
 * RxCommand} can execute is detected by boolean {@link Observable}.
 */
public class RxCommand<T> implements Subscription {
  private final ObservableBoolean canExecuteFlag;
  private final T listener;
  private Subscription sourceSubscription = null;
  private final Subject<Throwable, Throwable> errorNotifier = PublishSubject.create();

  /**
   * Creates {@code RxCommand} which is always enabled.
   */
  public RxCommand() {
    this((Observable<Boolean>) null);
  }

  /**
   * Creates {@code RxCommand} from the specified {@link Observable}.
   *
   * @param source an {@link Observable} representing that can this {@code RxCommand} execute
   */
  public RxCommand(Observable<Boolean> source) {
    this(source, true);
  }

  /**
   * Creates {@code RxCommand} from the specified {@link Observable} with the initial state.
   *
   * @param source       an {@link Observable} to emit whether this {@code RxCommand} can execute
   * @param initialValue whether this {@code RxCommand} is enabled at first
   */
  public RxCommand(Observable<Boolean> source, boolean initialValue) {
    this(source, initialValue, null);
  }

  /**
   * Creates {@code RxCommand} to execute the specified command.
   *
   * @param command a callback called when this {@code RxCommand} is executed
   */
  public RxCommand(T command) {
    this(null, command);
  }

  /**
   * Creates {@code RxCommand} to execute the specified command from the specified {@link
   * Observable}.
   *
   * @param source  an {@link Observable} to emit whether this {@code RxCommand} can execute
   * @param command a callback called when this {@code RxCommand} is executed
   */
  public RxCommand(Observable<Boolean> source, T command) {
    this(source, true, command);
  }

  /**
   * Creates {@code RxCommand} to execute the specified command from the specified {@link
   * Observable} with the initial state.
   *
   * @param source       an {@link Observable} to emit whether this {@code RxCommand} can execute
   * @param initialValue whether this {@code RxCommand} is enabled at first
   * @param command      a callback called when this {@code RxCommand} is executed
   */
  public RxCommand(Observable<Boolean> source, boolean initialValue, T command) {
    canExecuteFlag = new ObservableBoolean(initialValue);
    listener = command;
    if (source == null) {
      sourceSubscription = null;
    } else {
      sourceSubscription = source.distinctUntilChanged()
          .subscribe(new Subscriber<Boolean>() {
            @Override
            public void onNext(Boolean value) {
              canExecuteFlag.set(value);
            }

            @Override
            public void onError(Throwable e) {
              errorNotifier.onNext(e);
            }

            @Override
            public void onCompleted() {
              errorNotifier.onCompleted();
            }
          });
    }
  }

  /**
   * Gets whether this {@code RxCommand} can execute currently.
   *
   * @return {@code true} if this {@code RxCommand} can execute, {@code false} otherwise
   */
  public boolean canExecute() {
    return canExecuteFlag.get();
  }

  /**
   * Gets a hot {@link Observable} to emit errors propagated by the source {@link Observable}.
   *
   * @return a hot {@link Observable} to emit errors propagated by the source {@link Observable}
   */
  public Observable<Throwable> observeErrors() {
    return errorNotifier.asObservable();
  }

  /**
   * Stops receiving notifications by the source {@link Observable} and sending notifications to
   * error observers of this {@code RxCommand}.
   */
  @Override
  public void unsubscribe() {
    if (!isUnsubscribed()) {
      sourceSubscription.unsubscribe();
    }
    sourceSubscription = null;
  }

  /**
   * Indicates whether this {@code RxCommand} is currently unsubscribed.
   *
   * @return {@code true} if this {@code RxCommand} has no {@link Observable} as source or is
   * currently unsubscribed, {@code false} otherwise
   */
  @Override
  public boolean isUnsubscribed() {
    return sourceSubscription == null || sourceSubscription.isUnsubscribed();
  }

  /**
   * @deprecated This is a magic method for Data Binding. Don't call it in your code.
   */
  @Deprecated
  public T getExec() {
    return listener;
  }

  /**
   * @deprecated This is a magic method for Data Binding. Don't call it in your code.
   */
  @Deprecated
  public void setExec(T value) {
    throw new UnsupportedOperationException();
  }

  /**
   * @deprecated This is a magic method for Data Binding. Don't call it in your code.
   */
  @Deprecated
  public ObservableBoolean getEnabled() {
    return canExecuteFlag;
  }

  /**
   * @deprecated This is a magic method for Data Binding. Don't call it in your code.
   */
  @Deprecated
  public void setEnabled(ObservableBoolean value) {
    throw new UnsupportedOperationException();
  }
}
