package lesson1;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface BeforeSuite {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface AfterSuite {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface Test {
    //Минимальный и максимальный приоритеты. При выходе за пределы отрезка будет выброшено исключение
    int minPriority = 1, maxPriority = 10;
    int priority() default 5;
}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface BeforeTest {}

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface AfterTest {}

class Suite {

    @Test(priority = -1) void tInvalid() {
        System.out.println("Invalid -1");
    }

    @BeforeSuite static void start () {
        System.out.println("START");
    }

    @AfterSuite static void end() {
        System.out.println("END");
    }

    @Test(priority = 13) void t13() {
        System.out.println("Invalid t13");
    }

    @Test(priority = 10) void t10() {
        System.out.println("t10");
    }

    @Test(priority = 1) void t1() {
        System.out.println("t1");
    }

    @AfterSuite static void end1() {
        System.out.println("END");
    }

    @Test() void tDefault() {
        System.out.println("default t5");
    }

    @BeforeSuite static void start1 () {
        System.out.println("START");
    }

    @Test(priority = 9) void t9() {
        System.out.println("t9");
    }

    @AfterTest static void endTest() { System.out.println("----------------------------"); }

    @BeforeTest static void startTest() {
        System.out.println("----------------------------");
    }

}

public class TestRunner {
    public static void runTests(Class<?> c) throws Exception {
        Method before = null, after = null, beforeTest = null, afterTest = null;
        List<Method> tests = new ArrayList<Method>();
        for (Method m : c.getDeclaredMethods()) {
            try {

                if (m.isAnnotationPresent(BeforeSuite.class)) {
                    if (before != null)
                        throw new IllegalArgumentException("Multiple @BeforeSuite annotations found in " + m.getName());
                    before = m;
                } else if (m.isAnnotationPresent(AfterSuite.class)) {
                    if (after != null)
                        throw new IllegalStateException("Multiple @AfterSuite annotations found in " + m.getName());
                    after = m;
                } else if (m.isAnnotationPresent(Test.class)) {
                    if (m.getAnnotation(Test.class).priority() < Test.minPriority ||
                            m.getAnnotation(Test.class).priority() > Test.maxPriority)
                        throw new IllegalArgumentException("Priority must be between 0..10 in " + m.getName());
                    tests.add(m);
                } else if (m.isAnnotationPresent(BeforeTest.class)) beforeTest = m;
                else if (m.isAnnotationPresent(AfterTest.class)) afterTest = m;
            } catch (IllegalArgumentException | IllegalStateException e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
        }
        tests.sort(Comparator.comparingInt(
                (Method m) -> m.getAnnotation(Test.class).priority()).reversed());
        Object inst = c.getDeclaredConstructor().newInstance();
        if (before != null) before.invoke(null);
        for (Method m : tests) {
            if (beforeTest != null) beforeTest.invoke(null);
            m.invoke(inst);
            if (afterTest != null) afterTest.invoke(null);
        }
        if (after != null) after.invoke(null);

    }

    public static void main(String[] args) throws Exception {
        runTests(Suite.class);
    }
}
