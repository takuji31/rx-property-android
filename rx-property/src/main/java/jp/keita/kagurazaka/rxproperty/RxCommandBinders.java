package jp.keita.kagurazaka.rxproperty;

import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.view.View;

import io.reactivex.functions.Cancellable;

@SuppressWarnings("deprecation")
public final class RxCommandBinders {
    @BindingAdapter("rxCommandOnClick")
    public static void setOnClick(final View view, final RxCommand<Nothing> command) {
        // Set initial state.
        view.setEnabled(command.canExecute());

        // Observe click events.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (command.canExecute()) {
                    command.execute(Nothing.INSTANCE);
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

        command.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                command.getEnabled().removeOnPropertyChangedCallback(callback);
            }
        });
    }
}
