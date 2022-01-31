# 이왕이면 제네릭 메서드로 만들라

* 클래스와 마찬가지로 메서드도 제네릭으로 만들 수 있다.
  * 매개변수화 타입을 받는 정적 유틸리티 메서드는 보통 제네릭이다.
  
> raw type을 사용하는 union 메서드
```
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1);
    result.addAll(s2);
    return result;
}
```

* 위 메서드는 컴파일은 되지만 타입이 안전하지 않아 경고가 발생한다.
  * 메서드 선언에서의 세 집합(입력 2개, 반환 1개)의 원소 타입을 타입 매개변수로 명시하고, 메서드 안에서도 이 타입 매개변수만 사용하게 수정하면 된다.
  * 타입 매개변수들을 선언하는 타입 매개변수 목록은 메서드의 제한자와 반환 타입 사이에 온다.
  
> 제네릭 메서드
```
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = new HashSet<>(s1);
    result.addAll(s2);
    return result;
}
```

* 때때로 불변 객체를 여러 타입으로 활용할 수 있게 만들어야 할 때가 있다.
  * 제네릭은 런타임에 타입 정보가 소거되므로 하나의 객체를 어떤 타입으로든 매개변수화할 수 있다.
  * 하지만 이렇게 하려면 요청한 타입 매개변수에 맞게 매번 그 객체의 타입을 바꿔주는 정적 팩토리를 만들어야 한다. 이 패턴을 제네릭 싱글턴 팩토리라고 한다.
  
> 제네릭 싱글턴 팩토리 패턴
```
private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

@SuppressWarnings("unchecked")
public static <T> UnaryOperator<T> identityFunction() {
    return (UnaryOperator<T>) IDENTITY_FN;
}
```

* IDENTITY_FN을 UnaryOperator\<T>로 형변환하면 비검사 형변환 경고를 발생한다.
  * T가 어떤 타입이든 UnaryOperator\<Object>는 UnaryOperator\<T>가 아니기 때문이다.
  * 하지만 항등함수란 입력 값을 수정 없이 그대로 반환하는 특별한 함수이므로, T가 어떤 타입이든 UnaryOperator\<T>를 사용해도 타입 안전한다.
  * 따라서 @SuppressWarnings 애너테이션을 추가한다.
  
> 제네릭 싱글턴을 사용하는 예
```
public static void main(String[] args) {
    String[] strings = {"삼베", "대마", "나일론"};
    UnaryOperator<String> sameString = identityFunction();
    for (String s : strings) {
        System.out.println(sameString.apply(s));
    }
    
    Number[] numbers = {1, 2.0, 3L};
    for (Number n : numbers) {
        System.out.println(sameNumber.apply(n));
    }
}
```

* 상대적으로 드물지만 자기 자신이 들어간 표현식을 사용해 타입 매개변수의 허용 범위를 한정할 수 있다.
  * 이를 재귀적 타입 한정이라 한다.
  * 재귀적 타입 한정은 주로 타입의 자연적 순서를 정하는 Comparable 인터페이스와 함께 쓰인다.
  
> Comparable 인터페이스
```
public interface Comparable<T> {
    int compareTo(T o);
}
```

* 여기서 타입 매개변수 T는 Comparable<T>를 구현한 타입이 비교할 수 있는 원소의 타입으로, 보통 자신과 같은 타입의 원소와만 비교할 수 있기에 이를 자기 자신으로 하는 경우가 대부분이다.


> 재귀적 타입 한정을 이용해 상호 비교할 수 있음을 표현
```
public static <E extends Comparable<E>> E max(Collection<E> c) {
    if (c.isEmpty()) throw new IllegalArgumentException("컬렉션이 비어 있습니다.");
    
    E result = null;
    for (E e : c) {
        if (result == null || e.compareTo(result) > 0) {
            result = Objects.requireNonNull(e);
        }
    }
    return result;
}
```