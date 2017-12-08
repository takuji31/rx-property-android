package jp.keita.kagurazaka.rxproperty;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import jp.keita.kagurazaka.rxproperty.util.RxPropertyErrorObserver;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(Enclosed.class)
@SuppressWarnings("deprecation")
public class RxPropertyTest {

    public static class InvalidArguments {
        @Rule
        public ExpectedException thrown = ExpectedException.none();

        @Test
        public void raisesNPEWhenSourceObservableInNull() {
            thrown.expect(NullPointerException.class);
            thrown.expectMessage("source must not be null.");

            new RxProperty<>((Observable<String>) null);
        }

        @Test
        public void raisesNPEWhenInitialValueIsNull() {
            thrown.expect(NullPointerException.class);
            thrown.expectMessage("initialValue must not be null.");

            new RxProperty<>((String) null);
        }

        @Test
        public void raisesNPEWhenModeIsNull() {
            thrown.expect(NullPointerException.class);
            thrown.expectMessage("mode must not be null.");

            new RxProperty<>((EnumSet<RxProperty.Mode>) null);
        }
    }

    public static class AsRxObservable {
        private RxProperty<String> property;

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void emitsInitialValueOnSubscribeWhenCreateWithInitialValueAndDefaultMode() {
            // given
            property = new RxProperty<>("RxProperty");

            // then
            property.test()
                    .assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsInitialValueOnSubscribeWhenCreateWithInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>("RxProperty",
                    EnumSet.of(RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));

            // then
            property.test()
                    .assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsInitialValueOnSubscribeWhenCreateWithFromSourceObservableWithInitialValueAndDefaultMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source, "RxProperty");

            // then
            property.test()
                    .assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsInitialValueOnSubscribeWhenCreateWithFromSourceObservableWithInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source, "RxProperty",
                    EnumSet.of(RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));

            // then
            property.test()
                    .assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsNoValuesOnSubscribeWhenCreateWithoutInitialValueDespiteOfRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>();

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
            property = new RxProperty<>("RxProperty", EnumSet.of(RxProperty.Mode.NONE));

            // then
            property.test()
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsValuesWhenSets() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = property.test();

            // when
            property.set("First");
            property.set("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsValuesWhenSetWithoutViewUpdate() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = property.test();

            // when
            property.setWithoutViewUpdate("First");
            property.setWithoutViewUpdate("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsValuesWhenSetFromView() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = property.test();

            // when
            property.getValue().set("First");
            property.getValue().set("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void followsSourceObservable() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
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
        public void doesNotEmitSameValuesWhenSetsValuesWithDistinctUntilChangeMode() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = property.test();

            // when
            property.set("RxProperty");
            property.set("RxProperty");
            property.set("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void doesNotEmitSameValuesWhenSourceObservableEmitsSameValuesWithDistinctUntilChangeMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = property.test();

            // when
            source.onNext("RxProperty");
            source.onNext("RxProperty");
            source.onNext("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsAllValuesWhenSetsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            property = new RxProperty<>(EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = property.test();

            // when
            property.set("RxProperty");
            property.set("RxProperty");
            property.set("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsAllValuesWhenSourceObservableEmitsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source, EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = property.test();

            // when
            source.onNext("RxProperty");
            source.onNext("RxProperty");
            source.onNext("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void emitsLatestValueWhenForceNotify() {
            // given
            property = new RxProperty<>("RxProperty", EnumSet.of(RxProperty.Mode.NONE));
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
            property = new RxProperty<>("RxProperty");
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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = property.test();

            // when
            source.onComplete();

            // then
            testObserver.assertResult().dispose();
        }

        @Test
        public void emitsOnCompleteWhenDisposed() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = property.test();

            // when
            property.set("First");
            property.dispose();
            property.set("Second");

            // then
            testObserver.assertResult("First")
                    .dispose();
        }
    }

    public static class AsDataBindingObservable {
        private RxProperty<String> property;

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithoutInitialValueWithRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>();

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
            property = new RxProperty<>("RxProperty");

            // then
            propertyObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithoutInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>(EnumSet.of(RxProperty.Mode.NONE));

            // then
            propertyObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithInitialValueWithoutRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>("RxProperty", EnumSet.of(RxProperty.Mode.NONE));

            // then
            propertyObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyValueWhenSets() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.set("First");
            property.set("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyValueWhenSetWithoutViewUpdate() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.setWithoutViewUpdate("First");
            property.setWithoutViewUpdate("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyValueWhenSetFromView() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.getValue().set("First");
            property.getValue().set("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void followsSourceObservable() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
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
        public void distinctUntilChangeWhenSetsValuesWithDistinctUntilChangeMode() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.set("RxProperty");
            property.set("RxProperty");
            property.set("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void distinctUntilChangeWhenSourceObservableEmitsSameValuesWithDistinctUntilChangeMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("RxProperty");
            source.onNext("RxProperty");
            source.onNext("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyAllValuesWhenSetsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            property = new RxProperty<>(EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.set("RxProperty");
            property.set("RxProperty");
            property.set("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyAllValuesWhenSourceObservableEmitsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source, EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            source.onNext("RxProperty");
            source.onNext("RxProperty");
            source.onNext("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyLatestValueWhenForceNotify() {
            // given
            property = new RxProperty<>("RxProperty", EnumSet.of(RxProperty.Mode.NONE));
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
            property = new RxProperty<>("RxProperty");
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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
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
        public void noLongerNotifyWhenSetsAfterDisposed() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = propertyObserver(property);

            // when
            property.set("First");
            property.dispose();
            property.set("Second");

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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
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

    public static class AsDataBindingObservableForView {
        private RxProperty<String> property;

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithoutInitialValueWithRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>();

            // then
            valueFieldObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>("RxProperty");

            // then
            valueFieldObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithoutInitialValueAndRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>(EnumSet.of(RxProperty.Mode.NONE));

            // then
            valueFieldObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueOnSubscribeWhenCreateWithInitialValueWithoutRaiseLatestValueOnSubscribeMode() {
            // given
            property = new RxProperty<>("RxProperty", EnumSet.of(RxProperty.Mode.NONE));

            // then
            valueFieldObserver(property)
                    .assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyValueWhenSets() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            property.set("First");
            property.set("Second");

            // then
            testObserver.assertSubscribed()
                    .assertValues("First", "Second")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueWhenSetWithoutViewUpdate() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            property.setWithoutViewUpdate("First");
            property.setWithoutViewUpdate("Second");

            // then
            testObserver.assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyNoValueWhenSetFromView() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            property.getValue().set("First");
            property.getValue().set("Second");

            // then
            testObserver.assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void followsSourceObservable() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = valueFieldObserver(property);

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
        public void distinctUntilChangeWhenSetsValuesWithDistinctUntilChangeMode() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            property.set("RxProperty");
            property.set("RxProperty");
            property.set("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void distinctUntilChangeWhenSourceObservableEmitsSameValuesWithDistinctUntilChangeMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            source.onNext("RxProperty");
            source.onNext("RxProperty");
            source.onNext("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValue("RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyAllValuesWhenSetsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            property = new RxProperty<>(EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            property.set("RxProperty");
            property.set("RxProperty");
            property.set("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyAllValuesWhenSourceObservableEmitsSameValuesWithoutDistinctUntilChangeMode() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source, EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            source.onNext("RxProperty");
            source.onNext("RxProperty");
            source.onNext("RxProperty");

            // then
            testObserver.assertSubscribed()
                    .assertValues("RxProperty", "RxProperty", "RxProperty")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void notifyLatestValueWhenForceNotify() {
            // given
            property = new RxProperty<>("RxProperty", EnumSet.of(RxProperty.Mode.NONE));
            TestObserver<String> testObserver = valueFieldObserver(property);

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
            property = new RxProperty<>("RxProperty");
            TestObserver<String> testObserver = valueFieldObserver(property);

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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = valueFieldObserver(property);

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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = valueFieldObserver(property);

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
        public void noLongerNotifyWhenSetsAfterDisposed() {
            // given
            property = new RxProperty<>();
            TestObserver<String> testObserver = valueFieldObserver(property);

            // when
            property.set("First");
            property.dispose();
            property.set("Second");

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
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            TestObserver<String> testObserver = valueFieldObserver(property);

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
        private RxProperty<String> property;

        @After
        public void tearDown() {
            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void returnsNullWhenCreateWithoutArguments() {
            // given
            property = new RxProperty<>();

            // then
            assertThat(property.getOrNull(), is((String) null));
        }

        @Test
        public void returnsSpecifiedValueWhenCreateWithInitialValue() {
            // given
            property = new RxProperty<>("RxProperty");

            // then
            assertThat(property.getOrNull(), is("RxProperty"));
        }

        @Test
        public void returnsSetValue() {
            // given
            property = new RxProperty<>("RxProperty");

            // when
            property.set("New value");

            // then
            assertThat(property.getOrNull(), is("New value"));
        }

        @Test
        public void returnsNullWhenCreateWithSourceObservableThatNeverEmitsValues() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);

            // then
            assertThat(property.getOrNull(), is((String) null));
        }

        @Test
        public void followsSourceObservable() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);

            // when
            source.onNext("Value by source");

            // then
            assertThat(property.getOrNull(), is("Value by source"));
        }
    }

    public static class IsDisposed {
        private RxProperty<String> property;

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
            property = new RxProperty<>();

            // then
            assertThat(property.isDisposed(), is(false));
        }

        @Test
        public void returnsTrueAfterDisposed() {
            // given
            property = new RxProperty<>();

            // when
            property.dispose();

            // then
            assertThat(property.isDisposed(), is(true));
        }

        @Test
        public void returnsTrueAfterSourceObservableEmitsError() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);

            // when
            source.onError(new RuntimeException("Error in source observable"));

            // then
            assertThat(property.isDisposed(), is(true));
        }

        @Test
        public void returnsTrueAfterSourceObservableIsCompleted() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);

            // when
            source.onComplete();

            // then
            assertThat(property.isDisposed(), is(true));
        }

        @Test
        public void disposeCanBeCalledMoreThanOnce() {
            // given
            property = new RxProperty<>();

            // when
            property.dispose();
            property.dispose();
            property.dispose();

            // then
            assertThat(property.isDisposed(), is(true));
        }

        @Test
        public void onErrorChangedEmitsOnCompleteWhenDisposed() {
            // given
            property = new RxProperty<>();
            TestObserver<List<String>> testObserver = property.onErrorsChanged().test();

            // when
            property.dispose();

            // then
            testObserver.assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertComplete()
                    .dispose();
        }
    }

    public static class SetCancellable {
        private RxProperty<String> property;

        @Before
        public void setUp() {
            property = new RxProperty<>();
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

    public static class ValidationRxObserver {
        private RxProperty<String> property;
        private RxPropertyErrorObserver<String> testObserver;

        @Before
        public void setUp() {
            property = new RxProperty<>("RxProperty");
            testObserver = new RxPropertyErrorObserver<>(property);
        }

        @After
        public void tearDown() {
            if (testObserver != null) {
                testObserver.dispose();
                testObserver = null;
            }

            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void errorObservablesEmitOnCompleteWhenRxPropertyIsDisposed() {
            // given
            property.setValidator(new AllSuccessValidator());

            // when
            property.dispose();

            // then
            testObserver.assertNoErrors()
                    .assertNoSummarizedErrors()
                    .assertNoHasErrors()
                    .assertComplete()
                    .dispose();
        }

        @Test
        public void errorObservablesEmitOnCompleteWhenSourceObservableIsCompleted() {
            // given
            Subject<String> source = PublishSubject.create();
            property = new RxProperty<>(source);
            property.setValidator(new AllSuccessValidator());
            testObserver = new RxPropertyErrorObserver<>(property);

            // when
            source.onComplete();

            // then
            testObserver.assertNoErrors()
                    .assertNoSummarizedErrors()
                    .assertNoHasErrors()
                    .assertComplete()
                    .dispose();
        }

        @Test
        public void validationSucceedsWhenValidatorReturnsNull() {
            // given
            property.setValidator(new RxProperty.Validator<String>() {
                @Nullable
                @Override
                public List<String> validate(@NonNull String value) {
                    return null;
                }

                @Nullable
                @Override
                public String summarizeErrorMessages(@NonNull List<String> errorMessages) {
                    return null;
                }
            });

            // when
            property.set("123");
            property.set("1234");
            property.set("12345");

            // then
            testObserver.assertNoErrors()
                    .assertNoSummarizedErrors()
                    .assertNoHasErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void failedValidation() {
            // given
            property.setValidator(new RxProperty.Validator<String>() {
                @Nullable
                @Override
                public List<String> validate(@NonNull String value) {
                    ArrayList<String> errors = new ArrayList<>();
                    if (value.isEmpty()) {
                        errors.add("Value must not be empty.");
                    }
                    if (value.length() > 8) {
                        errors.add("Value length must be less than 9.");
                    }
                    return errors;
                }

                @Nullable
                @Override
                public String summarizeErrorMessages(@NonNull List<String> errorMessages) {
                    if (errorMessages.size() > 0) {
                        return errorMessages.get(0);
                    }
                    return null;
                }
            });

            // when
            property.set("12345678");
            property.set("123456789");
            property.set("");
            property.set("12345678");

            // then
            testObserver
                    .assertErrors(
                            Collections.singletonList("Value length must be less than 9."),
                            Collections.<String>emptyList(),
                            Collections.singletonList("Value length must be less than 9."),
                            Collections.singletonList("Value must not be empty."),
                            Collections.<String>emptyList())
                    .assertSummarizedErrors(
                            "Value length must be less than 9.",
                            "",
                            "Value length must be less than 9.",
                            "Value must not be empty.",
                            "")
                    .assertHasErrors(true, false, true, false)
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void functionValidator() {
            // given
            property.setValidator(EMPTY_FUNCTION_VALIDATOR);

            // when
            property.set("");
            property.set("1");
            property.set("");

            // then
            testObserver.assertErrors(
                    Collections.singletonList("Value must not be empty."),
                    Collections.<String>emptyList(),
                    Collections.singletonList("Value must not be empty."))
                    .assertSummarizedErrors(
                            "Value must not be empty.",
                            "",
                            "Value must not be empty.")
                    .assertHasErrors(true, false, true)
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void unsetValidator() {
            // given
            property.setValidator(EMPTY_FUNCTION_VALIDATOR);

            // when
            property.set("");
            property.setValidator((RxProperty.Validator<String>) null);
            property.set("");

            // then
            testObserver.assertErrors(
                    Collections.singletonList("Value must not be empty."),
                    Collections.<String>emptyList())
                    .assertSummarizedErrors(
                            "Value must not be empty.", "")
                    .assertHasErrors(true, false)
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void unsetFunctionValidator() {
            // given
            property.setValidator(EMPTY_FUNCTION_VALIDATOR);

            // when
            property.set("");
            property.setValidator((RxProperty.SimpleValidator<String>) null);
            property.set("");

            // then
            testObserver.assertErrors(
                    Collections.singletonList("Value must not be empty."),
                    Collections.<String>emptyList())
                    .assertSummarizedErrors(
                            "Value must not be empty.",
                            "")
                    .assertHasErrors(true, false)
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void failValidationWhenValidatorThrowsExceptionInValidate() {
            // given
            property.setValidator(new RxProperty.Validator<String>() {
                @Nullable
                @Override
                public List<String> validate(@NonNull String value) {
                    throw new IllegalStateException("Exception occurred.");
                }

                @Nullable
                @Override
                public String summarizeErrorMessages(@NonNull List<String> errorMessages) {
                    return null;
                }
            });

            // then
            testObserver.assertErrors(Collections.singletonList("Exception occurred."))
                    .assertSummarizedErrors("Exception occurred.")
                    .assertHasErrors(true)
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void failValidationWhenValidatorThrowsExceptionInSummarize() {
            // given
            property.setValidator(new RxProperty.Validator<String>() {
                @Nullable
                @Override
                public List<String> validate(@NonNull String value) {
                    return Collections.singletonList("Validation failed.");
                }

                @Nullable
                @Override
                public String summarizeErrorMessages(@NonNull List<String> errorMessages) {
                    throw new IllegalStateException("Exception occurred.");
                }
            });

            // then
            testObserver.assertErrors(Collections.singletonList("Exception occurred."))
                    .assertSummarizedErrors("Exception occurred.")
                    .assertHasErrors(true)
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void failValidationWhenFunctionValidatorThrowsException() {
            // given
            property.setValidator(new RxProperty.SimpleValidator<String>() {
                @Nullable
                @Override
                public String validate(@NonNull String value) {
                    throw new IllegalStateException("Exception occurred.");
                }
            });

            // then
            testObserver.assertErrors(Collections.singletonList("Exception occurred."))
                    .assertSummarizedErrors("Exception occurred.")
                    .assertHasErrors(true)
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void latestErrorStatusGettersWorkWell() {
            // given
            property.setValidator(EMPTY_FUNCTION_VALIDATOR);
            testObserver.assertLatestErrors()
                    .assertLatestSummarizedError("")
                    .assertLatestHasErrors(false);

            // when
            property.set("");

            // then
            testObserver.assertLatestErrors("Value must not be empty.")
                    .assertLatestSummarizedError("Value must not be empty.")
                    .assertLatestHasErrors(true);

            // when
            property.setValidator((RxProperty.Validator<String>) null);

            // then
            testObserver.assertLatestErrors()
                    .assertLatestSummarizedError("")
                    .assertLatestHasErrors(false)
                    .dispose();
        }
    }

    public static class ValidationDataBindingObserver {
        private RxProperty<String> property;
        private TestObserver<String> errorObserver;
        private TestObserver<Boolean> hasErrorObserver;

        @Before
        public void setUp() {
            property = new RxProperty<>("RxProperty");
            errorObserver = errorObserver(property);
            hasErrorObserver = hasErrorObserver(property);
        }

        @After
        public void tearDown() {
            if (errorObserver != null) {
                errorObserver.dispose();
                errorObserver = null;
            }

            if (hasErrorObserver != null) {
                hasErrorObserver.dispose();
                hasErrorObserver = null;
            }

            if (property != null) {
                property.dispose();
                property = null;
            }
        }

        @Test
        public void errorObservablesEmitOnCompleteWhenRxPropertyIsDisposed() {
            // given
            property.setValidator(new AllSuccessValidator());

            // when
            property.dispose();

            // then
            errorObserver.assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            hasErrorObserver.assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void validationSucceedsWhenValidatorReturnsNull() {
            // given
            property.setValidator(new RxProperty.Validator<String>() {
                @Nullable
                @Override
                public List<String> validate(@NonNull String value) {
                    return null;
                }

                @Nullable
                @Override
                public String summarizeErrorMessages(@NonNull List<String> errorMessages) {
                    return null;
                }
            });

            // when
            property.set("123");
            property.set("1234");
            property.set("12345");

            // then
            errorObserver.assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            hasErrorObserver.assertSubscribed()
                    .assertNoValues()
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }

        @Test
        public void failedValidation() {
            // given
            property.setValidator(new RxProperty.Validator<String>() {
                @Nullable
                @Override
                public List<String> validate(@NonNull String value) {
                    ArrayList<String> errors = new ArrayList<>();
                    if (value.isEmpty()) {
                        errors.add("Value must not be empty.");
                    }
                    if (value.length() > 8) {
                        errors.add("Value length must be less than 9.");
                    }
                    return errors;
                }

                @Nullable
                @Override
                public String summarizeErrorMessages(@NonNull List<String> errorMessages) {
                    if (errorMessages.size() > 0) {
                        return errorMessages.get(0);
                    }
                    return null;
                }
            });

            // when
            property.set("12345678");
            property.set("123456789");
            property.set("");
            property.set("12345678");

            // then
            errorObserver.assertSubscribed()
                    .assertValues(
                            "Value length must be less than 9.",
                            "",
                            "Value length must be less than 9.",
                            "Value must not be empty.",
                            "")
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();

            hasErrorObserver.assertSubscribed()
                    .assertValues(true, false, true, false)
                    .assertNoErrors()
                    .assertNotComplete()
                    .dispose();
        }
    }

    public static class Misc {
        @Rule
        public ExpectedException thrown = ExpectedException.none();

        private RxProperty<String> property;

        @Before
        public void setUp() {
            property = new RxProperty<>();
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
            thrown.expectMessage("RxProperty#error is read only.");

            // when
            property.getError().set("Value");
        }
    }

    private static class AllSuccessValidator implements RxProperty.Validator<String> {
        @Nullable
        @Override
        public List<String> validate(@NonNull String value) {
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String summarizeErrorMessages(@NonNull List<String> errorMessages) {
            return "";
        }
    }

    private static RxProperty.SimpleValidator<String> EMPTY_FUNCTION_VALIDATOR
            = new RxProperty.SimpleValidator<String>() {
        @Nullable
        @Override
        public String validate(@NonNull String value) {
            if (value.isEmpty()) {
                return "Value must not be empty.";
            }
            return null;
        }
    };

    private static <T> TestObserver<T> propertyObserver(final RxProperty<T> property) {
        return Observe.propertyOf(property, 0,
                new Function<RxProperty<T>, T>() {
                    @Override
                    public T apply(RxProperty<T> property) throws Exception {
                        return property.getOrNull();
                    }
                })
                .test();
    }

    private static <T> TestObserver<T> valueFieldObserver(final RxProperty<T> property) {
        return Observe.propertyOf(property.getValue(), 0,
                new Function<ObservableField<T>, T>() {
                    @Override
                    public T apply(ObservableField<T> field) throws Exception {
                        return field.get();
                    }
                })
                .test();
    }

    private static <T> TestObserver<String> errorObserver(final RxProperty<T> property) {
        return Observe.propertyOf(property.getError(), 0,
                new Function<ObservableField<String>, String>() {
                    @Override
                    public String apply(ObservableField<String> field) throws Exception {
                        return field.get();
                    }
                })
                .test();
    }

    private static <T> TestObserver<Boolean> hasErrorObserver(final RxProperty<T> property) {
        return Observe.propertyOf(property.getHasError(), 0,
                new Function<ObservableBoolean, Boolean>() {
                    @Override
                    public Boolean apply(ObservableBoolean value) throws Exception {
                        return value.get();
                    }
                })
                .test();
    }
}
