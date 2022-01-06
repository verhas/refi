package javax0.refi.selector;

import java.util.function.Function;

/**
 * This is a class that contains no code and never runs. The test uses this class to look up fields, methods and other
 * members in this class and then the {@link java.lang.reflect.Field Field}, {@link java.lang.reflect.Method Method}
 * and other objects are tested against the {@link Selector#match(Object)} matcher in the {@link TestSelector} test
 * class.
 */
@SuppressWarnings("ALL")
public class SutTargetClass {

    static int var_static;
    final int var_final = 1;
    private final int i = 0;
    public int var_public;
    protected int var_protected;
    int var_package;
    transient int var_transient;
    volatile int var_volatile;
    @Deprecated
    private int j = 0;
    private int var_private;

    @interface Z {
    }


    public boolean equals(Object other) {
        return true;
    }

    @SuppressWarnings("SameReturnValue")
    private int z() {
        return 1;
    }

    static void method_static() {
    }

    synchronized void method_synchronized() {
    }

    strictfp void method_strict() {
        j++;// to avoid analysis result saying this variable could be final
    }

    void method_vararg(Object... x) {
    }

    void method_notVararg(Object[] x) {
    }

    void method_void() {
    }

    public void method_public() {
    }
    final void method_final() {
    }

    int method_int() {
        return 0;
    }

    private void method_private() {
    }

    protected void method_protected() {
    }


    private void method_throws() throws IllegalArgumentException {
    }

    private void method_notThrows() {
    }


    interface A {
        void q();
    }

    interface B extends A {
    }

    interface C extends A, B {
    }

    static class Y implements C {
        public void q() {
        }
    }

    static abstract class X extends SutTargetClass implements Function<Object, Object> {
        abstract int method_abstract();

        public Object apply(Object t) {
            return null;
        }

        public int hashCode() {
            return 0;
        }
    }
}
