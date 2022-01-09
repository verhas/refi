package javax0.refi;

import javax0.refi.selector.MethodSignatureFactory;
import javax0.refi.selector.Selector;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isProtected;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Simple static utility methods.
 * <p>
 * The classes {@link Classes}, {@link Methods}, and {@link Fields} support the methods {@code get()},  {@code
 * getAll()}, and {@code getDeclared()} getting the class, method, or field respectively, that they have and accessible,
 * all or are declared.
 * <p>
 *
 * Most of these methods return a {@link Stream} of {@link Optional}. When a method returns a stream, it is always serial
 * and the order of the elements are predictable and the same. The original JDK libraries returning arrays may return
 * the members in different order when executed in different JDK. These methods guarantee the order to be the same.
 */
public class Utilities {
    private static final Selector<?> INHERITED_FIELD = Selector.compile("!static & !private");
    private static final Selector<?> INHERITED_FIELD_DIFFERENT_PACKAGE = Selector.compile("!static & !private & !package");

    public static final Map<String, Class<?>> PRIMITIVES = Map.of(
        "byte", byte.class,
        "char", char.class,
        "short", short.class,
        "int", int.class,
        "long", long.class,
        "float", float.class,
        "double", double.class,
        "boolean", boolean.class);

    /**
     * Utility class supporting method query.
     */
    public static class Methods {

        /**
         * Get the declared methods of the class similar to {@link Fields#getDeclared(Class)}
         *
         * The order of the elements is predictable and the same for the same set/stream of resulting methods.
         *
         * @param klass class of which the methods are returned
         * @return the sorted stream of the methods
         */
        public static Stream<Method> getDeclared(Class<?> klass) {
            final var methods = klass.getDeclaredMethods();
            Arrays.sort(methods, Comparator.comparing(m -> new MethodSignatureFactory().signature(m)));
            return Arrays.stream(methods);
        }

        /**
         * Get a method from a given class given the name and the argument types. It does not matter if the method is
         * declared or inherited. When no argument types are given then the method will try to find the method with no
         * argument, and if there is none then it tries to find a method with the given name. If there are multiple
         * methods with the same name and no argument types then an empty optional is returned.
         *
         * @param klass the class from which the method is returned
         * @param methodName the name of the method
         * @param classes the argument types of the method
         * @return an optional method
         */
        public static Optional<Method> get(Class<?> klass, String methodName, Class<?>... classes) {
            if (classes.length == 0) {
                return getAll(klass.getSuperclass()).filter(method -> method.getName().equals(methodName) && method.getParameterTypes().length == 0 ).findFirst()
                    .or( () -> getAll(klass.getSuperclass()).filter(method -> method.getName().equals(methodName)).collect(toSingleton()));
            } else {
                return getAll(klass)
                    .filter(method -> method.getName().equals(methodName) && Arrays.deepEquals(method.getParameterTypes(), classes))
                    .findFirst();
            }
        }

        private static Collector<Method, ?, Optional<Method>> toSingleton() {
            return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() > 1) {
                        return Optional.empty();
                    }
                    return Optional.of(list.get(0));
                }
            );
        }

        /**
         * Get all the methods of the class. This includes all the methods that are declared in the class and also
         * all the inherited methods even the protected or package private methods. Note that package private methods are
         * only inherited if the parent class is in the same package as the inheriting class, and it is not possible to
         * inherit via an intermediate package through a different package.
         *
         * @param klass the class of which we need the methods
         * @return the array of the methods of the class
         */
        public static Stream<Method> getAll(final Class<?> klass) {
            final var allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
            var samePackage = true;
            for (var currentClass = klass.getSuperclass(); currentClass != null; currentClass = currentClass.getSuperclass()) {
                samePackage = samePackage && klass.getPackage() == currentClass.getPackage();
                collectMethods(samePackage, currentClass, allMethods);
            }
            final Method[] methodArray = allMethods.toArray(new Method[0]);
            Arrays.sort(methodArray, Comparator.comparing(m -> new MethodSignatureFactory().signature(m)));
            return Arrays.stream(methodArray);
        }

        private static void collectMethods(boolean samePackage, Class<?> currentClass, ArrayList<Method> allMethods) {
            Arrays.stream(currentClass.getDeclaredMethods())
                .filter(method -> isVisible(method, samePackage))
                .filter(method -> isNotOverridden(method, allMethods))
                .forEach(allMethods::add);
        }

        private static boolean isVisible(Method method, boolean samePackage) {
            final var modifier = method.getModifiers();
            return isProtected(modifier)
                || isPublic(modifier)
                || (samePackage && !isPublic(modifier) && !isProtected(modifier) && !isPrivate(modifier));
        }

        private static boolean isNotOverridden(Method currentMethod, ArrayList<Method> allMethods) {
            return allMethods.stream()
                .filter(method -> method.getName().equals(currentMethod.getName()))
                .noneMatch(method -> Arrays.deepEquals(method.getParameterTypes(), currentMethod.getParameterTypes()));
        }
    }

    public static class Fields {
        /**
         * Get the declared fields of the class. The ordering is deterministic.
         *
         * @param klass of which the fields are collected
         * @return the sorted array of fields
         */
        public static Stream<Field> getDeclared(Class<?> klass) {
            final var fields = klass.getDeclaredFields();
            Arrays.sort(fields, Comparator.comparing(Field::getName));
            return Arrays.stream(fields);
        }

        /**
         * Get all the fields, declared and inherited fields. The ordering is deterministic.
         *
         * @param klass of which the fields are collected
         * @return the sorted array of fields
         */
        public static Stream<Field> getAll(Class<?> klass) {
            Set<Field> allFields = new HashSet<>(Arrays.asList(klass.getDeclaredFields()));
            var samePackage = true;
            for (var currentClass = klass.getSuperclass(); currentClass != null; currentClass = currentClass.getSuperclass()) {
                samePackage = samePackage && klass.getPackage() == currentClass.getPackage();
                collect(samePackage, currentClass, allFields);
            }
            final var fieldsArray = allFields.toArray(new Field[0]);
            Arrays.sort(fieldsArray, Comparator.comparing(Field::getName));
            return Arrays.stream(fieldsArray);
        }

        public static Optional<Field> get(Class<?> klass, String fieldName) {
            return getAll(klass)
                .filter(field -> field.getName().equals(fieldName)).findAny();
        }

        /**
         * Collect all the fields from the actual class that are inherited by the base class assuming that the base class
         * extends directly or through other classes transitively the actual class.
         *
         * @param isSamePackage a boolean flag for deciding whether add package-private fields.
         * @param actualClass   the class in which we look for the fields
         * @param fields        the collection of the fields where to put the fields
         */
        private static void collect(boolean isSamePackage, Class<?> actualClass, Set<Field> fields) {
            final var declaredFields = actualClass.getDeclaredFields();
            final var selector = isSamePackage ? INHERITED_FIELD : INHERITED_FIELD_DIFFERENT_PACKAGE;
            Arrays.stream(declaredFields)
                .filter(selector::match)
                .forEach(fields::add);
        }
    }

    public static class Classes {
        /**
         * Get the class that is represented by the name {@code className}. This functionality extends the basic
         * functionality provided by the static method {@link Class#forName(String)} so that it also works for input
         * strings {@code int}, {@code byte} and so on for all the eight primitive types, and also it works for types that
         * end with {@code []}, so when they are essentially arrays. This also works for primitives.
         * <p>
         * If the class cannot be found in the first round then this method tries it again prepending the {@code java.lang.}
         * in front of the name given as argument, so Java language types can be referenced as, for example {@code Integer}
         * and they do not need the fully qualified name.
         * <p>
         * Note that there are many everyday used types, like {@code Map}, which are NOT in the {@code java.lang} package.
         * They have to be specified with the fully qualified name.
         *
         * @param className the name of the class or a primitive type optionally one or more {@code []} pairs at the end.
         *                  The JVM limitation is that there can be at most 255 {@code []} pairs.
         * @return the optional class
         */
        public static Optional<Class<?>> forName(String className) {
            var arrayCounter = 0;
            while (className.endsWith("[]")) {
                className = className.substring(0, className.length() - 2);
                arrayCounter++;
            }
            var klass = classForNoArray(className);
            while (arrayCounter-- > 0) {
                klass = Array.newInstance(klass, 0).getClass();
            }
            return Optional.ofNullable(klass);
        }

        /**
         * Get all the member classes sorted either declared in the class or inherited.
         *
         * @param klass that we want the inner and nested classes
         * @return the array of {@code Class} objects representing the public members of this class in a sorted order. The
         * soring order is not guaranteed. Sorting only guarantees that the returned array contains the classes in the same
         * order even if the code runs on different JVMs.
         */
        public static Stream<Class<?>> getAll(Class<?> klass) {
            final var classes = Arrays.stream(klass.getClasses()).collect(Collectors.toSet());
            final var declaredClasses = Arrays.stream(klass.getDeclaredClasses()).collect(Collectors.toSet());
            final var allClasses = new HashSet<Class<?>>();
            allClasses.addAll(classes);
            allClasses.addAll(declaredClasses);
            final Class<?>[] classArray = allClasses.toArray(new Class[0]);
            Arrays.sort(classArray, Comparator.comparing(Class::getName));
            return Arrays.stream(classArray);
        }

        /**
         * Get the declared classes of the class sorted.
         *
         * <p> See the notes at the javadoc of the method {@link
         * Fields#getDeclared(Class)}
         *
         * @param klass class of which the member classes are returned
         * @return the sorted array of the classes
         */
        public static Stream<Class<?>> getDeclared(Class<?> klass) {
            final var classes = klass.getDeclaredClasses();
            Arrays.sort(classes, Comparator.comparing(Class::getName));
            return Arrays.stream(classes);
        }

        /**
         * The same as {@link Classes#getDeclared(Class)}} except it returns the classes and not the declared classes.
         * It means that only the classes that are available from outside but including the inherited classes are returned.
         *
         * @param klass the class of which we need the classes
         * @return the array of the classes of the class
         */
        public static Stream<Class<?>> get(Class<?> klass) {
            final var classes = klass.getClasses();
            Arrays.sort(classes, Comparator.comparing(Class::getName));
            return Arrays.stream(classes);
        }

        private static Class<?> classForNoArray(String name) {
            if (PRIMITIVES.containsKey(name)) {
                return PRIMITIVES.get(name);
            }
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
                try {
                    return Class.forName("java.lang." + name);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }
    }

}
