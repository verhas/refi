package javax0.selector.tools;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.stream.Collectors;

class Utilities {

    /**
     * Get the modifiers as string.
     *
     * @param method for which the modifiers are needed
     * @return the string containing the modifiers space separated
     */
    static String modifiersString(Method method) {
        return new ModifiersBuilder(method.getModifiers()).toString();
    }

    /**
     * Get the modifiers as a string except access modifier.
     *
     * @param method for which the modifiers are needed
     * @return the string containing the modifiers space separated, except the access modifier
     */
    static String modifiersStringNoAccess(Method method) {
        return new ModifiersBuilder(method.getModifiers()
            & ~Modifier.PROTECTED & ~Modifier.PRIVATE & ~Modifier.PUBLIC).toString();
    }

    /**
     * Get the type of a field or a method as string.
     *
     * @param member of which the type is needed
     * @return string containing the type as string with all the generics.
     */
    static String typeAsString(Member member) {
        return getGenericTypeName(member instanceof Field ?
            ((Field) member).getGenericType()
            :
            ((Method) member).getGenericReturnType());
    }


    private static String removeJavaLang(String s) {
        if (s.startsWith("java.lang.") && !s.substring("java.lang.".length()).contains(".")) {
            return s.substring("java.lang.".length());
        } else {
            return s;
        }
    }

    /**
     * Get the generic type name of the type passed as argument. The JDK {@code Type#getTypeName()} returns a string
     * that contains the classes with their names and not with the canonical names (inner classes have {@code $} in the
     * names instead of dot). This method goes through the type structure and converts the names (generic types also)
     * to
     *
     * @param t the type
     * @return the type as string
     */
    static String getGenericTypeName(Type t) {
        final String normalizedName;
        if (t instanceof ParameterizedType) {
            normalizedName = getGenericParametrizedTypeName((ParameterizedType) t);
        } else if (t instanceof Class<?>) {
            normalizedName = removeJavaLang(((Class<?>) t).getCanonicalName());
        } else if (t instanceof WildcardType) {
            normalizedName = getGenericWildcardTypeName((WildcardType) t);
        } else if (t instanceof GenericArrayType) {
            var at = (GenericArrayType) t;
            normalizedName = getGenericTypeName(at.getGenericComponentType()) + "[]";
        } else if (t instanceof TypeVariable) {
            normalizedName = t.getTypeName();
        } else {
            throw new RuntimeException(
                "Type is something not handled. It is '" + t.getClass() + "' for the type '" + t.getTypeName() + "'");
        }
        return normalizedName;
    }

    private static String getGenericParametersString(Class<?> t) {
        final var generics = Arrays.stream(t.getTypeParameters())
            .map(Utilities::getGenericTypeName)
            .collect(Collectors.joining(","));
        if (generics.length() == 0) {
            return "";
        } else {
            return "<" + generics + ">";
        }
    }

    private static String getGenericWildcardTypeName(WildcardType t) {
        String normalizedName;
        var ub = joinTypes(t.getUpperBounds());
        var lb = joinTypes(t.getLowerBounds());
        normalizedName = "?" +
            (lb.length() > 0 && !lb.equals("Object") ? " super " + lb : "") +
            (ub.length() > 0 && !ub.equals("Object") ? " extends " + ub : "");
        return normalizedName;
    }

    private static String getGenericParametrizedTypeName(ParameterizedType t) {
        String normalizedName;
        var types = t.getActualTypeArguments();
        if (!(t.getRawType() instanceof Class<?>)) {
            throw new RuntimeException("'getRawType()' returned something that is not a class : " + t.getClass().getTypeName());
        }
        final var klass = (Class<?>) t.getRawType();
        final String klassName = removeJavaLang(klass.getCanonicalName());
        if (types.length > 0) {
            normalizedName = klassName + "<" +
                joinTypes(types) +
                ">";
        } else {
            normalizedName = klassName;
        }
        return normalizedName;
    }

    /**
     * Join the type names also removing the {@code java.lang. } prefixes if any.
     *
     * @param types the types to join. These are the generic types, or the super or extends types in a wildcard.
     * @return the string of the list comma separated.
     */
    private static String joinTypes(Type[] types) {
        return Arrays.stream(types)
            .map(Utilities::getGenericTypeName)
            .collect(Collectors.joining(","));
    }

}
