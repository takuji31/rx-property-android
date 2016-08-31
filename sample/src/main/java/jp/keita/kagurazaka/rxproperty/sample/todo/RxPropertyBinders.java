package jp.keita.kagurazaka.rxproperty.sample.todo;

import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import jp.keita.kagurazaka.rxproperty.RxProperty;
import rx.functions.Action0;

/**
 * Sample implementation of {@link BindingAdapter} for {@link RxProperty}.
 */
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
                property.setWithoutViewUpdate(null);
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
        property.setUnbindView(new Action0() {
            @Override
            public void call() {
                property.getValue().removeOnPropertyChangedCallback(callback);
            }
        });
    }
}
