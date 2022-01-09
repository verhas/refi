package javax0.refi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

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
    private Method method;

    public FromClass on(Object target) {
        this.target = target;
        return new FromClass();
    }

    public class FromClass {
        public WithArgument types(Class<?>... types) {
            method = Utilities.Methods.get(target.getClass(), methodName, types)
                .orElseThrow(() -> new IllegalArgumentException("No method " + methodName + " with types " + types + " found on " + target.getClass()));
            return new WithArgument();
        }
        public Object args(Object... args) {
            method = Utilities.Methods.get(target.getClass(), methodName)
                .orElseThrow(() -> new IllegalArgumentException("No method " + methodName + " without arguments found on " + target.getClass()));
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

    public class WithArgument {
        public Object args(Object... args) {
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
