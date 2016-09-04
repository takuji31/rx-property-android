package jp.keita.kagurazaka.rxproperty.sample.basics;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty;
import jp.keita.kagurazaka.rxproperty.RxCommand;
import jp.keita.kagurazaka.rxproperty.RxProperty;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class JavaBasicsViewModel extends BasicsViewModel {
    private final RxProperty<String> input;
    private final ReadOnlyRxProperty<String> output;
    private final RxCommand<Void> command;

    public JavaBasicsViewModel() {
        input = new RxProperty<>("")
                .setValidator(new Func1<String, String>() {
                    @Override
                    public String call(String it) {
                        return (TextUtils.isEmpty(it)) ? "Text must not be empty!" : null;
                    }
                }, false);

        output = new RxProperty<>(
                input.asObservable().map(new Func1<String, String>() {
                    @Override
                    public String call(String it) {
                        return it == null ? "" : it.toUpperCase();
                    }
                })
        );

        final Observable<Boolean> inputHasNoErrorsStream = input.onHasErrorsChanged()
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean hasError) {
                        return !hasError;
                    }
                })
                .skip(1);
        command = new RxCommand<>(inputHasNoErrorsStream, false);
        final Subscription commandSubscription = command.asObservable()
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        input.set("clicked!");
                    }
                });

        addSubscriptions(input, output, command, commandSubscription);
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
    public RxCommand<Void> getCommand() {
        return command;
    }
}
