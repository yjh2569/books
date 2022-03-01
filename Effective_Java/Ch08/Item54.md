# null이 아닌, 빈 컬렉션이나 배열을 반환하라

* null을 반환하는 메서드의 경우 클라이언트에서 null 상황을 처리하는 코드를 추가로 작성해야 한다.

* 빈 컬렉션이나 배열을 반환해도 null을 반환하는 것과 성능 차이가 거의 없고, 새로 할당하지 않고도 반환할 수 있다.

> 빈 컬렉션을 반환하는 올바른 예
```
public List<Cheese> getCheeses() {
    return new ArrayList<>(cheesesInStock);
}
```

* 가능성은 작지만 사용 패턴에 따라 빈 컬렉션 할당이 성능을 눈에 띄게 떨어뜨릴 수 있다.
  * 이 경우에는 매번 똑같은 빈 불변 컬렉션을 반환한다.
  * Collections.emptyList, Collections.emptySet, Collections.emptyMap이 빈 불변 컬렉션을 반환하는 메서드들이다.
  * 단, 이 역시 최적화에 해당하기에 꼭 필요할 때만 사용한다.
  
* 배열을 쓸 때도 null을 반환하지 말고 길이가 0인 배열을 반환한다.

> 빈 배열을 반환하는 방법
```
public Cheese[] getCheeses() {
    return cheesesInStock.toArray(new Cheese[0]);
}
```

* 컬렉션과 마찬가지로 이 방식이 성능이 떨어지는 경우에는 길이 0짜리 배열을 미리 선언해두고 매번 그 배열을 반환한다.
  * 길이 0인 배열은 모두 불변이기 때문이다.
  
> 최적화 - 빈 배열을 매번 새로 할당하지 않도록 했다.
```
private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

public Cheese[] getCheeses() {
    return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
}
```

* 위 코드에서 List.toArray 메서드는 인수로 주어진 배열이 충분히 크면 그 배열 안에 원소를 담아 반환하고, 그렇지 않으면 Cheese[] 타입 배열을 새로 만들어 그 안에 원소를 담아 반환한다.