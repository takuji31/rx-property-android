package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;

import rx.Subscriber;
import rx.functions.Func1;

final class ObservePropertyOnSubscribe<T extends Observable, R> implements rx.Observable.OnSubscribe<R> {
    private final T target;
    private final int propertyId;
    private final Func1<T, R> getter;

    ObservePropertyOnSubscribe(T target, int propertyId, Func1<T, R> getter) {
        this.target = target;
        this.propertyId = propertyId;
        this.getter = getter;
    }

    @Override
    public void call(final Subscriber<? super R> subscriber) {
        final Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (i == propertyId && !subscriber.isUnsubscribed()) {
                    subscriber.onNext(getter.call(target));
                }
            }
        };
        target.addOnPropertyChangedCallback(callback);

        subscriber.add(new OnPropertyChangedSubscription(target, callback));
    }
}
