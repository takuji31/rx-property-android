package jp.keita.kagurazaka.rxproperty;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import jp.keita.kagurazaka.rxproperty.util.Person;
import jp.keita.kagurazaka.rxproperty.util.TestObserverResource;

@RunWith(Enclosed.class)
public class ObservePropertyTest {

    public static class ObservePropertyWithoutGetter {
        private Person person;

        @Rule
        public TestObserverResource<String> testObserver = new TestObserverResource<>();

        @Before
        public void setUp() {
            person = new Person("John", "Smith");
        }

        @Test
        public void whenTargetPropertyChanges() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValue(new Person("Changed", "Smith").toString())
                    .assertNoErrors()
                    .assertNotComplete();
        }

        @Test
        public void whenNonTargetPropertyChanges() {
            // given
            Observe.propertyOf(person, Person.ID_LAST_NAME)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get().assertEmpty();
        }

        @Test
        public void whenPropertiesChangeMoreThanOnce() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setFirstName("Hans");
            person.setLastName("Schmidt"); // this change isn't notified
            person.setFirstName("Changed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValues(
                            new Person("Hans", "Smith").toString(),
                            new Person("Changed", "Schmidt").toString()
                    )
                    .assertNoErrors()
                    .assertNotComplete();
        }

        @Test
        public void whenObserverThrowsExceptionInOnNext() {
            // given
            TestObserver<String> testObserver = Observe.propertyOf(person, Person.ID_FIRST_NAME)
                    .map(PERSON_TO_STRING)
                    .subscribeWith(new TestObserver<String>() {
                        @Override
                        public void onNext(String value) {
                            super.onNext(value);
                            throw new RuntimeException("Error in the onNext");
                        }
                    });

            // when
            person.setFirstName("Changed");

            // then
            testObserver.assertFailureAndMessage(
                    RuntimeException.class,
                    "Error in the onNext",
                    new Person("Changed", "Smith").toString());
        }

        @Test
        public void disposedOnArrival() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get(true));

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get().assertEmpty();
        }

        @Test
        public void disposedOnSubscribe() {
            // given
            TestObserver<String> testObserver = Observe.propertyOf(person, Person.ID_LAST_NAME)
                    .map(PERSON_TO_STRING)
                    .subscribeWith(new TestObserver<String>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            super.onSubscribe(disposable);
                            disposable.dispose();
                        }
                    });

            // when
            person.setLastName("Changed");

            // then
            testObserver.assertEmpty();

            // after
            testObserver.dispose();
        }

        @Test
        public void disposedAfterReceiving() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");
            testObserver.get().dispose();
            person.setFirstName("Changed after disposed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValue(new Person("Changed", "Smith").toString())
                    .assertNoErrors()
                    .assertNotComplete();
        }
    }

    public static class ObservePropertyWithGetter {
        private Person person;

        @Rule
        public TestObserverResource<String> testObserver = new TestObserverResource<>();

        @Before
        public void setUp() {
            person = new Person("John", "Smith");
        }

        @Test
        public void whenTargetPropertyChanges() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME, new Function<Person, String>() {
                @Override
                public String apply(Person person) {
                    return person.getFirstName();
                }
            }).subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValue("Changed")
                    .assertNoErrors()
                    .assertNotComplete();
        }

        @Test
        public void whenNonTargetPropertyChanges() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME, new Function<Person, String>() {
                @Override
                public String apply(Person person) {
                    return person.getFirstName();
                }
            }).subscribe(testObserver.get());

            // when
            person.setLastName("Changed");

            // then
            testObserver.get().assertEmpty();
        }

        @Test
        public void whenObserverThrowsExceptionInOnNext() {
            // given
            TestObserver<String> testObserver = Observe
                    .propertyOf(person, Person.ID_FIRST_NAME, new Function<Person, String>() {
                        @Override
                        public String apply(Person person) {
                            return person.getFirstName();
                        }
                    })
                    .subscribeWith(new TestObserver<String>() {
                        @Override
                        public void onNext(String value) {
                            super.onNext(value);
                            throw new RuntimeException("Error in the onNext");
                        }
                    });

            // when
            person.setFirstName("Changed");

            // then
            testObserver.assertFailureAndMessage(
                    RuntimeException.class, "Error in the onNext", "Changed");
        }

        @Test
        public void whenPropertyGetterThrowsException() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME, new Function<Person, String>() {
                @Override
                public String apply(Person person) {
                    throw new RuntimeException("Error on the property getter");
                }
            }).subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get().assertFailureAndMessage(
                    RuntimeException.class, "Error on the property getter");
        }

        @Test
        public void whenPropertiesChangeMoreThanOnce() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME, new Function<Person, String>() {
                @Override
                public String apply(Person person) {
                    return person.getFirstName();
                }
            }).subscribe(testObserver.get());

            // when
            person.setFirstName("Hans");
            person.setLastName("Schmidt"); // This change isn't notified
            person.setFirstName("Changed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValues("Hans", "Changed")
                    .assertNoErrors()
                    .assertNotComplete();
        }

        @Test
        public void disposedOnArrival() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME, new Function<Person, String>() {
                @Override
                public String apply(Person person) {
                    return person.getFirstName();
                }
            }).subscribe(testObserver.get(true));

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get().assertEmpty();
        }

        @Test
        public void disposedOnSubscribe() {
            // given
            TestObserver<String> testObserver = Observe
                    .propertyOf(person, Person.ID_LAST_NAME, new Function<Person, String>() {
                        @Override
                        public String apply(Person person) {
                            return person.getLastName();
                        }
                    })
                    .subscribeWith(new TestObserver<String>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            super.onSubscribe(disposable);
                            disposable.dispose();
                        }
                    });

            // when
            person.setLastName("Changed");

            // then
            testObserver.assertEmpty();

            // after
            testObserver.dispose();
        }

        @Test
        public void disposedAfterReceiving() {
            // given
            Observe.propertyOf(person, Person.ID_FIRST_NAME, new Function<Person, String>() {
                @Override
                public String apply(Person person) {
                    return person.getFirstName();
                }
            }).subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");
            testObserver.get().dispose();
            person.setFirstName("Changed after disposed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValue("Changed")
                    .assertNoErrors()
                    .assertNotComplete();
        }
    }

    public static class ObserveAllProperty {
        private Person person;

        @Rule
        public TestObserverResource<String> testObserver = new TestObserverResource<>();

        @Before
        public void setUp() {
            person = new Person("John", "Smith");
        }

        @Test
        public void whenAPropertyChanges1() {
            // given
            Observe.allPropertiesOf(person)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValue(new Person("Changed", "Smith").toString())
                    .assertNoErrors()
                    .assertNotComplete();
        }

        @Test
        public void whenAPropertyChanges2() {
            // given
            Observe.allPropertiesOf(person)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setLastName("Changed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValue(new Person("John", "Changed").toString())
                    .assertNoErrors()
                    .assertNotComplete();
        }

        @Test
        public void whenPropertiesChangeMoreThanOnce() {
            // given
            Observe.allPropertiesOf(person)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setFirstName("Hans");
            person.setLastName("Schmidt");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValues(
                            new Person("Hans", "Smith").toString(),
                            new Person("Hans", "Schmidt").toString()
                    )
                    .assertNoErrors()
                    .assertNotComplete();
        }

        @Test
        public void whenObserverThrowsExceptionInOnNext() {
            // given
            TestObserver<String> testObserver = Observe.allPropertiesOf(person)
                    .map(PERSON_TO_STRING)
                    .subscribeWith(new TestObserver<String>() {
                        @Override
                        public void onNext(String value) {
                            super.onNext(value);
                            throw new RuntimeException("Error in the onNext");
                        }
                    });

            // when
            person.setFirstName("Changed");

            // then
            testObserver.assertFailureAndMessage(
                    RuntimeException.class,
                    "Error in the onNext",
                    new Person("Changed", "Smith").toString());
        }

        @Test
        public void disposedOnArrival() {
            // given
            Observe.allPropertiesOf(person)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get(true));

            // when
            person.setFirstName("Changed");

            // then
            testObserver.get().assertEmpty();
        }

        @Test
        public void disposedOnSubscribe() {
            // given
            TestObserver<String> testObserver = Observe.allPropertiesOf(person)
                    .map(PERSON_TO_STRING)
                    .subscribeWith(new TestObserver<String>() {
                        @Override
                        public void onSubscribe(Disposable disposable) {
                            super.onSubscribe(disposable);
                            disposable.dispose();
                        }
                    });

            // when
            person.setLastName("Changed");

            // then
            testObserver.assertEmpty();

            // after
            testObserver.dispose();
        }

        @Test
        public void disposedAfterReceiving() {
            // given
            Observe.allPropertiesOf(person)
                    .map(PERSON_TO_STRING)
                    .subscribe(testObserver.get());

            // when
            person.setFirstName("Changed");
            testObserver.get().dispose();
            person.setLastName("Changed");

            // then
            testObserver.get()
                    .assertSubscribed()
                    .assertValue(new Person("Changed", "Smith").toString())
                    .assertNoErrors()
                    .assertNotComplete();
        }
    }

    private static Function<Person, String> PERSON_TO_STRING = new Function<Person, String>() {
        @Override
        public String apply(Person person) throws Exception {
            return person.toString();
        }
    };
}
