package jp.keita.kagurazaka.rxproperty;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.EnumSet;

import io.reactivex.Observable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
@SuppressWarnings("deprecation")
public class ReadOnlyRxPropertyTest {

    public static class InvalidArguments {
        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void raisesNPEWhenSourceObservableInNull() {
            thrown.expect(NullPointerException.class);
            thrown.expectMessage("source must not be null.");

            new ReadOnlyRxProperty<>((Observable<String>) null);
        }

        @Test
        public void raisesNPEWhenInitialValueIsNull() {
            thrown.expect(NullPointerException.class);
            thrown.expectMessage("initialValue must not be null.");

            new ReadOnlyRxProperty<>(PublishSubject.<String>create(), (String) null);
        }

        @Test
        public void raisesNPEWhenModeIsNull() {
            thrown.expect(NullPointerException.class);
            thrown.expectMessage("mode must not be null.");

            new ReadOnlyRxProperty<>(
                    PublishSubject.<String>create(), (EnumSet<RxProperty.Mode>) null);
        }
    }

    public static class AsRxObservable {
        private Subject<String> source;
        private ReadOnlyRxProperty<String> property;

        @Before
        public void setUp() {
            source = PublishSubject.create();
        }

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void emitsInitialValueOnSubscribedWhenCreateWithInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source, "ReadOnlyRxProperty");

            // then
            property.test()
                    .assertSubscribed()
                    .assertValue("ReadOnlyRxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsNoValuesOnSubscribeWhenCreateWithoutInitialValueDespiteOfRaiseLatestValueOnSubscribeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // then
            property.test()
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsNoValuesOnSubscribeWhenCreateWithInitialValueWithoutRaiseLatestValueOnSubscribeMode() {
            // given
            property = new ReadOnlyRxProperty<>(
                    source, "ReadOnlyRxProperty", EnumSet.of(RxProperty.Mode.NONE));

            // then
            property.test()
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void followsSourceObservable() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = property.test();

            // when
            source.onNext("First");
            source.onNext("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void distinctUntilChangeWhenSourceObservableEmitsSameValuesWithDistinctUntilChangeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = property.test();

            // when
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("ReadOnlyRxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsAllValuesWhenSourceObservableEmitsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source, EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = property.test();

            // when
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("ReadOnlyRxProperty", "ReadOnlyRxProperty", "ReadOnlyRxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsLatestValueWhenForceNotify() {
            // given
            property = new ReadOnlyRxProperty<>(
                    source, "RxProperty", EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = property.test();

            // when
            property.forceNotify();

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsLatestValueWhenForceNotifyDespiteOfDistinctUntilChangeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source, "RxProperty");
            TestObserver<String> testObserver = property.test();

            // when
            property.forceNotify();

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsErrorWhenSourceObservableEmitsError() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = property.test();

            // when
            source.onError(new RuntimeException("Error in source observable"));

            // then
            testObserver.assertFailureAndMessage(
                    RuntimeException.class, "Error in source observable")
                    .dispose();
        }

        @Test
        public void emitsOnCompleteWhenSourceObservableIsCompleted() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = property.test();

            // when
            source.onComplete();

            // then
            testObserver.assertResult().dispose();
        }

        @Test
        public void emitsOnCompleteWhenDisposed() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = property.test();

            // when
            source.onNext("First");
            property.dispose();
            source.onNext("Second");

            // then
            testObserver.assertResult("First")
                    .dispose();
        }
    }

    public static class AsDataBindingObservable {
        private Subject<String> source;
        private ReadOnlyRxProperty<String> property;

        @Before
        public void setUp() {
            source = PublishSubject.create();
        }

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void notifyNoValuesOnSubscribeWhenCreateWithoutInitialValueWithRaiseLatestValueOnSubscribeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // then
            propertyObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source, "ReadOnlyRxProperty");

            // then
            propertyObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValuesOnSubscribeWhenCreateWithoutInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source, EnumSet.of(RxProperty.Mode.NONE));

            // then
            propertyObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValuesOnSubscribeWhenCreateWithInitialValueWithoutRaiseLatestValueOnSubscribeMode() {
            // given
            property = new ReadOnlyRxProperty<>(
                    source, "ReadOnlyRxProperty", EnumSet.of(RxProperty.Mode.NONE));

            // then
            propertyObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void followsSourceObservable() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("First");
            source.onNext("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void distinctUntilChangeWhenSourceObservableEmitsSameValuesWithDistinctUntilChangeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("ReadOnlyRxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyAllValuesWhenSourceObservableEmitsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source, EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");
            source.onNext("ReadOnlyRxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("ReadOnlyRxProperty", "ReadOnlyRxProperty", "ReadOnlyRxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyLatestValueWhenForceNotify() {
            // given
            property = new ReadOnlyRxProperty<>(
                    source, "RxProperty", EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.forceNotify();

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyLatestValueWhenForceNotifyDespiteOfDistinctUntilChangeMode() {
            // given
            property = new ReadOnlyRxProperty<>(source, "RxProperty");
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.forceNotify();
            property.forceNotify();

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void noLongerNotifyWhenSourceObservableEmitsError() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("First");
            source.onError(new RuntimeException("Error in source observable"));
            source.onNext("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValue("First")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void noLongerNotifyWhenSourceObservableIsCompleted() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("First");
            source.onComplete();
            source.onNext("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValue("First")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void noLongerNotifyWhenSourceObservableEmitsValueAfterDisposed() {
            // given
            property = new ReadOnlyRxProperty<>(source);
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("First");
            property.dispose();
            source.onNext("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValue("First")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }
    }

    public static class GetOrNull {
        private Subject<String> source;
        private ReadOnlyRxProperty<String> property;

        @Before
        public void setUp() {
            source = PublishSubject.create();
        }

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void returnsNullWhenCreateWithSourceObservableThatNeverEmitsValues() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // then
            assertThat(property.getOrNull(), is((String) null));
        }

        @Test
        public void returnsSpecifiedValueWhenCreateWithInitialValue() {
            // given
            property = new ReadOnlyRxProperty<>(source, "ReadOnlyRxProperty");

            // then
            assertThat(property.getOrNull(), is("ReadOnlyRxProperty"));
        }

        @Test
        public void followsSourceObservable() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // when
            source.onNext("Value by source");

            // then
            assertThat(property.getOrNull(), is("Value by source"));
        }
    }

    public static class IsDisposed {
        private Subject<String> source;
        private ReadOnlyRxProperty<String> property;

        @Before
        public void setUp() {
            source = PublishSubject.create();
        }

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void returnsFalseAfterCreation() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // then
            assertThat(property.isDisposed(), is(false));
        }

        @Test
        public void returnsTrueAfterDisposed() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // when
            property.dispose();

            // then
            assertThat(property.isDisposed(), is(true));
        }

        @Test
        public void returnsTrueAfterSourceObservableEmitsError() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // when
            source.onError(new RuntimeException("Error in source observable"));

            // then
            assertThat(property.isDisposed(), is(true));
        }

        @Test
        public void returnsTrueAfterSourceObservableIsCompleted() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // when
            source.onComplete();

            // then
            assertThat(property.isDisposed(), is(true));
        }

        @Test
        public void disposeCanBeCalledMoreThanOnce() {
            // given
            property = new ReadOnlyRxProperty<>(source);

            // when
            property.dispose();
            property.dispose();
            property.dispose();

            // then
            assertThat(property.isDisposed(), is(true));
        }
    }

    public static class GetValue {
        @Rule
        public ExpectedException thrown = ExpectedException.none();

        private Subject<String> source;
        private ReadOnlyRxProperty<String> property;

        @Before
        public void setUp() {
            source = PublishSubject.create();
            property = new ReadOnlyRxProperty<>(source);
        }

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void raisesErrorWhenSet() {
            thrown.expect(UnsupportedOperationException.class);
            thrown.expectMessage("ReadOnlyRxProperty doesn't support two-way binding.");

            property.getValue().set("Value");
        }
    }

    public static class SetCancellable {
        private Subject<String> source;
        private ReadOnlyRxProperty<String> property;

        @Before
        public void setUp() {
            source = PublishSubject.create();
            property = new ReadOnlyRxProperty<>(source);
        }

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void nullAccepted() {
            // when
            property.setCancellable(null);
            property.dispose();

            // then
            // raises no errors
        }

        @Test
        public void unbindViewWillBeExecutedWhenDisposed() throws Exception {
            // given
            Cancellable mockCancellable = Mockito.mock(Cancellable.class);
            property.setCancellable(mockCancellable);
            verify(mockCancellable, never()).cancel();

            // when
            property.dispose();

            // then
            verify(mockCancellable).cancel();
        }
    }

    private static <T> TestObserver<T> propertyObserver(ReadOnlyRxProperty<T> property) {
        return Observe.propertyOf(property, 0,
                new Function<ReadOnlyRxProperty<T>, T>() {
                    @Override
                    public T apply(ReadOnlyRxProperty<T> property) throws Exception {
                        return property.getOrNull();
                    }
                })
                .test();
    }
}
