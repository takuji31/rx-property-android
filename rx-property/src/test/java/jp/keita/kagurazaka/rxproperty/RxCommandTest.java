package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import rx.functions.Action0;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
public class RxCommandTest {

    public static class VoidCommand {
        @Mock
        private Observable.OnPropertyChangedCallback mockPropertyChanged;
        private PublishSubject<Boolean> canExecuteSource;
        private TestSubscriber<Void> testSubscriber;

        @Before
        public void setUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            canExecuteSource = PublishSubject.create();
            testSubscriber = new TestSubscriber<>();
        }

        @After
        public void tearDown() throws Exception {
            mockPropertyChanged = null;
            canExecuteSource = null;
            testSubscriber = null;
        }

        private void setUpCommand(final RxCommand<Void> command) {
            command.asObservable().subscribe(testSubscriber);
            command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);
            command.setUnbindView(new Action0() {
                @Override
                public void call() {
                    command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
                }
            });
        }

        @Test
        public void constructWithoutArguments() {
            RxCommand<Void> command = new RxCommand<>();
            setUpCommand(command);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.execute(null);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void constructFromSource() {
            RxCommand<Void> command = new RxCommand<>(canExecuteSource.asObservable());
            setUpCommand(command);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Not changed
            canExecuteSource.onNext(true);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Change to unenabled
            canExecuteSource.onNext(false);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Force execute
            command.execute(null);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void constructFromSourceWithInitialValue() {
            RxCommand<Void> command = new RxCommand<>(canExecuteSource.asObservable(), false);
            setUpCommand(command);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Not changed
            canExecuteSource.onNext(false);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Change to enabled
            canExecuteSource.onNext(true);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Execute
            command.execute(null);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged, times(2)).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void multipleUnsubscriptionIsSafe() {
            RxCommand<Void> command = new RxCommand<>(canExecuteSource.asObservable(), true);
            setUpCommand(command);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void overwriteUnbindView() {
            RxCommand<Void> command = new RxCommand<>(canExecuteSource.asObservable(), true);
            setUpCommand(command);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.setUnbindView(new Action0() {
                @Override
                public void call() {
                    // Nothing to do
                }
            });

            // Change to disabled
            canExecuteSource.onNext(false);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void whenSourceEmitsError() {
            RxCommand<Void> command = new RxCommand<>(canExecuteSource.asObservable(), true);
            setUpCommand(command);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Source emits an error
            canExecuteSource.onError(new IllegalStateException("some error!"));

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertError(IllegalStateException.class);
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void whenSourceIsCompleted() {
            RxCommand<Void> command = new RxCommand<>(canExecuteSource.asObservable(), true);
            setUpCommand(command);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Source emits onCompleted
            canExecuteSource.onCompleted();

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void bindTriggerWhenCanExecuteTrue() {
            RxCommand<Void> command = new RxCommand<>();
            setUpCommand(command);

            Subject<Void, Void> trigger = PublishSubject.create();
            command.bindTrigger(trigger);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Kick by the trigger
            trigger.onNext(null);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void bindTriggerWhenCanExecuteFalse() {
            RxCommand<Void> command = new RxCommand<>(null, false);
            setUpCommand(command);

            PublishSubject<Void> trigger = PublishSubject.create();
            command.bindTrigger(trigger);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Kick by the trigger
            trigger.onNext(null);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void whenBoundTriggerEmitsError() {
            RxCommand<Void> command = new RxCommand<>(null, true);
            setUpCommand(command);

            PublishSubject<Void> trigger = PublishSubject.create();
            command.bindTrigger(trigger);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Bound trigger emits an error
            trigger.onError(new IllegalStateException("some error!"));

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertError(IllegalStateException.class);
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void whenBoundTriggerIsCompleted() {
            RxCommand<Void> command = new RxCommand<>(null, true);
            setUpCommand(command);

            PublishSubject<Void> trigger = PublishSubject.create();
            command.bindTrigger(trigger);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Bound trigger emits onCompleted
            trigger.onCompleted();

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }

        @Test
        public void bindTriggerTwice() {
            RxCommand<Void> command = new RxCommand<>(null, true);
            setUpCommand(command);

            PublishSubject<Void> trigger1 = PublishSubject.create();
            command.bindTrigger(trigger1);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Kick by the trigger
            trigger1.onNext(null);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Overwrite trigger
            PublishSubject<Void> trigger2 = PublishSubject.create();
            command.bindTrigger(trigger2);

            // Kick by the old trigger
            trigger1.onNext(null);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValue(null);
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Kick by the new trigger
            trigger2.onNext(null);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValues(null, null);
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertValues(null, null);
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());
        }
    }

    public static class TypedCommand {
        private static class CommandParameter {
            String value;

            CommandParameter(String value) {
                this.value = value;
            }
        }

        @Mock
        private Observable.OnPropertyChangedCallback mockPropertyChanged;
        private PublishSubject<Boolean> canExecuteSource;
        private PublishSubject<CommandParameter> triggerSource;
        private TestSubscriber<CommandParameter> testSubscriber;

        @Before
        public void setUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            canExecuteSource = PublishSubject.create();
            triggerSource = PublishSubject.create();
            testSubscriber = new TestSubscriber<>();
        }

        @After
        public void tearDown() throws Exception {
            mockPropertyChanged = null;
            canExecuteSource = null;
            triggerSource = null;
            testSubscriber = null;
        }

        private void setUpCommand(RxCommand<CommandParameter> command) {
            command.asObservable().subscribe(testSubscriber);
            command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);
            command.bindTrigger(triggerSource);
        }

        private void tearDownCommand(RxCommand<CommandParameter> command) {
            command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
        }

        @Test
        public void constructFromSourceWithInitialValue() {
            RxCommand<CommandParameter> command = new RxCommand<>(
                    canExecuteSource.asObservable(), false);
            setUpCommand(command);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Not changed
            canExecuteSource.onNext(false);

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertNoValues();
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Force execute
            command.execute(new CommandParameter("first"));

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValueCount(1);
            assertThat(testSubscriber.getOnNextEvents().get(0).value, is("first"));
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Kick by trigger
            triggerSource.onNext(new CommandParameter("second"));

            assertFalse(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValueCount(1);
            assertThat(testSubscriber.getOnNextEvents().get(0).value, is("first"));
            verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Change to enabled
            canExecuteSource.onNext(true);

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValueCount(1);
            assertThat(testSubscriber.getOnNextEvents().get(0).value, is("first"));
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            // Kick by trigger again
            triggerSource.onNext(new CommandParameter("second"));

            assertTrue(command.canExecute());
            assertFalse(command.isUnsubscribed());
            testSubscriber.assertValueCount(2);
            assertThat(testSubscriber.getOnNextEvents().get(0).value, is("first"));
            assertThat(testSubscriber.getOnNextEvents().get(1).value, is("second"));
            verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            command.unsubscribe();

            assertFalse(command.canExecute());
            assertTrue(command.isUnsubscribed());
            testSubscriber.assertValueCount(2);
            testSubscriber.assertCompleted();
            verify(mockPropertyChanged, times(2)).onPropertyChanged(Mockito.<Observable>any(), anyInt());

            tearDownCommand(command);
        }
    }
}
