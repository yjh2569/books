# 옵셔널 반환은 신중히 하라

* 자바 8 전에는 메서드가 특정 조건에서 값을 반환할 수 없을 때 예외를 던지거나 null을 반환했다.
  * 예외는 진짜 예외적인 상황에서만 사용해야 하며 예외를 생성할 때 스택 추적 전체를 캡처하므로 비용이 만만치 않다.
  * null을 반환하면 해당 메서드를 호출할 때 별도의 null 처리 코드를 추가해야 한다.
  
* 이 문제를 보완하기 위해 자바 8에서 Optional\<T\>가 추가되었다.
  * Optional\<T\>는 null이 아닌 T 타입 참조를 하나 담거나, 혹은 아무것도 담지 않을 수 있다.
  * 아무것도 담지 않은 옵셔널은 비었다고 한다. 어떤 값을 담은 옵셔널은 비지 않았다고 한다.
  * 옵셔널은 원소를 최대 1개 가질 수 있는 불변 컬렉션이다.
  * T를 반환해야 하지만 특정 조건에서는 아무것도 반환하지 않을 때 T 대신 Optional\<T\>를 반환하도록 선언하면 된다.
  * 옵셔널을 반환하는 메서드는 예외를 던지는 메서드보다 유연하고 사용하기 쉬우며, null을 반환하는 메서드보다 오류 가능성이 작다.
  
> Optional\<E\> 사용 예시
```
public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    if (c.isEmpty()) return Optional.empty();
    E result = null;
    for (E e : c) {
        if (result == null || e.compareTo(result) > 0) result = Objects.requireNonNull(e);
    }
    return Optional.of(result);
}
```

* 빈 옵셔널은 Optional.empty()로 만들고, 값이 든 옵셔널은 Optional.of(value)로 생성했다.
  * null 값도 허용하는 옵셔널을 만들려면 Optional.ofNullable(value)를 사용하면 된다. 하지만 옵셔널을 반환하는 메서드에서는 절대 null을 반환하면 안 된다.
  
* 스트림의 종단 연산 중 상당수가 옵셔널을 반환한다.

```
public static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    return c.stream().max(Comparator.naturalOrder());
}
```

* 옵셔널은 검사 예외와 취지가 비슷하다.
  * 반환값이 없을 수도 있음을 API 사용자에게 명확히 알려준다.
  * 검사 예외를 던지면 클라이언트에서는 반드시 이에 대처하는 코드를 작성해넣어야 하는 것처럼, 메서드가 옵셔널을 반환하는 경우 클라이언트는 값을 받지 못했을 때 취할 행동을 선택해야 한다.
  
* 클라이언트가 옵셔널을 반환하는 메서드로부터 값을 받지 못할 경우 취할 수 있는 행동
  * 기본값을 설정한다.
  * 상황에 맞는 예외를 던진다.
  * 항상 값이 채워져 있다고 확신하면 그냥 곧바로 값을 꺼내 사용한다. 단, 잘못 판단한 경우라면 NoSuchElementException이 발생한다.
  
```
String lastWordInLexicon = max(words).orElse("단어 없음...");
Toy myToy = max(toys).orElseThrow(TemperTantrumException::new);
Element lastNobleGas = max(Elements.NOBLE_GASES).get();
```

* 이따금 기본값을 설정하는 비용이 아주 커서 부담이 될 때가 있다.
  * Supplier\<T\>를 인수로 받는 orElseGet을 사용하면 값이 처음 필요할 때 Supplier\<T\>를 사용해 생성하므로 초기 설정 비용을 낮출 수 있다.
  * 앞에서 나온 기본 메서드로 처리하기 어렵다면 filter, map, flatMap, ifPresent 등을 통해 문제를 해결할 수 있는지 검토한다.
  
* 자바 9에서는 Optional에 stream() 메서드가 추가되었다.
  * 이 메서드는 Optional을 Stream으로 변환해주는 어댑터다.
  * 옵셔널에 값이 있으면 그 값을 원소로 담은 스트림으로, 값이 없다면 빈 스트림으로 변환한다.
  
* 컬렉션, 스트림, 배열, 옵셔널 같은 컨테이너 타입은 옵셔널로 감싸면 안 된다.
  * Optional\<List\<T\>\>를 반환하기보다는 빈 List\<\>를 반환하는 게 낫다.
  
* 결과가 없을 수 있으며, 클라이언트가 이 상황을 특별하게 처리해야 한다면 Optional\<\>를 반환한다.
  * 다만 이렇게 하면서 Optional이라는 객체를 초기화하고, 값을 꺼낼 때 메서드를 호출해야 하므로 성능 면에서는 다소 좋지 않을 수도 있다.
  
* int, long, double 전용 옵셔널 클래스인 OptionalInt, OptionalLong, OptionalDouble이 있기 때문에, 박싱된 기본 타입을 담은 옵셔널은 사용하지 않는다.
* 옵셔널을 컬렉션의 키, 값, 원소나 배열의 원소로 사용하지 않는다.