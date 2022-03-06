# 다른 타입이 적절하다면 문자열 사용을 피하라

* 문자열은 다른 값 타입을 대신하기에 적합하지 않다.
  * 받은 데이터가 수치형이라면 int, float, BigInteger 등 적당한 수치 타입으로 변환해야 한다.
  * 예/아니오 질문의 답이라면 적절한 열거 타입이나 boolean으로 변환해야 한다.
  * 일반화하면, 기본 타입이든 참조 타입이든 적절한 값 타입이 있다면 그것을 사용하고, 없다면 새로 하나 작성해야 한다.
  
* 문자열은 열거 타입을 대신하기에 적합하지 않다. 
  * 상수를 열거할 때는 문자열보다는 열거 타입이 월등히 낫다.
  
* 문자열은 혼합 타입을 대신하기에 적합하지 않다.
  * 여러 요소가 혼합된 데이터를 하나의 문자열로 표현하는 것은 지양해야 한다.
  
> 혼합 타입을 문자열로 처리한 부적절한 예
```
String compoundKey = className + "#" + i.next();
```

* 위 방식은 단점이 많은 방식이다.
  * 두 요소를 구분해주는 문자 "#"이 두 요소 중 하나에서 쓰였다면 문제가 발생할 수 있다.
  * 각 요소를 개별로 접근하려면 문자열을 파싱해야 한다. 이는 느리고, 오류 가능성도 높다.
  * 적절한 equals, toString, compareTo 메서드를 제공할 수 없다.
  * 따라서 이러한 경우 private 정적 멤버 클래스로 만드는 게 낫다.
  
* 문자열은 권한을 표현하기에 적합하지 않다.
  * 스레드 지역변수 기능을 설계할 때, 즉 각 스레드가 자신만의 변수를 갖게 해주는 기능을 설계할 때 예전에는 클라이언트가 제공한 문자열 키로 스레드별 지역변수를 식별했다.
  
> 문자열을 사용해 권한을 구분한 예
```
public class ThreadLocal {
    private ThreadLical() {} // 객체 생성 불가
    
    // 현 스레드의 값을 키로 구분해 저장
    public static void set(String key, Object value);
    
    // (키가 가리키는) 현 스레드의 값을 반환
    public static Object get(String key);
}
```

* 위 방식의 문제는 스레드 구분용 문자열 키가 전역 이름공간에서 공유된다는 점이다.
  * 각 클라이언트가 고유한 키를 제공해야 하는데, 만약 두 클라이언트가 같은 키를 쓰기로 결정한다면, 의도치 않게 같은 변수를 공유할 수 있다.
  * 악의적인 클라이언트라면 의도적으로 같은 키를 사용해 다른 클라이언트의 값을 가져올 수도 있어 보안이 취약하다.
  * 따라서 문자열 대신 위조할 수 없는 키를 사용해 해결한다. 이 키를 권한(capacity)이라고도 한다.
  
> Key 클래스로 권한 구분
```
public class ThreadLocal {
    private ThreadLocal() {}
    
    public static class Key { // 권한
        Key() {}
    }
    
    // 위조 불가능한 고유 키 생성
    public static Key getKey() {
        return new Key();
    }
    
    public static void set(Key key, Object value);
    public static Object get(Key key);
}
```

* 위 코드를 리팩토링
  * set과 get은 이제 정적 메서드일 이유가 없으니 Key 클래스의 인스턴스 메서드로 바꾼다.
  * 이렇게 하면 Key는 그 자체가 스레드 지역변수가 된다.
  * 따라서 톱레벨 클래스인 ThreadLocal은 별달리 하는 일이 없어지므로 치워버리고, 중첩 클래스 Key의 이름을 ThreadLocal로 바꾼다.
  * 처음의 문자열 기반 API와 Key를 사용한 API는 타입안전하게 만들기 어려웠지만, ThreadLocal을 매개변수화 타입으로 선언하면 타입 안전하게 만들 수 있다.
  
> 리팩토링
```
public final class ThreadLocal<T> {
    public ThreadLocal();
    public void set(T value);
    public T get();
}
```