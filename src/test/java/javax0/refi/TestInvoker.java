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
    void testInvoker() {
        Assumptions.assumeFalse(called);
        Invoker.call("wuff").on(this).types().args();
        Assertions.assertTrue(called);
    }
}
