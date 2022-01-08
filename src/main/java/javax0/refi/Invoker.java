package javax0.refi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflectively invoke the method specified by the name using the arguments on the target object. The way
 * to invoke the method is:
 *
 * <pre>{@code
 *    call(methodName).on(targetObject).types(argument classes listed).args(arguments listed);
 * }</pre>
 */
public class Invoker {
    private String methodName;
    private Object target;
    Class<?>[] types;

    public FromClass on(Object target) {
        this.target = target;
        return new FromClass();
    }

    public class FromClass {
        public WithArgument types(Class<?>... types) {
            Invoker.this.types = types;
            return new WithArgument();
        }
    }

    public class WithArgument {
        public Object args(Object... args) {
            Method method;
            try {
                method = target.getClass().getDeclaredMethod(methodName, types);
            } catch (NoSuchMethodException e) {
                try {
                    method = target.getClass().getMethod(methodName, types);
                } catch (NoSuchMethodException e1) {
                    throw new RuntimeException(e1);
                }
            }
            try {
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            } catch (InvocationTargetException ite) {
                throw sneakyThrow(ite.getCause());
            }
        }
    }

    private static <E extends Throwable> RuntimeException sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }


    public static Invoker call(String methodName) {
        final var invoker = new Invoker();
        invoker.methodName = methodName;
        return invoker;
    }

}
