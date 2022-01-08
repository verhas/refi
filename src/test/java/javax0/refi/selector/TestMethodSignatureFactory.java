package javax0.refi.selector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMethodSignatureFactory {

    private String sig(String name, Class<?>... parameters) throws NoSuchMethodException {
        return new MethodSignatureFactory().signature(SutTargetClass.class.getDeclaredMethod(name, parameters));
    }

    @Test
    @DisplayName("Test method signature factory")
    void test() throws NoSuchMethodException {
        assertEquals("public boolean equals(Object arg1)", sig("equals", Object.class));
        assertEquals("java.util.HashMap<java.util.Map<String,Integer>,Boolean> " +
                "wuzz(java.util.Set<java.util.Set<java.util.Set<java.math.BigInteger>>> arg1, " +
                "String arg2, " +
                "javax0.refi.selector.SutTargetClass... arg3)",
            sig("wuzz", Set.class, String.class, SutTargetClass[].class));
    }
}
