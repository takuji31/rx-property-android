package jp.keita.kagurazaka.rxproperty;

import android.databinding.BindingAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Custom {@link BindingAdapter} holder for {@link RxProperty}.
 */
public class RxPropertySetter {
  @BindingAdapter("app:text")
  public static void addTextChangedListener(
      final EditText view, final RxProperty<String> variable) {

    view.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        variable.setValueAsView(s.toString());
      }
    });
  }
}
