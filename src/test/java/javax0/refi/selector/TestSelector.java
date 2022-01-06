package javax0.refi.selector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TestSelector {
    private static final Member IGNORED_MEMBER = null;
    private static final Class[] NO_ARGS = null;

    @Test
    @DisplayName("List all private primitive fields")
    void testStream() {
        Set<Field> fields =
            Arrays.stream(SutTargetClass.class.getDeclaredFields())
                .filter(Selector.compile("private & primitive")::match)
                .collect(Collectors.toSet());
        Assertions.assertEquals(3, fields.size());
    }

    @Test
    @DisplayName("When the argument to match is null the return value has to be false except for 'null' and 'ture'")
    void testNullIsFalse() throws Exception {
        for (final var key : getFunctionMapKeys("selectors")) {
            Assertions.assertEquals("null".equals(key) || "true".equals(key),
                Selector.compile(key).match(null));
        }
    }

    @Test
    @DisplayName("When the argument to match is null then any conversion will return also null")
    void testNullIsNull() throws Exception {
        for (final var key : getFunctionMapKeys("converters")) {
            Assertions.assertTrue(Selector.compile(key + " -> null").match(null));
        }
    }

    @Test
    @DisplayName("When the argument to match is null then any regular expression matching is false")
    void testNullIsFalseInRegex() throws Exception {
        for (final var key : getFunctionMapKeys("regexMemberSelectors")) {
            Assertions.assertFalse(Selector.compile(key + " ~/.*/").match(null));
        }
    }

    private Set<String> getFunctionMapKeys(final String fieldName) throws NoSuchFieldException, IllegalAccessException {
        final var selectorsField = Selector.class.getDeclaredField(fieldName);
        selectorsField.setAccessible(true);
        final var selectorObject = Selector.compile("true");
        return ((Map) selectorsField.get(selectorObject)).keySet();
    }


    @Test
    @DisplayName("Test that a normal method is not a birdge method")
    void testNoBridge() throws Exception {
        Assertions.assertTrue(Selector.compile("!bridge").match(SutTargetClass.class.getDeclaredMethod("method_void")));
    }

    @Test
    @DisplayName("Test that Object.declaringClass returns null and that returns false when checked")
    void testNoParent() {
        Assertions.assertFalse(Selector.compile("declaringClass -> !null & simpleName ~ /^Test/").match(Object.class));
        Assertions.assertFalse(Selector.compile("declaringClass -> simpleName ~ /^Test/").match(Object.class));
    }

    @Test
    @DisplayName("Test that nestHost converter works")
    void testNestingHost() {
        Assertions.assertTrue(Selector.compile("nestHost -> (!null & simpleName ~ /^Object/)").match(Object.class));
        Assertions.assertTrue(Selector.compile("nestHost -> (!null & simpleName ~ /^Map/)").match(Map.Entry.class));
    }

    @Test
    void testDeclaringClassDemo() throws Exception {
        final var equals = SutTargetClass.class.getMethod("equals", Object.class);
        final var xEquals = SutTargetClass.X.class.getMethod("equals", Object.class);
        final var hashCode = SutTargetClass.class.getMethod("hashCode");
        // snipline SimpleNameExample
        final var matcher = Selector.compile("(returnType -> simpleName ~ /boolean/ | simpleName ~ /int/) & declaringClass -> !simpleName ~ /Object/ ");
        Assertions.assertTrue(matcher.match(equals));
        Assertions.assertTrue(matcher.match(xEquals));
        Assertions.assertFalse(matcher.match(hashCode));
    }

    @Test
    void testDeclaringClass() throws Exception {
        Assertions.assertTrue(Selector.compile("declaringClass -> simpleName ~ /^SutTarget/").match(SutTargetClass.class.getDeclaredMethod("method_void")));
    }

    @Test
    void testImplements() {
        Assertions.assertTrue(Selector.compile("implements ~ /Function/").match(SutTargetClass.X.class));
        Assertions.assertTrue(Selector.compile("implements").match(SutTargetClass.X.class));
        Assertions.assertFalse(Selector.compile("implements").match(SutTargetClass.class));
        Assertions.assertFalse(Selector.compile("implements").match(Object.class));
    }

    @Test
    @DisplayName("tests that a class has a super class other than object")
    void testExtends() {
        Assertions.assertFalse(Selector.compile("extends").match(Object.class));
        Assertions.assertFalse(Selector.compile("extends").match(SutTargetClass.class));
        Assertions.assertTrue(Selector.compile("extends").match(Integer.class));
    }

    @Test
    @DisplayName("compiles empty string")
    void testEmptyString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Selector.compile("").match(IGNORED_MEMBER));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Selector.compile("  ").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("compiles expression with dangling space")
    void testDanglingSpace() {
        Assertions.assertTrue(Selector.compile("true ").match(IGNORED_MEMBER));
        Assertions.assertTrue(Selector.compile("!false ").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("throws IllegalArgumentException when we test something for 'blabla'")
    void testInvalidTest() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("blabla").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("true and !false return true")
    void testTrue() {
        Assertions.assertTrue(Selector.compile("true").match(IGNORED_MEMBER));
        Assertions.assertTrue(Selector.compile("!false").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("false and !true return false")
    void testFalse() {
        Assertions.assertFalse(Selector.compile("false").match(IGNORED_MEMBER));
        Assertions.assertFalse(Selector.compile("!true").match(IGNORED_MEMBER));
    }


    @Test
    @DisplayName("& has higher precedence than | and there can be parentheses")
    void testPrecedence() {
        Assertions.assertTrue(Selector.compile("true | false & false").match(IGNORED_MEMBER));
        Assertions.assertFalse(Selector.compile("(true | false) & false").match(IGNORED_MEMBER));
    }

    @Test
    @DisplayName("field with final is recognized")
    void testFinal() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertTrue(Selector.compile("final").match(f));
    }


    @Test
    @DisplayName("Method that overrides method from superclass is recognized")
    void testOverrides1() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("equals", Object.class);
        Assertions.assertTrue(Selector.compile("overrides").match(f));
    }

    @Test
    @DisplayName("Method that does not override any method recognized")
    void testOverrides2(TestInfo info) throws NoSuchMethodException {
        final var f2 = info.getTestMethod().get();
        Assertions.assertFalse(Selector.compile("overrides").match(f2));
    }

    @Test
    @DisplayName("Method that overrides method from the superclass of the superclass is recognized")
    void testOverrides3() throws NoSuchMethodException {
        final var f3 = SutTargetClass.X.class.getDeclaredMethod("hashCode", NO_ARGS);
        Assertions.assertTrue(Selector.compile("overrides").match(f3));
    }

    @Test
    @DisplayName("Class simple name is matched")
    void testClassSimpleName() throws NoSuchFieldException {
        final var f = SutTargetClass.class;
        Assertions.assertTrue(Selector.compile("simpleName ~ /SutTarget/").match(f));
    }

    @Test
    @DisplayName("Class canonical name is matched")
    void testClassCanonicalName() throws NoSuchFieldException {
        final var f = SutTargetClass.class;
        Assertions.assertTrue(Selector.compile("canonicalName ~ /Sut.*Class/").match(f));
    }

    @Test
    @DisplayName("Class that does not extend anything extends Object")
    void testClassExtendsObject() throws NoSuchFieldException {
        final var f = SutTargetClass.class;
        Assertions.assertTrue(Selector.compile("extends ~ /java\\.lang\\.Object/").match(f));
    }

    @Test
    @DisplayName("Integer class extends Number")
    void testClassExtends() throws NoSuchFieldException {
        Assertions.assertTrue(Selector.compile("extends ~ /Number/").match(Integer.class));
    }

    @Test
    @DisplayName("test that a class is an annotation")
    void testClassIsAnnotation() throws NoSuchFieldException {
        Assertions.assertTrue(Selector.compile("annotation").match(SutTargetClass.Z.class));
        Assertions.assertFalse(Selector.compile("annotation").match(SutTargetClass.class));
    }

    @Test
    @DisplayName("field with annotation is recognized")
    void testFieldHasAnnotation() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("j");
        Assertions.assertTrue(Selector.compile("annotated").match(f));
        Assertions.assertTrue(Selector.compile("annotation ~ /Deprecated/").match(f));
        Assertions.assertTrue(Selector.compile("annotation ~ /Deprecated/").match(f));
        Assertions.assertFalse(Selector.compile("annotation ~ /^Deprecated/").match(f));
        Assertions.assertTrue(Selector.compile("annotation ~ /^java\\.lang\\.Deprecated$/").match(f));
    }


    @Test
    @DisplayName("return type can be checked for int and void")
    void testReturns() throws NoSuchMethodException {
        final var f1 = SutTargetClass.class.getDeclaredMethod("z");
        Assertions.assertTrue(Selector.compile("returns ~ /int/").match(f1));
        Assertions.assertTrue(Selector.compile("simpleName ~ /int/").match(f1));
        Assertions.assertTrue(Selector.compile("returnType -> simpleName ~ /int/").match(f1));
        Assertions.assertFalse(Selector.compile("array").match(f1));
        final var f2 = SutTargetClass.class.getDeclaredMethod("method_void");
        Assertions.assertTrue(Selector.compile("returns ~ /void/").match(f2));
    }

    @Test
    @DisplayName("method with annotation is recognized")
    void testMethodHasAnnotation() throws NoSuchMethodException {
        final var f = TestSelector.class.getDeclaredMethod("testMethodHasAnnotation", (Class<?>[]) null);
        Assertions.assertTrue(Selector.compile("annotation ~ /Test/").match(f));
        Assertions.assertTrue(Selector.compile("annotation ~ /Test$/").match(f));
        Assertions.assertFalse(Selector.compile("annotation ~ /^Test/").match(f));
        Assertions.assertTrue(Selector.compile("annotation ~ /^org\\.junit\\.jupiter\\.api\\.Test$/").match(f));
    }


    @Test
    @DisplayName("non final field is recognized")
    void testNotFinal() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("j");
        Assertions.assertFalse(Selector.compile("final").match(f));
    }

    @Test
    @DisplayName("Testing a final field for !final is false")
    void testNegFinal() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertFalse(Selector.compile("!final").match(f));
    }

    @Test
    @DisplayName("Non final field tested with !final is true")
    void testNegNotFinal() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("j");
        Assertions.assertTrue(Selector.compile("!final").match(f));
    }

    @Test
    void testPrivateAndFinal() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertTrue(Selector.compile("final & private").match(f));
    }

    @Test
    void testNegPrivateAndFinal() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertFalse(Selector.compile("!(final | private)").match(f));
        Assertions.assertFalse(Selector.compile("!final & !private").match(f));
    }

    @Test
    void testNegPrivateAndFinal2() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertTrue(Selector.compile("!final | private").match(f));
    }

    @Test
    void testPrivateOrFinal() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("j");
        Assertions.assertTrue(Selector.compile("final | private").match(f));
    }

    @Test
    void testPrivateField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_private");
        Assertions.assertTrue(Selector.compile("private").match(f));
    }

    @Test
    void testProtectedField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_protected");
        Assertions.assertTrue(Selector.compile("protected").match(f));
    }

    @Test
    void testPackageField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_package");
        Assertions.assertTrue(Selector.compile("package").match(f));
    }

    @Test
    void testPublicField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_public");
        Assertions.assertTrue(Selector.compile("public").match(f));
    }

    @Test
    void testFinalField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_final");
        Assertions.assertTrue(Selector.compile("final").match(f));
    }

    @Test
    void testTransientField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_transient");
        Assertions.assertTrue(Selector.compile("transient").match(f));
    }

    @Test
    void testVolatileField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_volatile");
        Assertions.assertTrue(Selector.compile("volatile").match(f));
    }

    @Test
    void testStaticField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("var_static");
        Assertions.assertTrue(Selector.compile("static").match(f));
    }

    @Test
    void testSynchronizedMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_synchronized");
        Assertions.assertTrue(Selector.compile("synchronized").match(f));
    }

    @Test
    void testAbstractMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.X.class.getDeclaredMethod("method_abstract");
        Assertions.assertTrue(Selector.compile("abstract").match(f));
    }

    @Test
    void testStrictMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_strict");
        Assertions.assertTrue(Selector.compile("strict").match(f));
    }

    @Test
    void testSignatureMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("equals", Object.class);
        //Assertions.assertTrue(Selector.compile("signature ~ /equals\\(Object\\s+arg1\\)/").match(f));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Selector.compile("signature ~ /equals\\(Object\\s+arg1\\)").match(f));
    }

    @Test
    void testVarargMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_vararg", Object[].class);
        Assertions.assertTrue(Selector.compile("vararg").match(f));
    }

    @Test
    void testNotVarargMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_notVararg", Object[].class);
        Assertions.assertFalse(Selector.compile("vararg").match(f));
    }

    @Test
    void testNativeMethod() throws NoSuchMethodException {
        final var f = System.class.getDeclaredMethod("registerNatives");
        Assertions.assertTrue(Selector.compile("native").match(f));
    }


    @Test
    void testPrivateMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_private");
        Assertions.assertTrue(Selector.compile("private").match(f));
    }


    @Test
    void testProtectedMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_protected");
        Assertions.assertTrue(Selector.compile("protected").match(f));
    }

    @Test
    void testPackageMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_static");
        Assertions.assertTrue(Selector.compile("package").match(f));
    }


    @Test
    void testPublicMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_public");
        Assertions.assertTrue(Selector.compile("public").match(f));
    }


    @Test
    void testFinalMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_final");
        Assertions.assertTrue(Selector.compile("final").match(f));
    }

    @Test
    void testTransientMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_static");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("transient").match(f));
    }

    @Test
    void testVolatileMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_static");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("volatile").match(f));
    }

    @Test
    void testStaticMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.class.getDeclaredMethod("method_static");
        Assertions.assertTrue(Selector.compile("static").match(f));
    }

    @Test
    void testSynchronizedField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("synchronized").match(f));
    }

    @Test
    void testAbstractField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("abstract").match(f));
    }

    @Test
    void testStrictField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("strict").match(f));
    }

    @Test
    void testVarargField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("vararg").match(f));
    }

    @Test
    void testNativeField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("native").match(f));
    }

    @Test
    void testThrowingField() throws NoSuchFieldException {
        final var f = SutTargetClass.class.getDeclaredField("i");
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Selector.compile("throws ~ /IllegalArgumentException/").match(f));
    }

    @Test
    @DisplayName("A method that implements directly from interface is recognized")
    void testMethodImplementsInterfaceMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.X.class.getDeclaredMethod("apply", Object.class);
        Assertions.assertTrue(Selector.compile("implements").match(f));
    }

    @Test
    @DisplayName("A method that implements transitively from some super interface is recognized")
    void testMethodImplementsTransitiveInterfaceMethod() throws NoSuchMethodException {
        final var f = SutTargetClass.Y.class.getDeclaredMethod("q", NO_ARGS);
        Assertions.assertTrue(Selector.compile("implements").match(f));
    }

    @Test
    @DisplayName("A method that does not implement is recognized")
    void testMethodDoeNotImplementsInterfaceMethod(TestInfo info) throws NoSuchMethodException {
        Assertions.assertFalse(Selector.compile("implements").match(info.getTestMethod().get()));
    }


    @Test
    void testThrowingMethod() throws NoSuchMethodException {
        final var method_throws = SutTargetClass.class.getDeclaredMethod("method_throws");
        Assertions.assertTrue(Selector.compile("throws ~ /IllegalArgumentException/").match(method_throws));
        final var method_notThrows = SutTargetClass.class.getDeclaredMethod("method_notThrows");
        Assertions.assertFalse(Selector.compile("throws ~ /IllegalArgumentException/").match(method_notThrows));
        Assertions.assertFalse(Selector.compile("throws ~ /NullPointerException/").match(method_throws));
    }


    @Test
    @DisplayName("Recognize that a void method is indeed void")
    void testMethodReturnTypeIsVoid() throws NoSuchMethodException {
        final var thisMethod = SutTargetClass.class.getDeclaredMethod("method_void");
        Assertions.assertTrue(Selector.compile("void").match(thisMethod));
    }

    @Test
    @DisplayName("Recognize that a method returning int is not void")
    void testMethodReturnTypeIsNotVoid() throws NoSuchMethodException {
        final var thisMethod = SutTargetClass.class.getDeclaredMethod("method_int");
        Assertions.assertFalse(Selector.compile("void").match(thisMethod));
    }
}

