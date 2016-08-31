package jp.keita.kagurazaka.rxproperty;

import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.view.View;

import rx.functions.Action0;

public class RxCommandBinders {
    @BindingAdapter("rxCommandOnClick")
    public static void setOnClick(final View view, final RxCommand<Void> command) {
        // Set initial state.
        view.setEnabled(command.canExecute());

        // Observe click events.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (command.canExecute()) {
                    command.execute(null);
                }
            }
        });

        // Observe enabled changed events.
        final Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                view.setEnabled(command.canExecute());
            }
        };
        command.getEnabled().addOnPropertyChangedCallback(callback);

        command.setUnbindView(new Action0() {
            @Override
            public void call() {
                command.getEnabled().removeOnPropertyChangedCallback(callback);
            }
        });
    }
}
