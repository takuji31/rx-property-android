package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@SuppressWarnings("deprecation")
public class RxCommandTest {

    public static class NoParameterCommand {
        Subject<Boolean> canExecuteSource;

        @Before
        public void setUp() {
            canExecuteSource = PublishSubject.create();
        }

        @Test
        public void canExecuteReturnsTrueWhenConstructWithoutArguments() {
            // when
            RxCommand<NoParameter> command = new RxCommand<>();

            // then
            assertThat(command.canExecute(), is(true));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledReturnsTrueWhenConstructWithoutArguments() {
            // when
            RxCommand<NoParameter> command = new RxCommand<>();

            // then
            assertThat(command.getEnabled().get(), is(true));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledEmitsNoValuesWhenConstructWithoutArguments() {
            // when
            RxCommand<NoParameter> command = new RxCommand<>();

            // then
            observableBooleanTestObserver(command)
                    .assertNoValues()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void canExecuteInitiallyReturnsTrueWhenConstructWithSourceObservable() {
            // when
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);

            // then
            assertThat(command.canExecute(), is(true));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledInitiallyReturnsTrueWhenConstructWithSourceObservable() {
            // when
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);

            // then
            assertThat(command.getEnabled().get(), is(true));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledEmitsNoValuesTrueWhenConstructWithSourceObservable() {
            // when
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);

            // then
            observableBooleanTestObserver(command)
                    .assertNoValues()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void canExecuteInitiallyReturnsSpecifiedValueWhenConstructWithInitialValue() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource, false);

            // then
            assertThat(command.canExecute(), is(false));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledInitiallyReturnsSpecifiedValueWhenConstructWithInitialValue() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource, false);

            // then
            assertThat(command.getEnabled().get(), is(false));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledEmitsNoValuesWhenConstructWithInitialValue() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource, false);

            // then
            observableBooleanTestObserver(command)
                    .assertNoValues()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void initialValueOfCanExecuteIsOverwrittenBySourceObservable() {
            // given
            Subject<Boolean> source = BehaviorSubject.createDefault(false);
            RxCommand<NoParameter> command = new RxCommand<>(source);

            // then
            assertThat(command.canExecute(), is(false));

            // after
            command.dispose();
        }

        @Test
        public void initialValueOfGetEnabledIsOverwrittenBySourceObservable() {
            // given
            Subject<Boolean> source = BehaviorSubject.createDefault(false);
            RxCommand<NoParameter> command = new RxCommand<>(source);

            // then
            assertThat(command.getEnabled().get(), is(false));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledEmitsNoValuesWhenInitialValueIsOverwrittenBySourceObservable() {
            // given
            Subject<Boolean> source = BehaviorSubject.createDefault(false);
            RxCommand<NoParameter> command = new RxCommand<>(source);

            // then
            observableBooleanTestObserver(command)
                    .assertNoValues()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void canExecuteFollowsSourceObservable() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);

            // when
            canExecuteSource.onNext(false);

            // then
            assertThat(command.canExecute(), is(false));

            // when
            canExecuteSource.onNext(true);

            // then
            assertThat(command.canExecute(), is(true));

            // after
            command.dispose();
        }

        @Test
        public void getEnabledFollowsSourceObservable() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);
            TestObserver<Boolean> testObserver = observableBooleanTestObserver(command);

            // when
            canExecuteSource.onNext(false);
            canExecuteSource.onNext(true);
            canExecuteSource.onNext(true);
            canExecuteSource.onNext(false);

            // then
            testObserver.assertValues(false, true, false)
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void throwsErrorWhenSourceObservableEmitsError() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);
            TestObserver<NoParameter> testObserver = command.test();

            // when
            canExecuteSource.onError(new RuntimeException("Error in the source observable"));

            // then
            testObserver.assertFailureAndMessage(
                    RuntimeException.class, "Error in the source observable")
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void autoDisposedWhenSourceObservableEmitsError() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);

            // when
            canExecuteSource.onError(new RuntimeException("Error in the source observable"));

            // then
            assertThat(command.isDisposed(), is(true));
        }

        @Test
        public void emitsOnCompleteWhenSourceObservableIsCompleted() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);
            TestObserver<NoParameter> testObserver = command.test();

            // when
            canExecuteSource.onComplete();

            // then
            testObserver.assertResult()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void autoDisposedWhenSourceObservableIsCompleted() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource);

            // when
            canExecuteSource.onComplete();

            // then
            assertThat(command.isDisposed(), is(true));
        }

        @Test
        public void emitsNothingWhenExecutes() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>();
            TestObserver<NoParameter> testObserver = command.test();

            // when
            command.execute(NoParameter.INSTANCE);
            command.execute(NoParameter.INSTANCE);

            // then
            testObserver.assertSubscribed()
                    .assertValues(NoParameter.INSTANCE, NoParameter.INSTANCE)
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void emitsNothingWhenExecutesAlthoughCanExecuteIsFalse() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>(canExecuteSource, false);
            TestObserver<NoParameter> testObserver = command.test();

            // when
            command.execute(NoParameter.INSTANCE);

            // then
            testObserver.assertSubscribed()
                    .assertValue(NoParameter.INSTANCE)
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void emitValueWhenBoundTriggerEmitsValue() {
            // given
            Subject<NoParameter> trigger = PublishSubject.create();
            RxCommand<NoParameter> command = new RxCommand<NoParameter>()
                    .bindTrigger(trigger);
            TestObserver<NoParameter> testObserver = command.test();

            // when
            trigger.onNext(NoParameter.INSTANCE);

            // then
            testObserver.assertSubscribed()
                    .assertValue(NoParameter.INSTANCE)
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void throwsErrorWhenBoundTriggerEmitsError() {
            // given
            Subject<NoParameter> trigger = PublishSubject.create();
            RxCommand<NoParameter> command = new RxCommand<NoParameter>()
                    .bindTrigger(trigger);
            TestObserver<NoParameter> testObserver = command.test();

            // when
            trigger.onError(new RuntimeException("Error in the trigger observable"));

            // then
            testObserver.assertFailureAndMessage(
                    RuntimeException.class, "Error in the trigger observable")
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void autoDisposedWhenBoundTriggerEmitsError() {
            // given
            Subject<NoParameter> trigger = PublishSubject.create();
            RxCommand<NoParameter> command = new RxCommand<NoParameter>()
                    .bindTrigger(trigger);

            // when
            trigger.onError(new RuntimeException("Error in the trigger observable"));

            // then
            assertThat(command.isDisposed(), is(true));
        }

        @Test
        public void emitsOnCompleteWhenBoundTriggerIsCompleted() {
            // given
            Subject<NoParameter> trigger = PublishSubject.create();
            RxCommand<NoParameter> command = new RxCommand<NoParameter>()
                    .bindTrigger(trigger);
            TestObserver<NoParameter> testObserver = command.test();

            // when
            trigger.onNext(NoParameter.INSTANCE);
            trigger.onComplete();

            // then
            testObserver.assertResult(NoParameter.INSTANCE)
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void autoDisposedWhenBoundTriggerIsCompleted() {
            // given
            Subject<NoParameter> trigger = PublishSubject.create();
            RxCommand<NoParameter> command = new RxCommand<NoParameter>()
                    .bindTrigger(trigger);

            // when
            trigger.onComplete();

            // then
            assertThat(command.isDisposed(), is(true));
        }

        @Test
        public void triggerBindingCanExecuteMoreThanOnce() {
            // given
            Subject<NoParameter> firstTrigger = PublishSubject.create();
            Subject<NoParameter> secondTrigger = PublishSubject.create();
            RxCommand<NoParameter> command = new RxCommand<>();
            TestObserver<NoParameter> testObserver = command.test();

            // when
            command.bindTrigger(firstTrigger);
            firstTrigger.onNext(NoParameter.INSTANCE);
            command.bindTrigger(secondTrigger);
            firstTrigger.onNext(NoParameter.INSTANCE);
            secondTrigger.onNext(NoParameter.INSTANCE);

            // then
            testObserver.assertSubscribed()
                    .assertValues(NoParameter.INSTANCE, NoParameter.INSTANCE)
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            // after
            command.dispose();
        }

        @Test
        public void isDisposedReturnsTrueWhenDisposed() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>();
            TestObserver<NoParameter> testObserver = command.test();

            // when
            command.dispose();

            // then
            assertThat(command.isDisposed(), is(true));

            // after
            testObserver.dispose();
        }

        @Test
        public void canDisposeMoreThanOnce() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>();
            TestObserver<NoParameter> testObserver = command.test();

            // when
            command.dispose();
            command.dispose();
            command.dispose();

            // then
            assertThat(command.isDisposed(), is(true));

            // after
            testObserver.dispose();
        }

        @Test
        public void emitsOnCompleteWhenDisposed() {
            // given
            RxCommand<NoParameter> command = new RxCommand<>();
            TestObserver<NoParameter> testObserver = command.test();

            // when
            command.execute(NoParameter.INSTANCE);
            command.dispose();

            // then
            testObserver.assertResult(NoParameter.INSTANCE)
                    .dispose();
        }

        @Test
        public void unbindViewWillBeExecutedWhenDisposed() throws Exception {
            // given
            RxCommand<NoParameter> command = new RxCommand<>();
            Cancellable mockCancellable = Mockito.mock(Cancellable.class);
            command.setCancellable(mockCancellable);
            verify(mockCancellable, never()).cancel();

            // when
            command.dispose();

            // then
            verify(mockCancellable).cancel();
        }

        @Test
        public void ignoreExceptionByUnbindView() throws Exception {
            // given
            RxCommand<NoParameter> command = new RxCommand<>();
            Cancellable mockCancellable = Mockito.mock(Cancellable.class);
            doThrow(new RuntimeException("Error in unbindView")).when(mockCancellable).cancel();
            command.setCancellable(mockCancellable);
            verify(mockCancellable, never()).cancel();

            // when
            command.dispose();

            // then
            verify(mockCancellable).cancel();
        }
    }

    public static class ParameterCommand {
        @Test
        public void emitsValueWhenExecutes() {
            // given
            RxCommand<String> command = new RxCommand<>();
            TestObserver<String> testObserver = command.test();

            // when
            command.execute("John Smith");

            // then
            testObserver.assertSubscribed()
                    .assertValues("John Smith")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            // after
            command.dispose();
        }
    }

    private static TestObserver<Boolean> observableBooleanTestObserver(RxCommand command) {
        return Observe.allPropertiesOf(command.getEnabled())
                .map(new Function<ObservableBoolean, Boolean>() {
                    @Override
                    public Boolean apply(ObservableBoolean value) throws Exception {
                        return value.get();
                    }
                })
                .test();
    }
}
