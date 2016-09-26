package jp.keita.kagurazaka.rxproperty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import jp.keita.kagurazaka.rxproperty.util.Person;
import rx.Subscription;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Enclosed.class)
public class ObservePropertyTest {

    public static class ObservePropertyWithoutGetter {
        private Person person;
        private TestSubscriber<Person> testSubscriber;
        private Subscription subscription;

        @Before
        public void setUp() {
            person = new Person("John", "Smith");
            testSubscriber = new TestSubscriber<>();
        }

        @After
        public void tearDown() {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }

        @Test
        public void whenTargetPropertyChanges() {
            // given
            subscription = Observe.propertyOf(person, Person.ID_FIRST_NAME)
                    .subscribe(testSubscriber);

            // when
            person.setFirstName("Changed");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            assertThat(testSubscriber.getOnNextEvents().get(0).getFirstName(), is("Changed"));
        }

        @Test
        public void whenNonTargetPropertyChanges() {
            // given
            subscription = Observe.propertyOf(person, Person.ID_LAST_NAME)
                    .subscribe(testSubscriber);

            // when
            person.setFirstName("Changed");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();
        }
    }

    public static class ObservePropertyWithGetter {
        private Person person;
        private TestSubscriber<String> testSubscriber;
        private Subscription subscription;

        @Before
        public void setUp() {
            person = new Person("John", "Smith");
            testSubscriber = new TestSubscriber<>();
        }

        @After
        public void tearDown() {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }

        @Test
        public void whenTargetPropertyChanges() {
            // given
            subscription = Observe.propertyOf(person, Person.ID_FIRST_NAME, new Func1<Person, String>() {
                @Override
                public String call(Person person) {
                    return person.getFirstName();
                }
            }).subscribe(testSubscriber);

            // when
            person.setFirstName("Changed");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            testSubscriber.assertValue("Changed");
        }

        @Test
        public void whenNonTargetPropertyChanges() {
            // given
            subscription = Observe.propertyOf(person, Person.ID_LAST_NAME, new Func1<Person, String>() {
                @Override
                public String call(Person person) {
                    return person.getFirstName();
                }
            }).subscribe(testSubscriber);

            // when
            person.setFirstName("Changed");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();
        }
    }

    public static class ObserveAllProperty {
        private Person person;
        private TestSubscriber<Person> testSubscriber;
        private Subscription subscription;

        @Before
        public void setUp() {
            person = new Person("John", "Smith");
            testSubscriber = new TestSubscriber<>();
        }

        @After
        public void tearDown() {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }

        @Test
        public void whenAPropertyChanges1() {
            // given
            subscription = Observe.allPropertiesOf(person).subscribe(testSubscriber);

            // when
            person.setFirstName("Changed");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            assertThat(testSubscriber.getOnNextEvents().get(0).getFirstName(), is("Changed"));
        }

        @Test
        public void whenAPropertyChanges2() {
            // given
            subscription = Observe.allPropertiesOf(person).subscribe(testSubscriber);

            // when
            person.setLastName("Changed");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            assertThat(testSubscriber.getOnNextEvents().get(0).getLastName(), is("Changed"));
        }

        @Test
        public void whenPropertiesChangeMoreThanOnce() {
            // given
            subscription = Observe.allPropertiesOf(person).subscribe(testSubscriber);

            // when
            person.setFirstName("Hans");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);
            assertThat(testSubscriber.getOnNextEvents().get(0).getFirstName(), is("Hans"));

            // when
            person.setLastName("Schmidt");

            // then
            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);
            assertThat(testSubscriber.getOnNextEvents().get(1).getLastName(), is("Schmidt"));
        }
    }
}
