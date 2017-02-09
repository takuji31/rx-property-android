package jp.keita.kagurazaka.rxproperty.sample.todo;

import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import io.reactivex.functions.Cancellable;
import jp.keita.kagurazaka.rxproperty.RxProperty;

/**
 * Sample implementation of {@link BindingAdapter} for {@link RxProperty}.
 */
@SuppressWarnings("deprecation")
public class RxPropertyBinders {
    @BindingAdapter("rxPropertySelectedItemIndex")
    public static void setSelectedItemIndex(final Spinner spinner, final RxProperty<Integer> property) {
        // View -> RxProperty
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                property.setWithoutViewUpdate(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                property.setWithoutViewUpdate(-1);
            }
        });

        // RxProperty -> View
        final Observable.OnPropertyChangedCallback callback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                final Integer position = property.getValue().get();
                if (position != null) {
                    spinner.setSelection(position);
                }
            }
        };
        property.getValue().addOnPropertyChangedCallback(callback);
        property.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                property.getValue().removeOnPropertyChangedCallback(callback);
            }
        });
    }
}
