# 한정적 와일드카드를 사용해 API 유연성을 높이라

* 매개변수화 타입은 불공변(invariant)이지만 때론 불공변 방식보다 유연한 것이 필요하기도 하다.

> Stack의 pushAll 메서드
```
public void pushAll(Iterable<E> src) {
    for (E e : src) {
        push(e);
    }
}
```

* 위 메서드는 Iterable src의 원소 타입이 스택의 원소 타입과 일치하면 잘 작동하지만, Stack<Number>로 선언한 후 pushAll(intVal)을 호출하면 오류 메시지가 뜬다.(intVal은 Integer 타입)
  * 매개변수화 타입이 불공변이기 때문이다.
  
* 위 문제를 해결하기 위해 한정적 와일드카드 타입이라는 특별한 매개변수화 타입을 지원한다.
  * pushAll의 입력 매개변수 타입은 E의 Iterable이 아니라 E의 하위 타입의 Iterable이어야 하므로 와일드카드 타입 Iterable\<? extends E>로 바꾼다.
  
```
public void pushAll(Iterable<? extends E> src) {
    for (E e : src) {
        push(e);
    }
}
```

* Stack의 popAll 메서드의 경우 Stack 안의 모든 원소를 주어진 컬렉션으로 옮겨 담는데, 와일드카드 타입을 사용하지 않으면 상위 타입의 컬렉션에 담을 수 없기 때문에 이를 해결하기 위해 Collection\<? super E>와 같이 한정적 와일드카드 타입을 사용한다.

> Stack의 popAll 메서드
```
public void popAll(Collection<? super E> dst) {
    while (!isEmpty()) dst.add(pop());
}
```

* 위 예시처럼 유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용한다.
  * 단, 입력 매개변수가 생산자와 소비자 역할을 동시에 한다면 타입을 정확히 지정해야 하므로 와일드카드 타입을 쓰지 않는다.
  * 와일드카드 타입 사용 공식 - PECS : producer-extends, consumer-super
  
* 반환 타입에는 한정적 와일드카드 타입을 사용하면 안 된다. 클라이언트 코드에서도 와일드카드 타입을 써야 하기 때문이다.

> 와일드카드 타입을 사용한 max 메시드 선언부
```
public static <E extends Comparable<? super E>> E max(List<? extends E> list)
```

* 입력 매개변수에서는 E 인스턴스를 생산하므로 원래의 List\<E>를 List\<? extends E>로 수정했다.
* 타입 매개변수 E의 경우 원래는 Comparable\<E>를 확장한다고 정의했으나, Comparable은 언제나 소비자이므로 Comparable\<? super E>를 사용하는 편이 낫다.

* 타입 매개변수와 와일드카드에는 공통되는 부분이 있어서 메서드를 정의할 때 둘 중 어느 것을 사용해도 괜찮을 때가 많다.

> 두 인덱스의 아이템들을 교환하는 정적 메서드
```
public static <E> void swap(List<E> list, int i, int j); // 비한정적 타입 매개변수 사용
public static void swap(List<?> list, int i , int j); // 비한정적 와일드카드 사용
```

* 메서드 선언에 타입 매개변수가 한 번만 나오면 와일드카드로 대체한다.
  * 비한정적 타입 매개변수는 비한정적 와일드카드로, 한정적 타입 매개변수는 한정적 와일드카드로 바꾼다.

* 하지만 두 번쨰 swap 선언의 경우 다음과 같이 구현한 코드가 컴파일되지 않는다.

```
public static void swap(List<?> list, int i , int j) {
    list.set(i, list.set(j, list.get(i)));
}
```

* 이는 리스트의 타입이 List\<?>인데, List\<?>에는 null 외에 어떤 값도 넣을 수 없기 때문이다.
  * 이를 해결하기 위해 와일드카드 타입의 실제 타입을 알려주는 메서드를 private 도우미 메서드로 따로 작성해 활용한다. 실제 타입을 알아내려면 이 도우미 메서드는 제네릭 메서드여야 한다.

```
public static void swap(List<?>, int i, int j) {
    swqpHelper(list, i, j);
}

// 와일드카드 타입을 실제 타입으로 바꿔주는 private 도우미 메서드
private static <E> void swapHelper(List<E> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));
}
```