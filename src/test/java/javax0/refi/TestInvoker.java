package javax0.refi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class TestInvoker {
    private boolean called = false;
    private void wuff(){
        called = true;
    }

    @Test
    void testInvoker1() {
        Assumptions.assumeFalse(called);
        Invoker.call("wuff").on(this).types().args();
        Assertions.assertTrue(called);
    }    @Test
    void testInvoker2() {
        Assumptions.assumeFalse(called);
        Invoker.call("wuff").on(this).args();
        Assertions.assertTrue(called);
    }
}
