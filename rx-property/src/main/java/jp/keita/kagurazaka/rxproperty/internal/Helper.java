package jp.keita.kagurazaka.rxproperty.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.Maybe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.subjects.Subject;

public final class Helper {
    public static <T> void checkNull(@Nullable T value, @NonNull String name) {
        if (value == null) {
            throw new NullPointerException(name + " must not be null.");
        }
    }

    public static <T> Maybe<T> createInitialMaybe(@Nullable T initialValue) {
        checkNull(initialValue, "initialValue");
        return Maybe.just(initialValue);
    }

    public static <T> boolean compare(@Nullable T value1, @Nullable T value2) {
        return (value1 == null && value2 == null) || (value1 != null && value1.equals(value2));
    }

    public static void safeDispose(@Nullable Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public static void safeCancel(@Nullable Cancellable cancellable) {
        if (cancellable == null) {
            return;
        }

        try {
            cancellable.cancel();
        } catch (Exception e) {
            // Ignore the exception.
        }
    }

    public static <T> void safeComplete(@NonNull Subject<T> emitter) {
        if (!emitter.hasThrowable() && !emitter.hasComplete()) {
            emitter.onComplete();
        }
    }

    private Helper() {
        throw new AssertionError("No instances.");
    }
}
