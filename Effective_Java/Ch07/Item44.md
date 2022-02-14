# 표준 함수형 인터페이스를 사용하라

* 자바가 람다를 지원하면서 API를 작성하는 방법도 바뀌었다.
  * 상위 클래스의 기본 메서드를 재정의해 원하는 동작을 구현하는 템플릿 메서드 패턴이 줄고, 같은 효과의 함수 객체를 받는 정적 팩토리나 생성자를 제공한다.
  * 이때 함수형 매개변수 타입을 올바르게 선택해야 한다.
  
> LinkedHashMap에서 removeEldestEntry를 재정의하면 캐시로 사용할 수 있다.
```
// put 메서드를 사용할 때 맵에 100개 이상의 원소가 있으면 가장 오래된 원소부터 제거한다.
protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > 100;
}
```

* 위 메서드를 람다를 사용하면 훨씬 잘 구현할 수 있다.
  * 다만, 이를 다시 구현한다면 함수 객체를 받는 정적 팩토리나 생성자를 제공할텐데, 생성자에 넘거는 함수 객체는 인스턴스 메서드가 아니기에 맵은 자기 자신도 함수 객체로 건네줘야 한다.
  * 이를 반영한 함수형 인터페이스는 다음과 같이 선언한다.
  
```
@FunctionalInterface interface EldestEntryRemovalFunction<K, V> {
    boolean remove(Map<K, V> map, Map.Entry<K, V> eldest);
}
```

* 자바 표준 라이브러리에 이미 같은 모양의 인터페이스인 표준 함수형 인터페이스가 있다.
  * java.util.function 패키지에 있다.
  * 유용한 디폴트 메서드를 많이 제공해 다른 코드와의 상호운용성도 좋다.
  
* 대표적인 표준 함수형 인터페이스
  * UnaryOperator : 인수가 1개인 Operator 인터페이스, 반환값과 인수가 타입이 같은 함수 ex) String::toLowerCase
  * BinaryOperator : 인수가 2개인 Operator 인터페이스, 반환값과 인수가 타입이 같은 함수 ex) BigInteger::add
  * Predicate : 인수 하나를 받아 boolean을 반환하는 함수 ex) Collection::isEmpty
  * Function : 인수와 반환 타입이 다른 함수 ex) Arrays::asList
  * Supplier : 인수를 받지 않고 값을 반환하는 함수 ex) Instant::now
  * Consumer : 인수를 하나 받고 반환값은 없는 함수 ex) System.out::println
  
* 기본 인터페이스는 기본 타입인 int, long, double용으로 각 3개씩 변형이 생겨난다.
> int를 받는 PRedicate는 IntPredicate가 되고 long을 받아 long을 반환하는 BinaryOperator는 LongBinaryOperator가 된다.

* Funciton 인터페이스에는 기본 타입을 반환하는 변형이 총 9개가 더 있다.
  * 입력과 결과 타입이 모두 기본 타입이면 접두어로 SrcToResult를 사용한다.
  > long을 받아 int를 반환하면 LongToIntFunction이 된다.
  * 입력이 객체 참조이고 결과가 int, long, double인 경우 입력을 매개변수화하고 접두어로 ToResult를 사용한다.
  > ToLongFunction<int[]>은 int[] 인수를 받아 long을 반환한다.
  
* 기본 함수형 인터페이스 중 3개에는 인수를 2개씩 받는 변형으로 BiPredicate<T, U>, BiFunction<T, U, R>, BiConsumer<T, U>가 있다.
  * BiFunction에는 다시 기본 타입을 반환하는 세 변형 ToIntBiFunction<T, U>, ToLongBiFunction<T, U>, ToDoubleBiFunction<T, U>이 있다.
  * Consumer에도 객체 참조와 기본 타입 하나, 즉 인수를 2개 받는 변형인 ObjDoubleConsumer\<T>, ObjIntConsumer\<T>, ObjLongConsumer\<T>이 있다.
  
* BooleanSupplier 인터페이스는 boolean을 반환하도록 한 Supplier의 변형이다.

* 표준 함수형 인터페이스 대부분은 기본 타입만 지원한다. 그렇다고 기본 함수형 인터페이스에 박싱된 기본 타입을 넣어 사용하지 않는다.

* 표준 함수형 인터페이스를 사용하지 않고 직접 작성해야 하는 경우
  * 표준 인터페이스 중 필요한 용도에 맞는 게 없는 경우
  > 매개변수 3개를 받는 Predicate나 검사 예외를 던지는 경우
  * 구조적으로 똑같은 표준 함수형 인터페이스가 있음에도 독자적인 인터페이스로 작성해야 하는 경우 : 전용 함수형 인터페이스를 구현한다.
  
* 전용 함수형 인터페이스를 구현하는 경우
  * 자주 쓰이고, 이름 자체가 용도를 명확히 설명할 경우
  * 반드시 따라야 하는 규약이 있는 경우
  * 유용한 디폴트 메서드를 제공할 수 있는 경우
> Comparator\<T> 인터페이스는 구조적으로 ToIntBiFunction<T, U>와 동일하지만 위와 같은 이유로 독자적인 인터페이스로 살아남았다.

* 직접 만든 함수형 인터페이스에는 @FunctionalInterface 애너테이션을 추가한다.
  * 해당 클래스의 코드나 설명 문서를 읽을 이에게 그 인터페이스가 람다용으로 설계된 것임을 알려준다.
  * 해당 인터페이스가 추상 메서드를 오직 하나만 가지고 있어야 컴파일되게 해준다.
  * 유지보수 과정에서 누군가 실수로 메서드를 추가하지 못하게 막아준다.
  
* 함수형 인터페이스를 API에서 사용할 때 서로 다른 함수형 인터페이스를 같은 위치의 인수로 받는 메서드들을 다중정의해서는 안 된다.