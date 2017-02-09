package jp.keita.kagurazaka.rxproperty.sample;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import jp.keita.kagurazaka.rxproperty.Nothing;
import jp.keita.kagurazaka.rxproperty.RxCommand;
import jp.keita.kagurazaka.rxproperty.sample.basics.BasicsActivity;
import jp.keita.kagurazaka.rxproperty.sample.todo.TodoActivity;

public class MainViewModel extends ViewModelBase {
    public final RxCommand<Nothing> goToBasicsCommand = new RxCommand<>();

    public final RxCommand<Nothing> goToTodoCommand = new RxCommand<>();

    public MainViewModel(final Activity activity) {
        final WeakReference<Activity> ref = new WeakReference<>(activity);

        final Disposable d1 = goToBasicsCommand
                .subscribe(new Consumer<Nothing>() {
                    @Override
                    public void accept(Nothing value) {
                        startActivity(ref, BasicsActivity.class);
                    }
                });

        final Disposable d2 = goToTodoCommand
                .subscribe(new Consumer<Nothing>() {
                    @Override
                    public void accept(Nothing value) {
                        startActivity(ref, TodoActivity.class);
                    }
                });

        addDisposables(goToBasicsCommand, goToTodoCommand, d1, d2);
    }

    private static void startActivity(final WeakReference<Activity> ref, Class<?> cls) {
        final Activity activity = ref.get();
        if (activity != null) {
            activity.startActivity(new Intent(activity, cls));
        }
    }
}
