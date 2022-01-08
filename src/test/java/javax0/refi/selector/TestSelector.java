package javax0.refi.selector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSelector {
    private static final Member IGNORED_MEMBER = null;
    private static final Class<?>[] NO_ARGS = null;
    private static final Method SUT_METHOD_EQUALS;
    private static final Method SUT_X_METHOD_EQUALS;
    private static final Method SUT_METHOD_HASHCODE;
    private static final Method SUT_X_METHOD_HASHCODE;
    private static final Method SUT_METHOD_VOID;
    private static final Method SUT_METHOD_Z;
    private static final Method SUT_METHOD_STATIC;
    private static final Method SUT_METHOD_SYNCHRONIZED;
    private static final Method SUT_METHOD_ABSTRACT;
    private static final Method SUT_METHOD_STRICT;
    private static final Method SUT_METHOD_VARARG;
    private static final Method SUT_METHOD_NOT_VARARG;
    private static final Method SUT_METHOD_PRIVATE;
    private static final Method SUT_METHOD_PROTECTED;
    private static final Method SUT_METHOD_PUBLIC;
    private static final Method SUT_METHOD_FINAL;
    private static final Method SYSTEM_REGISTER_NATIVES;
    private static final Field SUT_FIELD_I;
    private static final Field SUT_FIELD_J;
    private static final Field SUT_FIELD_PRIVATE;
    private static final Field SUT_FIELD_PROTECTED;
    private static final Field SUT_FIELD_PACKAGE;
    private static final Field SUT_FIELD_PUBLIC;
    private static final Field SUT_FIELD_FINAL;
    private static final Field SUT_FIELD_TRANSIENT;
    private static final Field SUT_FIELD_VOLATILE;
    private static final Field SUT_FIELD_STATIC;
    private static final Method SUT_X_METHOD_APPLY;
    private static final Method SUT_METHOD_Q;
    private static final Method SUT_METHOD_NOT_THROWS;
    private static final Method SUT_METHOD_INT;
    private static final Method SUT_METHOD_THROWS;


    static {
        try {
            SUT_METHOD_EQUALS = SutTargetClass.class.getMethod("equals", Object.class);
            SUT_X_METHOD_EQUALS = SutTargetClass.X.class.getMethod("equals", Object.class);
            SUT_METHOD_HASHCODE = SutTargetClass.class.getMethod("hashCode", NO_ARGS);
            SUT_X_METHOD_HASHCODE = SutTargetClass.X.class.getDeclaredMethod("hashCode", NO_ARGS);
            SUT_METHOD_VOID = SutTargetClass.class.getDeclaredMethod("method_void");
            SUT_METHOD_Z = SutTargetClass.class.getDeclaredMethod("z");
            SUT_METHOD_STATIC = SutTargetClass.class.getDeclaredMethod("method_static");
            SUT_METHOD_SYNCHRONIZED = SutTargetClass.class.getDeclaredMethod("method_synchronized");
            SUT_METHOD_ABSTRACT = SutTargetClass.X.class.getDeclaredMethod("method_abstract");
            SUT_METHOD_STRICT = SutTargetClass.class.getDeclaredMethod("method_strict");
            SUT_METHOD_VARARG = SutTargetClass.class.getDeclaredMethod("method_vararg", Object[].class);
            SUT_METHOD_NOT_VARARG = SutTargetClass.class.getDeclaredMethod("method_notVararg", Object[].class);
            SUT_METHOD_PRIVATE = SutTargetClass.class.getDeclaredMethod("method_private");
            SUT_METHOD_PROTECTED = SutTargetClass.class.getDeclaredMethod("method_protected");
            SUT_METHOD_PUBLIC = SutTargetClass.class.getDeclaredMethod("method_public");
            SUT_METHOD_FINAL = SutTargetClass.class.getDeclaredMethod("method_final");
            SYSTEM_REGISTER_NATIVES = System.class.getDeclaredMethod("registerNatives");
            SUT_FIELD_I = SutTargetClass.class.getDeclaredField("i");
            SUT_FIELD_J = SutTargetClass.class.getDeclaredField("j");
            SUT_FIELD_PRIVATE = SutTargetClass.class.getDeclaredField("var_private");
            SUT_FIELD_PROTECTED = SutTargetClass.class.getDeclaredField("var_protected");
            SUT_FIELD_PACKAGE = SutTargetClass.class.getDeclaredField("var_package");
            SUT_FIELD_PUBLIC = SutTargetClass.class.getDeclaredField("var_public");
            SUT_FIELD_FINAL = SutTargetClass.class.getDeclaredField("var_final");
            SUT_FIELD_TRANSIENT = SutTargetClass.class.getDeclaredField("var_transient");
            SUT_FIELD_VOLATILE = SutTargetClass.class.getDeclaredField("var_volatile");
            SUT_FIELD_STATIC = SutTargetClass.class.getDeclaredField("var_static");
            SUT_X_METHOD_APPLY = SutTargetClass.X.class.getDeclaredMethod("apply", Object.class);
            SUT_METHOD_Q = SutTargetClass.Y.class.getDeclaredMethod("q", NO_ARGS);
            SUT_METHOD_NOT_THROWS = SutTargetClass.class.getDeclaredMethod("method_notThrows");
            SUT_METHOD_INT = SutTargetClass.class.getDeclaredMethod("method_int");
            SUT_METHOD_THROWS = SutTargetClass.class.getDeclaredMethod("method_throws");

        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException("This is an error in the test and not in the SUT.", e);
        }
    }

    @Test
    @DisplayName("List all private primitive fields")
    void testStream() {
        Set<Field> fields =
            Arrays.stream(SutTargetClass.class.getDeclaredFields())
                .filter(Selector.compile("private & primitive")::match)
                .collect(Collectors.toSet());
        assertEquals(3, fields.size());
    }

    @Test
    @DisplayName("When the argument to match is null the return value has to be false except for 'null' and 'ture'")
    void testNullIsFalse() throws Exception {
        for (final var key : getFunctionMapKeys("selectors")) {
            assertEquals("null".equals(key) || "true".equals(key),
                Selector.compile(key).match(null));
        }
    }

    @Test
    @DisplayName("Documentation samples for 'null' condition")
    void testNullDemo() throws NoSuchMethodException {
        // snipline ObjectClassHasNoSuperClass
        assertTrue(Selector.compile("superClass -> null").match(Object.class));
        // snipline nullIsNotAnInterface
        assertFalse(Selector.compile("superClass -> interface").match(Object.class));
    }

    @Test
    @DisplayName("Test the conditions interface and class")
    void testInterfaceAndClass() {
        assertFalse(Selector.compile("interface").match(SutTargetClass.class));
        assertTrue(Selector.compile("interface").match(Serializable.class));
        assertTrue(Selector.compile("class").match(SutTargetClass.class));
        assertFalse(Selector.compile("class").match(Serializable.class));
        assertFalse(Selector.compile("class").match(SUT_METHOD_EQUALS));
    }

    @Test
    @DisplayName("When the argument to match is null then any conversion will return also null")
    void testNullIsNull() throws Exception {
        for (final var key : getFunctionMapKeys("converters")) {
            assertTrue(Selector.compile(key + " -> null").match(null));
        }
    }

    @Test
    @DisplayName("When the argument to match is null then any regular expression matching is false")
    void testNullIsFalseInRegex() throws Exception {
        for (final var key : getFunctionMapKeys("regexMemberSelectors")) {
            assertFalse(Selector.compile(key + " ~/.*/").match(null));
        }
    }

    /**
     * Get the "selectors", "converters" and "regexMemberSelectors" keys from the {@link Selector} function map.
     *
     * @param fieldName either "selectors", "converters" or "regexMemberSelectors"
     * @return the keys
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private Set<String> getFunctionMapKeys(final String fieldName) throws NoSuchFieldException, IllegalAccessException {
        final var selectorsField = Selector.class.getDeclaredField(fieldName);
        selectorsField.setAccessible(true);
        final var selectorObject = Selector.compile("true");
        return ((Map<String,?>) selectorsField.get(selectorObject)).keySet();
    }


    @Test
    @DisplayName("Test that a normal method is not a birdge method")
    void testNoBridge() throws Exception {
        assertTrue(Selector.compile("!bridge").match(SUT_METHOD_VOID));
    }

    @Test
    @DisplayName("Test that Object.declaringClass returns null and that returns false when checked")
    void testNoParent() {
        assertFalse(Selector.compile("declaringClass -> !null & simpleName ~ /^Test/").match(Object.class));
        assertFalse(Selector.compile("declaringClass -> simpleName ~ /^Test/").match(Object.class));
    }

    @Test
    @DisplayName("Test that nestHost converter works")
    void testNestingHost() {
        // snippet NestHostSample
        assertTrue(Selector.compile("nestHost -> (!null & simpleName ~ /^SutTarget/)")
            .match(SutTargetClass.X.class));
        // end snippet
        assertTrue(Selector.compile("nestHost -> (!null & simpleName ~ /^Object/)").match(Object.class));
        assertTrue(Selector.compile("nestHost -> (!null & simpleName ~ /^Map/)").match(Map.Entry.class));
    }

    @Test
    void testDeclaringClassDemo() throws Exception {
        // snipline SimpleNameExample
        final var matcher = Selector.compile("(returnType -> simpleName ~ /boolean/ | simpleName ~ /int/) & declaringClass -> !simpleName ~ /Object/ ");
        assertTrue(matcher.match(SUT_METHOD_EQUALS));
        assertTrue(matcher.match(SUT_X_METHOD_EQUALS));
        assertFalse(matcher.match(SUT_METHOD_HASHCODE));
    }

    @Test
    void testDeclaringClass() throws Exception {
        assertTrue(Selector.compile("declaringClass -> simpleName ~ /^SutTarget/").match(SUT_METHOD_VOID));
    }

    @Test
    void testImplements() {
        assertTrue(Selector.compile("implements ~ /Function/").match(SutTargetClass.X.class));
        assertTrue(Selector.compile("implements").match(SutTargetClass.X.class));
        assertFalse(Selector.compile("implements").match(SutTargetClass.class));
        assertFalse(Selector.compile("implements").match(Object.class));
    }

    @Test
    @DisplayName("tests that a class has a super class other than object")
    void testExtends() {
        assertFalse(Selector.compile("extends").match(Object.class));
        assertFalse(Selector.compile("extends").match(SutTargetClass.class));
        assertTrue(Selector.compile("extends").match(Integer.class));
    }

    @Test
    @DisplayName("compiles empty string")
    void testEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> Selector.compile("").match(IGNORED_MEMBER));
        assertThrows(IllegalArgumentException.class, () -> Selector.compile("  ").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("compiles expression with dangling space")
    void testDanglingSpace() {
        assertTrue(Selector.compile("true ").match(IGNORED_MEMBER));
        assertTrue(Selector.compile("!false ").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("throws IllegalArgumentException when we test something for 'blabla'")
    void testInvalidTest() {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("blabla").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("true and !false return true")
    void testTrue() {
        assertTrue(Selector.compile("true").match(IGNORED_MEMBER));
        assertTrue(Selector.compile("!false").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("false and !true return false")
    void testFalse() {
        assertFalse(Selector.compile("false").match(IGNORED_MEMBER));
        assertFalse(Selector.compile("!true").match(IGNORED_MEMBER));
    }


    @Test
    @DisplayName("& has higher precedence than | and there can be parentheses")
    void testPrecedence() {
        assertTrue(Selector.compile("true | false & false").match(IGNORED_MEMBER));
        assertFalse(Selector.compile("(true | false) & false").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("field with final is recognized")
    void testFinal() throws NoSuchFieldException {
        assertTrue(Selector.compile("final").match(SUT_FIELD_I));
    }


    @Test
    @DisplayName("Method that overrides method from superclass is recognized")
    void testOverrides1() throws NoSuchMethodException {
        assertTrue(Selector.compile("overrides").match(SUT_METHOD_EQUALS));
    }

    @Test
    @DisplayName("Method that does not override any method recognized")
    void testOverrides2(TestInfo info) throws NoSuchMethodException {
        final var f2 = info.getTestMethod().get();
        assertFalse(Selector.compile("overrides").match(f2));
    }

    @Test
    @DisplayName("Method that overrides method from the superclass of the superclass is recognized")
    void testOverrides3() throws NoSuchMethodException {
        assertTrue(Selector.compile("overrides").match(SUT_X_METHOD_HASHCODE));
    }

    @Test
    @DisplayName("Class simple name is matched")
    void testClassSimpleName() throws NoSuchFieldException {
        final var f = SutTargetClass.class;
        assertTrue(Selector.compile("simpleName ~ /SutTarget/").match(f));
    }

    @Test
    @DisplayName("Class canonical name is matched")
    void testClassCanonicalName() throws NoSuchFieldException {
        assertTrue(Selector.compile("canonicalName ~ /Sut.*Class/").match(SutTargetClass.class));
    }

    @Test
    @DisplayName("Class that does not extend anything extends Object")
    void testClassExtendsObject() throws NoSuchFieldException {
        assertTrue(Selector.compile("extends ~ /java\\.lang\\.Object/").match(SutTargetClass.class));
    }

    @Test
    @DisplayName("Integer class extends Number")
    void testClassExtends() throws NoSuchFieldException {
        assertTrue(Selector.compile("extends ~ /Number/").match(Integer.class));
    }

    @Test
    @DisplayName("test that a class is an annotation")
    void testClassIsAnnotation() throws NoSuchFieldException {
        assertTrue(Selector.compile("annotation").match(SutTargetClass.Z.class));
        assertFalse(Selector.compile("annotation").match(SutTargetClass.class));
    }

    @Test
    @DisplayName("field with annotation is recognized")
    void testFieldHasAnnotation() throws NoSuchFieldException {
        assertTrue(Selector.compile("annotated").match(SUT_FIELD_J));
        assertTrue(Selector.compile("annotation ~ /Deprecated/").match(SUT_FIELD_J));
        assertTrue(Selector.compile("annotation ~ /Deprecated/").match(SUT_FIELD_J));
        assertFalse(Selector.compile("annotation ~ /^Deprecated/").match(SUT_FIELD_J));
        assertTrue(Selector.compile("annotation ~ /^java\\.lang\\.Deprecated$/").match(SUT_FIELD_J));
    }


    @Test
    @DisplayName("return type can be checked for int and void")
    void testReturns() throws NoSuchMethodException {
        assertTrue(Selector.compile("returns ~ /int/").match(SUT_METHOD_Z));
        assertTrue(Selector.compile("simpleName ~ /int/").match(SUT_METHOD_Z));
        assertTrue(Selector.compile("returnType -> simpleName ~ /int/").match(SUT_METHOD_Z));
        assertFalse(Selector.compile("array").match(SUT_METHOD_Z));
        assertTrue(Selector.compile("returns ~ /void/").match(SUT_METHOD_VOID));
    }

    @Test
    @DisplayName("method with annotation is recognized")
    void testMethodHasAnnotation(TestInfo info) {
        final var f = info.getTestMethod().get();
        assertTrue(Selector.compile("annotation ~ /Test/").match(f));
        assertTrue(Selector.compile("annotation ~ /Test$/").match(f));
        assertFalse(Selector.compile("annotation ~ /^Test/").match(f));
        assertTrue(Selector.compile("annotation ~ /^org\\.junit\\.jupiter\\.api\\.Test$/").match(f));
    }


    @Test
    @DisplayName("non final field is recognized")
    void testNotFinal() throws NoSuchFieldException {
        assertFalse(Selector.compile("final").match(SUT_FIELD_J));
    }

    @Test
    @DisplayName("Testing a final field for !final is false")
    void testNegFinal() throws NoSuchFieldException {
        assertFalse(Selector.compile("!final").match(SUT_FIELD_I));
    }

    @Test
    @DisplayName("Non final field tested with !final is true")
    void testNegNotFinal() throws NoSuchFieldException {
        assertTrue(Selector.compile("!final").match(SUT_FIELD_J));
    }

    @Test
    void testPrivateAndFinal() throws NoSuchFieldException {
        assertTrue(Selector.compile("final & private").match(SUT_FIELD_I));
    }

    @Test
    void testNegPrivateAndFinal() throws NoSuchFieldException {
        assertFalse(Selector.compile("!(final | private)").match(SUT_FIELD_I));
        assertFalse(Selector.compile("!final & !private").match(SUT_FIELD_I));
    }

    @Test
    void testNegPrivateAndFinal2() throws NoSuchFieldException {
        assertTrue(Selector.compile("!final | private").match(SUT_FIELD_I));
    }

    @Test
    void testPrivateOrFinal() throws NoSuchFieldException {
        assertTrue(Selector.compile("final | private").match(SUT_FIELD_J));
    }

    @Test
    void testPrivateField() throws NoSuchFieldException {
        assertTrue(Selector.compile("private").match(SUT_FIELD_PRIVATE));
    }

    @Test
    void testProtectedField() throws NoSuchFieldException {
        assertTrue(Selector.compile("protected").match(SUT_FIELD_PROTECTED));
    }

    @Test
    void testPackageField() throws NoSuchFieldException {
        assertTrue(Selector.compile("package").match(SUT_FIELD_PACKAGE));
    }

    @Test
    void testPublicField() throws NoSuchFieldException {
        assertTrue(Selector.compile("public").match(SUT_FIELD_PUBLIC));
    }

    @Test
    void testFinalField() throws NoSuchFieldException {
        assertTrue(Selector.compile("final").match(SUT_FIELD_FINAL));
    }

    @Test
    void testTransientField() throws NoSuchFieldException {
        assertTrue(Selector.compile("transient").match(SUT_FIELD_TRANSIENT));
    }

    @Test
    void testVolatileField() throws NoSuchFieldException {
        assertTrue(Selector.compile("volatile").match(SUT_FIELD_VOLATILE));
    }

    @Test
    void testStaticField() throws NoSuchFieldException {
        assertTrue(Selector.compile("static").match(SUT_FIELD_STATIC));
    }

    @Test
    void testSynchronizedMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("synchronized").match(SUT_METHOD_SYNCHRONIZED));
    }

    @Test
    void testAbstractMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("abstract").match(SUT_METHOD_ABSTRACT));
    }

    @Test
    void testStrictMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("strict").match(SUT_METHOD_STRICT));
    }

    @Test
    void testSignatureMethod() throws NoSuchMethodException {
        //Assertions.assertTrue(Selector.compile("signature ~ /equals\\(Object\\s+arg1\\)/").match(f));
        assertThrows(IllegalArgumentException.class, () -> Selector.compile("signature ~ /equals\\(Object\\s+arg1\\)").match(SUT_METHOD_EQUALS));
    }

    @Test
    void testVarargMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("vararg").match(SUT_METHOD_VARARG));
    }

    @Test
    void testNotVarargMethod() throws NoSuchMethodException {
        assertFalse(Selector.compile("vararg").match(SUT_METHOD_NOT_VARARG));
    }

    @Test
    void testNativeMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("native").match(SYSTEM_REGISTER_NATIVES));
    }


    @Test
    void testPrivateMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("private").match(SUT_METHOD_PRIVATE));
    }


    @Test
    void testProtectedMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("protected").match(SUT_METHOD_PROTECTED));
    }

    @Test
    void testPackageMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("package").match(SUT_METHOD_STATIC));
    }


    @Test
    void testPublicMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("public").match(SUT_METHOD_PUBLIC));
    }


    @Test
    void testFinalMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("final").match(SUT_METHOD_FINAL));
    }

    @Test
    void testTransientMethod() throws NoSuchMethodException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("transient").match(SUT_METHOD_STATIC));
    }

    @Test
    void testVolatileMethod() throws NoSuchMethodException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("volatile").match(SUT_METHOD_STATIC));
    }

    @Test
    void testStaticMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("static").match(SUT_METHOD_STATIC));
    }

    @Test
    void testSynchronizedField() throws NoSuchFieldException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("synchronized").match(SUT_FIELD_I));
    }

    @Test
    void testAbstractField() throws NoSuchFieldException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("abstract").match(SUT_FIELD_I));
    }

    @Test
    void testStrictField() throws NoSuchFieldException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("strict").match(SUT_FIELD_I));
    }

    @Test
    void testVarargField() throws NoSuchFieldException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("vararg").match(SUT_FIELD_I));
    }

    @Test
    void testNativeField() throws NoSuchFieldException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("native").match(SUT_FIELD_I));
    }

    @Test
    void testThrowingField() throws NoSuchFieldException {
        assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("throws ~ /IllegalArgumentException/").match(SUT_FIELD_I));
    }

    @Test
    @DisplayName("A method that implements directly from interface is recognized")
    void testMethodImplementsInterfaceMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("implements").match(SUT_X_METHOD_APPLY));
    }

    @Test
    @DisplayName("A method that implements transitively from some super interface is recognized")
    void testMethodImplementsTransitiveInterfaceMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("implements").match(SUT_METHOD_Q));
    }

    @Test
    @DisplayName("A method that does not implement is recognized")
    void testMethodDoeNotImplementsInterfaceMethod(TestInfo info) throws NoSuchMethodException {
        assertFalse(Selector.compile("implements").match(info.getTestMethod().get()));
    }


    @Test
    void testThrowingMethod() throws NoSuchMethodException {
        assertTrue(Selector.compile("throws ~ /IllegalArgumentException/").match(SUT_METHOD_THROWS));
        assertFalse(Selector.compile("throws ~ /IllegalArgumentException/").match(SUT_METHOD_NOT_THROWS));
        assertFalse(Selector.compile("throws ~ /NullPointerException/").match(SUT_METHOD_THROWS));
    }


    @Test
    @DisplayName("Recognize that a void method is indeed void")
    void testMethodReturnTypeIsVoid() throws NoSuchMethodException {
        assertTrue(Selector.compile("void").match(SUT_METHOD_VOID));
    }

    @Test
    @DisplayName("Recognize that a method returning int is not void")
    void testMethodReturnTypeIsNotVoid() throws NoSuchMethodException {
        assertFalse(Selector.compile("void").match(SUT_METHOD_INT));
    }
}

