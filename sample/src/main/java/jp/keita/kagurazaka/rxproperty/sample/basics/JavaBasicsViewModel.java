package jp.keita.kagurazaka.rxproperty.sample.basics;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import jp.keita.kagurazaka.rxproperty.Nothing;
import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty;
import jp.keita.kagurazaka.rxproperty.RxCommand;
import jp.keita.kagurazaka.rxproperty.RxProperty;

public class JavaBasicsViewModel extends BasicsViewModel {
    private final RxProperty<String> input;
    private final ReadOnlyRxProperty<String> output;
    private final RxCommand<Nothing> command;

    public JavaBasicsViewModel() {
        input = new RxProperty<>("")
                .setValidator(it -> TextUtils.isEmpty(it) ? "Text must not be empty!" : null);

        output = new ReadOnlyRxProperty<>(
                input.map(String::toUpperCase)
        );

        final Observable<Boolean> inputHasNoErrorsStream = input.onHasErrorsChanged()
                .map(hasError -> !hasError);
        command = new RxCommand<>(inputHasNoErrorsStream, false);

        final Disposable commandDisposable = command.subscribe(value -> input.set("clicked!"));

        addDisposables(input, output, command, commandDisposable);
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
    public RxCommand<Nothing> getCommand() {
        return command;
    }
}
