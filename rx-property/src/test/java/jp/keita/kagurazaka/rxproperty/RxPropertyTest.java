package jp.keita.kagurazaka.rxproperty;

import android.support.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import jp.keita.kagurazaka.rxproperty.util.RxPropertyTestRule;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class RxPropertyTest {

    public static class ConstructorVariationsWithoutSource {
        @Rule
        public RxPropertyTestRule<String> rule = new RxPropertyTestRule<>();

        private void scenarioTestWhenInitialValueIsNullAndModeNoneIsOn(final RxProperty<String> property) {
            rule.setUp(property);

            // default initial value is null
            rule.assertPropertyValueIs(null);
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is off
            rule.getValueSubscriber().assertNoValues();

            property.set(null);

            // DISTINCT_UNTIL_CHANGED is off
            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValue(null);

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues(null, "Changed");
        }

        @Test
        public void noArguments() {
            RxProperty<String> property = new RxProperty<>();
            rule.setUp(property);

            // default initial value is null
            rule.assertPropertyValueIs(null);
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertValue(null);

            property.set(null);

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValue(null);

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues(null, "Changed");
        }

        @Test
        public void initialValue() {
            RxProperty<String> property = new RxProperty<>("RxProperty");
            rule.setUp(property);

            rule.assertPropertyValueIs("RxProperty");
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertValue("RxProperty");

            property.set("RxProperty");

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs("RxProperty");
            rule.getValueSubscriber().assertValue("RxProperty");

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues("RxProperty", "Changed");
        }

        @Test
        public void modeEmpty() {
            RxProperty<String> property
                    = new RxProperty<>(EnumSet.noneOf(RxProperty.Mode.class));
            scenarioTestWhenInitialValueIsNullAndModeNoneIsOn(property);
        }

        @Test
        public void modeNone() {
            RxProperty<String> property = new RxProperty<>(EnumSet.of(RxProperty.Mode.NONE));
            scenarioTestWhenInitialValueIsNullAndModeNoneIsOn(property);
        }

        @Test
        public void modeNoneAndDistinctUntilChanged() {
            RxProperty<String> property = new RxProperty<>(
                    EnumSet.of(RxProperty.Mode.NONE, RxProperty.Mode.DISTINCT_UNTIL_CHANGED));
            scenarioTestWhenInitialValueIsNullAndModeNoneIsOn(property);
        }

        @Test
        public void modeNoneAndRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>(
                    EnumSet.of(RxProperty.Mode.NONE,
                            RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
            scenarioTestWhenInitialValueIsNullAndModeNoneIsOn(property);
        }

        @Test
        public void modeAll() {
            RxProperty<String> property = new RxProperty<>(EnumSet.allOf(RxProperty.Mode.class));
            scenarioTestWhenInitialValueIsNullAndModeNoneIsOn(property);
        }

        @Test
        public void modeDistinctUntilChanged() {
            RxProperty<String> property = new RxProperty<>(
                    EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED));
            rule.setUp(property);

            // default initial value is null
            rule.assertPropertyValueIs(null);
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is off
            rule.getValueSubscriber().assertNoValues();

            property.set(null);

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertNoValues();

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValue("Changed");
        }

        @Test
        public void modeRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>(
                    EnumSet.of(RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
            rule.setUp(property);

            // default initial value is null
            rule.assertPropertyValueIs(null);
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertValue(null);

            property.set(null);

            // DISTINCT_UNTIL_CHANGED is off
            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValues(null, null);

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues(null, null, "Changed");
        }

        @Test
        public void modeDistinctUntilChangedAndRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>(
                    EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED,
                            RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
            rule.setUp(property);

            // default initial value is null
            rule.assertPropertyValueIs(null);
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertValue(null);

            property.set(null);

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValue(null);

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues(null, "Changed");
        }

        @Test
        public void initialValueAndMode() {
            RxProperty<String> property = new RxProperty<>(
                    "RxProperty", EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED));
            rule.setUp(property);

            rule.assertPropertyValueIs("RxProperty");
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is off
            rule.getValueSubscriber().assertNoValues();

            property.set("RxProperty");

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs("RxProperty");
            rule.getValueSubscriber().assertNoValues();

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValue("Changed");
        }
    }

    public static class ConstructorVariationsWithSource {
        @Rule
        public RxPropertyTestRule<String> rule = new RxPropertyTestRule<>();

        private PublishSubject<String> source;

        @Before
        public void setUp() {
            source = PublishSubject.create();
        }

        @After
        public void TearDown() {
            source = null;
        }

        @Test
        public void noMoreArguments() {
            RxProperty<String> property = new RxProperty<>(source.asObservable());
            rule.setUp(property);

            // default initial value is null
            rule.assertPropertyValueIs(null);
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertValue(null);

            source.onNext(null);

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValue(null);

            source.onNext("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues(null, "Changed");
        }

        @Test
        public void initialValue() {
            RxProperty<String> property = new RxProperty<>(source.asObservable(), "RxProperty");
            rule.setUp(property);

            rule.assertPropertyValueIs("RxProperty");
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertValue("RxProperty");

            source.onNext("RxProperty");

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs("RxProperty");
            rule.getValueSubscriber().assertValue("RxProperty");

            source.onNext("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues("RxProperty", "Changed");
        }

        @Test
        public void modeNone() {
            RxProperty<String> property = new RxProperty<>(
                    source.asObservable(), EnumSet.of(RxProperty.Mode.NONE));
            rule.setUp(property);

            // default initial value is null
            rule.assertPropertyValueIs(null);
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertNoValues();

            source.onNext(null);

            // DISTINCT_UNTIL_CHANGED is off
            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValue(null);

            source.onNext("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues(null, "Changed");
        }

        @Test
        public void initialValueAndModeRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>(
                    source.asObservable(),
                    "RxProperty",
                    EnumSet.of(RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
            rule.setUp(property);

            rule.assertPropertyValueIs("RxProperty");
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getValueSubscriber().assertValue("RxProperty");

            source.onNext("RxProperty");

            // DISTINCT_UNTIL_CHANGED is off
            rule.assertPropertyValueIs("RxProperty");
            rule.getValueSubscriber().assertValues("RxProperty", "RxProperty");

            source.onNext("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues("RxProperty", "RxProperty", "Changed");
        }
    }

    public static class Validations {
        @Rule
        public RxPropertyTestRule<String> rule = new RxPropertyTestRule<>();

        @Test
        public void setNoValidatorsWithRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>();
            rule.setUp(property);

            rule.assertPropertyHasNoErrors();
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getErrorMessagesSubscriber().assertValue(null);
            rule.getSummarizedErrorMessageSubscriber().assertValue(null);
            rule.getHasErrorsSubscriber().assertValue(false);

            // Validators are not set, property.hasErrors() is always false
            property.set("Changed");
            rule.assertPropertyHasNoErrors();
            property.set(null);
            rule.assertPropertyHasNoErrors();
            property.set("");
            rule.assertPropertyHasNoErrors();

            rule.getErrorMessagesSubscriber().assertValue(null);
            rule.getSummarizedErrorMessageSubscriber().assertValue(null);
            rule.getHasErrorsSubscriber().assertValue(false);
        }

        @Test
        public void setNoValidatorsWithoutRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>(
                    EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED));
            rule.setUp(property);

            rule.assertPropertyHasNoErrors();
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is off
            rule.getErrorMessagesSubscriber().assertNoValues();
            rule.getSummarizedErrorMessageSubscriber().assertNoValues();
            rule.getHasErrorsSubscriber().assertNoValues();

            // Validators are not set, property.hasErrors() is always false
            property.set("Changed");
            rule.assertPropertyHasNoErrors();
            property.set(null);
            rule.assertPropertyHasNoErrors();
            property.set("");
            rule.assertPropertyHasNoErrors();

            rule.getErrorMessagesSubscriber().assertNoValues();
            rule.getSummarizedErrorMessageSubscriber().assertNoValues();
            rule.getHasErrorsSubscriber().assertNoValues();
        }

        @Test
        public void setValidatorWithRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>("RxProperty")
                    .setValidator(new Func1<String, String>() {
                        @Override
                        public String call(String value) {
                            return (value == null) ? "Null value isn't allowed" : null;
                        }
                    });
            rule.setUp(property);

            rule.assertPropertyHasNoErrors();
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getErrorMessagesSubscriber().assertValue(null);
            rule.getSummarizedErrorMessageSubscriber().assertValue(null);
            rule.getHasErrorsSubscriber().assertValue(false);

            property.set("Changed");
            rule.assertPropertyHasNoErrors();
            property.set(null);
            rule.assertSummarizedErrorMessageIs("Null value isn't allowed");
            property.set("");
            rule.assertPropertyHasNoErrors();

            rule.getErrorMessagesSubscriber().assertValues(
                    null, null, Collections.singletonList("Null value isn't allowed"), null);
            rule.getSummarizedErrorMessageSubscriber()
                    .assertValues(null, null, "Null value isn't allowed", null);
            rule.getHasErrorsSubscriber().assertValues(false, false, true, false);
        }

        @Test
        public void setValidatorWithoutRaiseLatestValueOnSubscribe() {
            RxProperty<String> property = new RxProperty<>(
                    "RxProperty",
                    EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED))
                    .setValidator(new Func1<String, String>() {
                        @Override
                        public String call(String value) {
                            return (value == null) ? "Null value isn't allowed" : null;
                        }
                    });
            rule.setUp(property);

            rule.assertPropertyHasNoErrors();
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is off
            rule.getErrorMessagesSubscriber().assertNoValues();
            rule.getSummarizedErrorMessageSubscriber().assertNoValues();
            rule.getHasErrorsSubscriber().assertNoValues();

            property.set("Changed");
            rule.assertPropertyHasNoErrors();
            property.set(null);
            rule.assertSummarizedErrorMessageIs("Null value isn't allowed");
            property.set("");
            rule.assertPropertyHasNoErrors();

            rule.getErrorMessagesSubscriber().assertValues(
                    null, Collections.singletonList("Null value isn't allowed"), null);
            rule.getSummarizedErrorMessageSubscriber()
                    .assertValues(null, "Null value isn't allowed", null);
            rule.getHasErrorsSubscriber().assertValues(false, true, false);
        }

        @Test
        public void setComplexValidator() {
            RxProperty<String> property = new RxProperty<>("RxProperty")
                    .setValidator(new RxProperty.Validator<String>() {
                        @Override
                        public List<String> validate(@Nullable String value) {
                            final List<String> results = new ArrayList<>();
                            if (value == null) {
                                results.add("Null value isn't allowed");
                            } else {
                                if (value.length() > 5) {
                                    results.add("Max length is 5 letters.");
                                }
                                if (!value.toLowerCase().equals(value)) {
                                    results.add("Uppercase isn't allowed");
                                }
                            }
                            return results;
                        }

                        @Override
                        public String summarizeErrorMessages(@Nullable List<String> errorMessages) {
                            if (errorMessages == null || errorMessages.isEmpty()) {
                                return null;
                            }
                            return errorMessages.get(0);
                        }
                    });
            rule.setUp(property);

            rule.assertSummarizedErrorMessageIs("Max length is 5 letters.");
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getErrorMessagesSubscriber().assertValue(
                    Arrays.asList("Max length is 5 letters.", "Uppercase isn't allowed")
            );
            rule.getSummarizedErrorMessageSubscriber().assertValue("Max length is 5 letters.");
            rule.getHasErrorsSubscriber().assertValue(true);

            property.set("Changed");
            rule.assertSummarizedErrorMessageIs("Max length is 5 letters.");
            property.set(null);
            rule.assertSummarizedErrorMessageIs("Null value isn't allowed");
            property.set("");
            rule.assertPropertyHasNoErrors();

            rule.getErrorMessagesSubscriber().assertValues(
                    Arrays.asList("Max length is 5 letters.", "Uppercase isn't allowed"),
                    Arrays.asList("Max length is 5 letters.", "Uppercase isn't allowed"),
                    Collections.singletonList("Null value isn't allowed"),
                    null);
            rule.getSummarizedErrorMessageSubscriber().assertValues(
                    "Max length is 5 letters.",
                    "Max length is 5 letters.",
                    "Null value isn't allowed",
                    null);
            rule.getHasErrorsSubscriber().assertValues(true, true, true, false);
        }

        @Test
        public void clearValidatorWhenPropertyHasErrors() {
            RxProperty<String> property = new RxProperty<>("RxProperty")
                    .setValidator(new Func1<String, String>() {
                        @Override
                        public String call(String value) {
                            return (value == null) ? "Null value isn't allowed" : null;
                        }
                    });
            rule.setUp(property);

            rule.assertPropertyHasNoErrors();
            // RAISE_LATEST_VALUE_ON_SUBSCRIBE is on
            rule.getErrorMessagesSubscriber().assertValue(null);
            rule.getSummarizedErrorMessageSubscriber().assertValue(null);
            rule.getHasErrorsSubscriber().assertValue(false);

            property.set(null);

            rule.assertErrorMessagesIs(Collections.singletonList("Null value isn't allowed"));
            rule.assertSummarizedErrorMessageIs("Null value isn't allowed");

            rule.getErrorMessagesSubscriber().assertValues(
                    null, Collections.singletonList("Null value isn't allowed"));
            rule.getSummarizedErrorMessageSubscriber()
                    .assertValues(null, "Null value isn't allowed");
            rule.getHasErrorsSubscriber().assertValues(false, true);

            // Clear the validator
            property.setValidator((Func1<String, String>) null);

            rule.assertPropertyHasNoErrors();
            rule.getErrorMessagesSubscriber().assertValues(
                    null, Collections.singletonList("Null value isn't allowed"), null);
            rule.getSummarizedErrorMessageSubscriber()
                    .assertValues(null, "Null value isn't allowed", null);
            rule.getHasErrorsSubscriber().assertValues(false, true, false);
        }
    }

    public static class Termination {
        @Rule
        public RxPropertyTestRule<String> rule = new RxPropertyTestRule<>();

        private PublishSubject<String> source;

        @Before
        public void setUp() {
            source = PublishSubject.create();
        }

        @After
        public void tearDown() {
            source = null;
        }

        @Test
        public void whenSourceEmitError() {
            RxProperty<String> property = new RxProperty<>(source.asObservable());
            rule.setUp(property);

            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValue(null);
            rule.getValueSubscriber().assertNoErrors();

            Throwable error = new IllegalStateException();
            source.onError(error);

            rule.getValueSubscriber().assertError(error);
        }

        @Test
        public void propagateWhenSourceEmitOnComplete() {
            RxProperty<String> property = new RxProperty<>(source.asObservable());
            rule.setUp(property);

            rule.assertPropertyValueIs(null);
            rule.getValueSubscriber().assertValue(null);
            rule.getValueSubscriber().assertNotCompleted();

            source.onCompleted();

            rule.getValueSubscriber().assertCompleted();
        }
    }

    public static class ViewNotification {
        @Rule
        public RxPropertyTestRule<String> rule = new RxPropertyTestRule<>();

        @Test
        public void overwriteUnbindView() {
            RxProperty<String> property = new RxProperty<>("RxProperty");
            rule.setUp(property);

            rule.assertPropertyValueIs("RxProperty");
            rule.getValueSubscriber().assertValue("RxProperty");
            rule.assertViewNotificationCountIs(0);

            property.setUnbindView(new Action0() {
                @Override
                public void call() {
                    // Nothing to do
                }
            });

            property.set("Changed");

            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues("RxProperty", "Changed");
            rule.assertViewNotificationCountIs(0);
        }

        @Test
        public void scenario() {
            RxProperty<String> property = new RxProperty<>("RxProperty");
            rule.setUp(property);

            rule.assertPropertyValueIs("RxProperty");
            rule.getValueSubscriber().assertValue("RxProperty");
            rule.assertViewNotificationCountIs(0);

            property.setWithoutViewUpdate("Changed");

            // Without view update
            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues("RxProperty", "Changed");
            rule.assertViewNotificationCountIs(0);

            property.set("Changed");

            // DISTINCT_UNTIL_CHANGED is on
            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues("RxProperty", "Changed");
            rule.assertViewNotificationCountIs(0);

            property.forceNotify();

            // forceNotify ignores DISTINCT_UNTIL_CHANGED
            rule.assertPropertyValueIs("Changed");
            rule.getValueSubscriber().assertValues("RxProperty", "Changed", "Changed");
            rule.assertViewNotificationCountIs(1);

            property.set("RxProperty");

            rule.assertPropertyValueIs("RxProperty");
            rule.getValueSubscriber()
                    .assertValues("RxProperty", "Changed", "Changed", "RxProperty");
            rule.assertViewNotificationCountIs(2);
        }
    }

    public static class Others {
        @Test
        public void multipleUnsubscriptionIsSafe() {
            RxProperty<String> property = new RxProperty<>("RxProperty");
            assertThat(property.get(), is("RxProperty"));

            property.set("Changed");
            assertThat(property.get(), is("Changed"));

            property.unsubscribe();
            assertThat(property.get(), is((String) null));

            property.unsubscribe();
            assertThat(property.get(), is((String) null));
        }
    }
}
