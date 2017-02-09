package jp.keita.kagurazaka.rxproperty.util;

import org.junit.rules.ExternalResource;

import io.reactivex.observers.TestObserver;

public class TestObserverResource<T> extends ExternalResource {
    private TestObserver<T> testObserver;

    public TestObserver<T> get() {
        return get(false);
    }

    public TestObserver<T> get(boolean dispose) {
        if (dispose && testObserver != null) {
            testObserver.dispose();
        }
        return testObserver;
    }

    @Override
    protected void before() throws Throwable {
        testObserver = new TestObserver<>();
    }

    @Override
    protected void after() {
        if (testObserver != null) {
            testObserver.dispose();
        }
        testObserver = null;
    }
}
