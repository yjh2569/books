# 박싱된 기본 타입보다는 기본 타입을 사용하라

* 기본 타입과 박싱된 기본 타입의 주된 차이
  * 기본 타입은 값만 가지고 있으나, 박싱된 기본 타입을 값에 더해 식별성(identity)이란 속성을 갖는다. 즉, 박싱된 기본 타입의 두 인스턴스는 값이 같아도 서로 다르다고 식별될 수 있다.
  * 기본 타입의 값은 언제나 유효하지만 박싱된 기본 타입은 유효하지 않은 값, 즉 null을 가질 수 있다.
  * 기본 타입이 박싱된 기본 타입보다 시간과 메모리 사용면에서 더 효율적이다.
  
```
Comparator<Integer> naturalOrder = (i, j) -> (i < j) ? -1 : (i == j ? 0 : 1);
```

* 위 비교자는 문제가 없어 보이지만, naturalOrder.compare(new Integer(42), new Integer(42))의 값이 0이 아닌 1로 출력된다.
  * 이는 두 번째 비교(i == j)에서 기본 타입 값으로 변환되지 않고 객체 참조의 식별성을 검사해 비교 결과가 false가 되어 발생하는 문제다.
  * 즉, 박싱된 기본 타입에 == 연산자를 사용하면 오류가 발생한다.
  
* 이와 같이 기본 타입을 다루는 비교자가 필요하다면 Comparator.naturalOrder()를 사용한다.
  * 비교자를 직접 만들면 비교자 생성 메서드나 기본 타입을 받는 정적 compare 메서드를 사용해야 한다.
  * 그렇더라도 이 문제를 고치려면 지역변수 2개를 두어 각각 박싱된 Integer 매개변수의 값을 기본 타입 정수로 저장한 다음, 모든 비교를 이 기본 타입 변수로 수행해야 한다.
  
> 문제를 수정한 비교자
```
Comparator<Integer> naturalOrder = (iBoxed, jBoxed) -> {
    int i = iBoxed, j = jBoxed; // 오토박싱
    return i < j ? -1 : (i == j ? 0 : 1);
}
```

* 기본 타입을 선언할 때 박싱된 기본 타입으로 선언하면 초깃값은 0이 아닌 null이 됨에 유의해야 한다.

* 박싱된 기본 타입과 기본 타입 연산 시 박싱과 언박싱이 일어나는데, 이는 성능에 영향을 끼칠 수 있다.

* 박싱된 기본 타입을 쓰는 경우
  * 컬렉션의 원소, 키, 값
  * 매개변수화 타입이나 매개변수화 메서드의 타입 매개변수
  * 리플렉션을 통해 메서드를 호출할 경우