package jp.keita.kagurazaka.rxproperty.sample;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.Disposable;
import jp.keita.kagurazaka.rxproperty.NoParameter;
import jp.keita.kagurazaka.rxproperty.RxCommand;
import jp.keita.kagurazaka.rxproperty.sample.basics.BasicsActivity;
import jp.keita.kagurazaka.rxproperty.sample.todo.TodoActivity;

public class MainViewModel extends ViewModelBase {
    public final RxCommand<NoParameter> goToBasicsCommand = new RxCommand<>();

    public final RxCommand<NoParameter> goToTodoCommand = new RxCommand<>();

    public MainViewModel(final Activity activity) {
        final WeakReference<Activity> ref = new WeakReference<>(activity);

        final Disposable d1 = goToBasicsCommand
                .subscribe(value -> startActivity(ref, BasicsActivity.class));

        final Disposable d2 = goToTodoCommand
                .subscribe(value -> startActivity(ref, TodoActivity.class));

        addDisposables(goToBasicsCommand, goToTodoCommand, d1, d2);
    }

    private static void startActivity(final WeakReference<Activity> ref, Class<?> cls) {
        final Activity activity = ref.get();
        if (activity != null) {
            activity.startActivity(new Intent(activity, cls));
        }
    }
}
