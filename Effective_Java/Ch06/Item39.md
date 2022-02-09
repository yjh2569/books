# 명명 패턴보다 애너테이션을 사용하라

* 전통적으로 도구나 프레임워크가 특별히 다뤄야 할 프로그램 요소에는 딱 구분되는 명명 패턴을 적용해왔다.
> 테스트 프레임워크인 JUnit은 버전 3까지 테스트 메서드 이름을 test로 시작하게끔 헀다.

* 명명 패턴의 문제점
  * 오타가 나면 안 된다.
  * 올바른 프로그램 요소에서만 사용된다고 보증할 방법이 없다.
  * 프로그램 요소를 매개변수로 전달할 마땅한 방법이 없다.
  
* 위 문제를 해결하기 위해 애너테이션이 도입되었다.

> @Test 애너테이션 타입을 선언하는 예시
```
import java.lang.annotation.*;

/**
 * 테스트 메서드임을 선언하는 애너테이션
 * 매개변수 없는 정적 메서드 전용
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```

* 메타애너테이션
  * 애너테이션 타입 선언에 다는 애너테이션
  * 위 예시에서 @Retention과 @Target이 이에 속한다.
  * @Retention(RetentionPolicy.RUNTIME)은 @Test가 런타임에도 유지되어야 한다는 표시다. 이를 생략하면 테스트 도구는 @Test를 인식할 수 없다.
  * @Target(ElementType.METHOD) 메타애너테이션은 @Test가 반드시 메서드 선언에서만 사용돼야 한다고 알려준다.
  
> 마커 애너테이션을 사용한 프로그램 예
```
public class Sample {
    @Test public static void m1() { } // 성공해야 한다.
    public static void m2() { }
    @Test public static void m3() { // 실패해야 한다.
        throw new RuntimeException("실패");
    }
    public static void m4() { }
    @Test public void m5() { } // 잘못 사용한 예 : 정적 메서드가 아니다.
    public static void m6() { }
    @Test public static void m7() { // 실패해야 한다.
        throw new RuntimeException("실패");
    }
    public static void m8() { }
}
```
  
* 위 예시에서 m1은 성공, m3와 m7은 실패, m5는 잘못 사용했고, 나머지는 테스트 도구가 무시한다.

* @Test 애너테이션이 Sample 클래스의 의미에 직접적인 영향을 주지는 않는다.
  * 애너테이션에 관심 있는 프로그램에게 추가 정보를 제공할 뿐이다.
  * 대상 코드의 의미는 그대로 둔 채 그 애너테이션에 관심 있는 도구에서 특별한 처리를 할 기회를 준다.
  
> 마커 애너테이션을 처리하는 프로그램
```
import java.lang.reflect.*;

public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;
        Class<?> testClass = Class.forName(args[0]);
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                tests++;
                try {
                    m.invoke(null); // 테스트 메서드가 예외를 던지면 리플렉션 매커니즘이 InvocationTargetException으로 감싸서 다시 던진다.
                    passed++;
                } catch (InvocationTargetException wrappedExc) {
                    Throwable exc = wrappedExc.getCause();
                    System.out.println(m + " 실패: " + exc);
                } catch (Exception exc) { // @Test 애너테이션을 잘못 사용한 경우
                    System.out.println("잘못 사용한 @Test: " + m);
                }
            }
        }
        System.out.printf("성공: %d, 실패: %d%n", passed, tests - passed);
    }
}
```

> 매개변수 하나를 받는 애너테이션 타입
```
import java.lang.annotation.*;

/**
 * 명시한 예외를 던져야만 성공하는 테스트 메서드용 애너테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable> value(); // 매개변수의 타입은 Throwable을 확장한 클래스의 Class 객체로 모든 예외 타입을 다 수용한다.
}

// 실제 활용 예
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() { // 성공해야 한다.
        int i = 0;
        i = i / i;
    }
    
    @ExceptionTest(ArithmeticException.class)
    public static void m2() { // 실패해야 한다. (다른 예외 발생)
        int[] a = new int[0];
        int i = a[1];
    }
    
    @ExceptionTest(ArithmeticException.class)
    public static void m3() { } // 실패해야 한다. (예외가 발생하지 않음)
}
```

> 이 애너테이션을 다룰 수 있게 테스트 도구 수정
```
if (m.isAnnotationPresent(Test.class)) {
    tests++;
    try {
        m.invoke(null);
        System.out.printf("테스트 %s 실패: 예외를 던지지 않음%n", m);
    } catch (InvocationTargetException wrappedExc) {
        Throwable exc = wrappedExc.getCause();
        Class<? extends Throwable> excType = m.getAnnotation(ExceptionTest.class).value();
        if (excType.isInstance(exc)) passed++;
        else {
            System.out.printf("테스트 %s 실패: 기대한 예외 %s, 발생한 예외 %s%n", m, excType.getName(), exc);
        }
    } catch (Exception exc) {
        System.out.println("잘못 사용한 @ExceptionTest: " + m);
    }
}
```

* 예외를 여러 개 명시하고 그중 하나가 발생하면 성공하게 만들 수도 있다.

```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable>[] value();
}
```

* 원소가 여럿인 배열을 지정할 떄는 다음과 같이 원소들을 중괄호로 감싸고 쉼표로 구분해주면 된다.

```
@ExceptionTest({ IndexOutOfBoundsException.class, NullPointerException.class })
public static void doublyBad() {
    List<String> list = new ArrayList<>();
    list.addAll(5, null); // NullPointerException
}

// 새로운 ExceptionTest를 지원하도록 테스트 러너를 수정
if (m.isAnnotationPresent(ExceptionTest.class)) {
    tests++;
    try {
        m.invoke(null);
        System.out.printf("테스트 %s 실패: 예외를 던지지 않음%n", m);
    } catch (InvocationTargetException wrappedExc) {
        Throwable exc = wrappedExc.getCause();
        int oldPassed = passed;
        Class<? extends Throwable>[] excTypes = m.getAnnotation(ExceptionTest.class).value();
        for (Class<? extends Throwable> excType : excTypes) {
            if (excType.isInstance(exc)) {
                passed++;
                break;
            }
        }
        if (passed == oldPassed) System.out.printf("테스트 %s 실패: %s %n", m, exc);
    } 
}
```

* 자바 8에서는 여러 개의 값을 받는 애너테이션을 배열 매개변수 대신 @Repeatable 메타애너테이션을 다는 방식으로 만들 수 있다.
  * @Repeatable을 단 애너테이션은 하나의 프로그램 요소에 여러 번 달 수 있다.
  * 단, @Repeatable을 단 애너테이션을 반환하는 컨테이너 애너테이션을 하나 더 정의하고, @Repeatable에 이 컨테이녀 애너테이션의 class 객체를 매개변수로 전달해야 한다.
  * 컨테이너 애너테이션은 내부 애너테이션 타입의 배열을 반환하는 value 메서드를 정의해야 한다.
  * 컨테이너 애너테이션 타입에는 적절한 보존 정책(@Retention)과 적용 대상(@Target)을 명시해야 한다.
  
> @Repeatable을 단 애너테이션의 예
```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}

// 컨테이너 애너테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTestContainer {
    ExceptionTest[] value();
}

// 사용 예시
@ExceptionTest(IndexOutOfBoundsException.class)
@ExceptionTest(NullPointerException.class)
public static void doublyBad() {...}
```

* @Repeatable을 단 애너테이션을 여러 개 달면 하나만 달았을 때와 구분하기 위해 해당 컨테이너 애너테이션 타입이 적용돼 isAnnotationPresent 메서드가 애너테이션을 여러 개 단 메서드를 무시할 수 있다.
  * 따라서 isAnnotationPresent로 컨테이너 애너테이션이 달렸는지 검사한다.
  
```
if (m.isAnnotationPresent(ExceptionTest.class) || m.isAnnotationPresent(ExceptionTestContainer.class)) {
    tests++;
    try {
        m.invoke(null);
        System.out.printf("테스트 %s 실패: 예외를 던지지 않음%n", m);
    } catch (InvocationTargetException wrappedExc) {
        Throwable exc = wrappedExc.getCause();
        int oldPassed = passed;
        ExceptionTest[] excTests = m.getAnnotationByType(ExceptionTest.class);
        for (ExceptionTest excTest : excTests) {
            if (excTest.value().isInstance(exc)) {
                passed++;
                break;
            }
        }
        if (passed == oldPassed) System.out.printf("테스트 %s 실패: %s %n", m, exc);
    } 
}
```