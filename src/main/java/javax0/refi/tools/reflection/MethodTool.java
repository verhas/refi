package javax0.refi.tools.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MethodTool {
    final protected AtomicInteger argCounter = new AtomicInteger(0);
    protected Method method;

    public static MethodTool with(Method method) {
        var it = new MethodTool();
        it.method = method;
        return it;
    }

    public static String methodSignature(Method method) {
        return with(method).signature();
    }

    public String signature() {
        final var types = method.getGenericParameterTypes();
        final var sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (i == types.length - 1 && method.isVarArgs()) {
                sb.append(getVarArg(types[i]));
            } else {
                sb.append(getArg(types[i]));
            }
        }
        var argList = sb.toString();
        var exceptionList = Arrays.stream(method.getGenericExceptionTypes())
            .map(Utilities::getGenericTypeName)
            .collect(Collectors.joining(","));
        final String modifiers;
        modifiers = Utilities.modifiersString(method);
        return modifiers + Utilities.typeAsString(method) +
            " " +
            method.getName() +
            "(" + argList + ")" +
            (exceptionList.length() == 0 ? "" : " throws " + exceptionList);
    }

    public String getVarArg(Type t) {
        final var normType = Utilities.getGenericTypeName(t);
        final String actualType = normType.substring(0, normType.length() - 2) + "... ";
        return actualType + " arg" + argCounter.addAndGet(1);
    }

    public String getArg(Type t) {
        final var normType = Utilities.getGenericTypeName(t);
        return normType + " arg" + argCounter.addAndGet(1);
    }
}
