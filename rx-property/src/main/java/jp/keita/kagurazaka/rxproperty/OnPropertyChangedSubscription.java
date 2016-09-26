package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;

import rx.Subscription;

final class OnPropertyChangedSubscription implements Subscription {
    private final Observable observable;
    private final Observable.OnPropertyChangedCallback callback;
    private boolean isUnsubscribed = false;

    OnPropertyChangedSubscription(Observable observable, Observable.OnPropertyChangedCallback callback) {
        this.observable = observable;
        this.callback = callback;
    }

    @Override
    public boolean isUnsubscribed() {
        return isUnsubscribed;
    }

    @Override
    public void unsubscribe() {
        if (isUnsubscribed) {
            return;
        }
        isUnsubscribed = true;

        observable.removeOnPropertyChangedCallback(callback);
    }
}
