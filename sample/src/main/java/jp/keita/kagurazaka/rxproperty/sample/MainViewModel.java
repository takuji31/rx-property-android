package jp.keita.kagurazaka.rxproperty.sample;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

import jp.keita.kagurazaka.rxproperty.RxCommand;
import jp.keita.kagurazaka.rxproperty.sample.basics.BasicsActivity;
import jp.keita.kagurazaka.rxproperty.sample.todo.TodoActivity;
import rx.Subscription;
import rx.functions.Action1;

public class MainViewModel extends ViewModelBase {
    public final RxCommand<Void> goToBasicsCommand = new RxCommand<>();

    public final RxCommand<Void> goToTodoCommand = new RxCommand<>();

    public MainViewModel(final Activity activity) {
        final WeakReference<Activity> ref = new WeakReference<>(activity);

        final Subscription s1 = goToBasicsCommand.asObservable()
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startActivity(ref, BasicsActivity.class);
                    }
                });

        final Subscription s2 = goToTodoCommand.asObservable()
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startActivity(ref, TodoActivity.class);
                    }
                });

        addSubscriptions(goToBasicsCommand, goToTodoCommand, s1, s2);
    }

    private static void startActivity(final WeakReference<Activity> ref, Class<?> cls) {
        final Activity activity = ref.get();
        if (activity != null) {
            activity.startActivity(new Intent(activity, cls));
        }
    }
}
