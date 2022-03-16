# 추상화 수준에 맞는 예외를 던져라

* 예외 번역(exception translation)
  * 상위 계층에서는 저수준 예외를 잡아 자신의 추상화 수준에 맞는 예외로 바꿔 던져야 한다.
  
```
try {
    ... // 저수준 추상화 이용
} catch (LowerLevelException e) {
    throw new HigherLevelException(...);
}
```

> AbstractSequentialList에서 수행하는 예외 번역 예(AbstractSequentialList는 List 인터페이스의 골격 구현이다.)

```
/**
 * 이 리스트 안의 지정한 위치의 원소를 반환한다.
 * @throws IndexOutOfBoundsException index가 범위 밖이라면,
 *         즉 ({@code index < 0 || index >= size()})라면 발생한다.
 */
public E get(int index) {
    ListIterator<E> i = listIterator(index);
    try {
        return i.next();
    } catch (NoSuchElementException e) {
        throw new IndexOutOfBoundsException("인덱스: "+index);
    }
}
```

* 예외를 번역할 때, 저수준 예외가 디버깅에 도움이 된다면 예외 연쇄를 사용하는 게 좋다.
  * 예외 연쇄(exception chaining) : 문제의 근본 원인인 저수준 예외를 고수준 예외에 실어 보내는 방식
  * 별도의 접근자 메서드(Throwable의 getCause 메서드)를 통해 필요하면 언제든 저수준 예외를 꺼내 볼 수 있다.
  
> 예외 연쇄
```
try {
    ... // 저수준 추상화를 이용한다.
} catch (LowerLevelException cause) {
    throw new HigherLevelException(cause);
}
```

* 고수준 예외의 생성자는 상위 클래스의 생성자에 이 원인을 건네줘 최종적으로 Throwable(Throwable) 생성자까지 건네지게 된다.

> 예외 연쇄용 생성자
```
class HigherLevelException extends Exception {
    HigherLevelException(Throwable cause) {
        super(cause);
    }
}
```

* 대부분의 표준 예외는 예외 연쇄용 생성자를 갖추고 있다.
  * 그렇지 않더라도 Throwable의 initCause 메서드를 이용해 원인을 직접 못박을 수 있다.
  * 예외 연쇄는 문제의 원인을 프로그램에서 접근할 수 있게 해주며, 원인과 고수준 예외의 스택 추적 정보를 잘 통합해준다.

* 무턱대고 예외를 전파하는 것보다 예외 번역이 우수한 방법이지만, 그렇다고 남용해서는 안 된다.
  * 가능하면 저수준 메서드가 반드시 성공하도록 해 아래 계층에서는 예외가 발생하지 않도록 하는 것이 최선이다.
  * 때로는 상위 계층 메서드의 매개변수 값을 아래 계층 메서드로 건네기 전에 미리 검사하는 방법으로 이 목적을 달성할 수 있다.
  * 아래 계층에서 예외를 피할 수 없다면, 상위 계층에서 그 예외를 조용히 처리해 문제를 API 호출자에까지 전파하지 않는 방법이 있다.
    * 이 경우 발생한 예외는 java.util.logging 같은 적절한 로깅 기능을 활용해 기록해두면 좋다.
    * 그렇게 하면 클라이언트 코드와 사용자에게 문제를 전파하지 않으면서 프로그래머가 로그를 분석해 추가 조치를 취할 수 있게 해준다.