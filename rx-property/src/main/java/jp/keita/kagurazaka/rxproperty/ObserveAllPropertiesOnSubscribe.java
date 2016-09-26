package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;

import rx.Subscriber;

final class ObserveAllPropertiesOnSubscribe<T extends Observable> implements rx.Observable.OnSubscribe<T> {
    private final T target;

    ObserveAllPropertiesOnSubscribe(T target) {
        this.target = target;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        final Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(target);
                }
            }
        };
        target.addOnPropertyChangedCallback(callback);

        subscriber.add(new OnPropertyChangedSubscription(target, callback));
    }
}
