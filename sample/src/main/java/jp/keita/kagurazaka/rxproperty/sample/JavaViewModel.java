package jp.keita.kagurazaka.rxproperty.sample;

import android.text.TextUtils;
import android.view.View;

import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty;
import jp.keita.kagurazaka.rxproperty.RxCommand;
import jp.keita.kagurazaka.rxproperty.RxProperty;

import org.jetbrains.annotations.NotNull;

public class JavaViewModel extends ViewModel {
  private final RxProperty<String> input;
  private final ReadOnlyRxProperty<String> output;
  private final RxCommand<View.OnClickListener> command;

  public JavaViewModel() {
    input = new RxProperty<>("");

    output = new RxProperty<>(
        input.asObservable()
            .map(it -> it == null ? "" : it.toUpperCase())
    );

    command = new RxCommand<>(
        output.asObservable()
            .map(it -> !TextUtils.isEmpty(it)),
        view -> { input.setValue("clicked"); }
    );

    addSubscriptions(input, output, command);
  }

  @NotNull
  @Override
  public RxProperty<String> getInput() {
    return input;
  }

  @NotNull
  @Override
  public ReadOnlyRxProperty<String> getOutput() {
    return output;
  }

  @NotNull
  @Override
  public RxCommand<View.OnClickListener> getCommand() {
    return command;
  }
}
