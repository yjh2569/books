# 지연 초기화는 신중히 사용하라

* 지연 초기화(lazy initialization)
  * 필드의 초기화 시점을 그 값이 처음 필요할 때까지 늦추는 기법
  * 값이 전혀 쓰이지 않으면 초기화도 결코 일어나지 않는다.
  * 정적 필드와 인스턴스 필드 모두에 사용할 수 있다.
  * 주로 최적화 용도로 쓰이나, 클래스와 인스턴스 초기화 때 발생하는 위험한 순환 문제를 해결하는 효과도 있다.
  
* 지연 초기화는 필요할 때까지 하지 않는다.
  * 지연 초기화하는 필드에 접근하는 비용이 커진다.
  * 지연 초기화하려는 필드 중 실제 초기화에 드는 비용에 따라 초기화된 각 필드를 얼마나 빈번히 호출하느냐에 따라 지연 초기화가 실제로는 성능을 느려지게 할 수도 있다.
  * 지연 초기화는 필드를 사용하는 인스턴스의 비율이 낮고, 그 필드를 초기화하는 비용이 큰 경우 효과적이다.
  
* 멀티스레드 환경에서는 지연 초기화를 하기 까다롭다.
  * 동기화하지 않으면 심각한 버그로 이어질 수 있다.
  
* 대부분의 상황에서는 일반적인 초기화가 지연 초기화보다 낫다.

* 지연 초기화가 초기화 순환성을 깨뜨릴 것 같으면 synchronized를 단 접근자를 사용한다.

> synchronized 접근자 방식을 이용한 인스턴스 필드의 자연 초기화
```
private FieldType field;

private synchronized FieldType getField() {
    if (field == null) {
        field = computeFieldValue();
    }
    return field;
}
```

* 성능 때문에 정적 필드를 지연 초기화해야 한다면 지연 초기화 홀더 클래스 관용구를 사용한다.

> 정적 필드용 지연 초기화 홀더 클래스 관용구
```
private static class FieldHolder {
    static final FieldType field = computeFieldValue();
}

private static FieldType getField() {
    return FieldHolder.field;
}
```

* getField가 처음 호출되는 순간 FieldHolder.field가 처음 읽히면서 FieldHolder 클래스 초기화를 촉발한다.
  * 동기화를 전혀 하지 않아 성능이 느려지지 않는다.
  * 일반적인 VM은 오직 클래스를 초기화할 때만 필드 접근을 동기화하고, 클래스 초기화가 끝난 후에는 VM이 동기화 코드를 제거하여 아무런 검사나 동기화 없이 필드에 접근하게 된다.
  
* 성능 때문에 인스턴스 필드를 지연 초기화해야 한다면 이중검사 관용구를 사용한다.
  * 초기화된 필드에 접근할 때의 동기화 비용을 없애준다.
  * 필드의 값을 동기화 없이 한 번, 동기화한 뒤 한 번 검사한다.
  * 두 번째 검사에서도 필드가 초기화되지 않았을 때만 필드를 초기화한다.
  * 필드가 초기화된 후로는 동기화하지 않으므로 해당 필드는 반드시 volatile로 선언해야 한다.
  
> 인스턴스 필드 지연 초기화용 이중검사 관용구
```
private volatile FieldType field;

private FieldType getField() {
    FieldType result = field;
    if (result != null) return result;
    synchronized(this) {
        if (field == null) field = computeFieldValue();
        return field;
    }
}
```

* result 지역변수는 필드가 이미 초기화된 상황에서는 그 필드를 딱 한 번만 읽도록 보장하는 역할을 한다.
  * 반드시 필요하지는 않지만 성능을 높여주고, 저수준 동시성 프로그래밍에 표준적으로 적용되는 방법이다.
  
* 반복해서 초기화해도 상관없는 인스턴스 필드를 지연 초기화해야 할 때 이중검사에서 두 번째 검사를 생략할 수도 있다.

* 모든 초기화 기법은 기본 타입 필드와 객체 참조 필드 모두에 적용할 수 있다.
  * 필드이 타입이 long과 double을 제외한 다른 기본 타입이면 단일검사의 필드 선언에서 volatile 한정자를 없애도 된다.