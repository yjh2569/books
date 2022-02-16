# 스트림에서는 부작용 없는 함수를 사용하라

* 스트림 패러다임의 핵심은 계산을 일련의 변환(transformation)으로 재구성하는 부분이다.
  * 이때 각 변환 단계는 가능한 한 이전 단계의 결과를 받아 처리하는 순수 함수여야 한다.
  * 순수 함수 : 입력만이 결과에 영향을 주는 함수로 다른 가변 상태를 참조하지 않고, 함수 스스로도 다른 상태를 변경하지 않는다.
  * 즉, 스트림 연산에 건네는 함수 객체는 모두 side effect가 없어야 한다.
  
> 스트림 패러다임을 잘못 사용한 예시
```
Map<String, Long> freq = new HashMap<>();
try (Stream<String> words = new Scanner(file).tokens()) {
    words.forEach(word -> {
        freq.merge(word.toLowerCase(), 1L, Long::sum);
    })
} 
```

* 위 코드는 스트림 코드를 가장한 반복적 코드다.
  * 모든 작업이 종단 연산인 forEach에서 일어나는데, 외부 상태(freq)를 수정하는 람다를 실행하면서 문제가 생긴다.

> 스트림을 제대로 활용한 예시
```
Map<String, Long> freq;
try (Stream<String> words = new Scanner(file).tokens()) {
    freq = words.collect(groupingBy(String::toLowerCase, counting()));
} 
```

* forEach 연산은 스트림 계산 결과를 보고할 때만 사용한다.

* 위 코드는 수집기(collector)를 사용한다. Collector 인터페이스는 축소 전략을 캡슐화한 블랙박스 객체라 할 수 있다.
  * 축소 : 스트림의 원소들을 객체 하나에 취합한다.
  * 수집기에는 toList(), toSet(), toCollection(collectionFactory)가 있다.
  
> 수집기 사용 예시
```
List<String> topTen = freq.keySet().stream()
    // comparing 메서드는 키 추출 함수를 받아 비교자를 생성한다. 
    // 여기서는 키 추출함수로 쓰인 freq::get은 입력받은 단어(키)를 빈도표에서 찾아 그 빈도를 반환한다. 
    .sorted(comparing(freq::get).reversed())
    .limit(10)
    .collect(toList());
```

* 가장 간단한 맵 수집기는 toMap(keyMapper, valueMapper)로, 스트림 원소를 키에 매핑하는 함수와 값에 매핑하는 함수를 인수로 받는다.
  * 스트림의 각 원소가 고유한 키에 매핑되어 있을 때 적합하다.
```
private static final Map<String, Operation> stringToEnum = Stream.of(values()).collect(toMap(Object::toString, e -> e));
```

* 더 복잡한 형태의 toMap은 스트림 원소 다수가 같은 키를 사용할 때 발생하는 충돌을 다루는 전략을 제공한다.
  * toMap에 키 매퍼와 값 매퍼에 더해 병합(merge) 함수(형태는 BinaryOperator<U>)를 추가해 같은 키를 공유하는 값들은 이 병합 함수를 사용해 기존 값에 합쳐지게 한다.
  * 네 번째 인수로 맵 팩토리를 받아 EnumMap이나 TreeMap처럼 원하는 특정 맵 구현체를 직접 지정할 수 있다.
  
> 더 복잡한 toMap 예시
```
Map<Artist, Album> topHits = albums.collect(toMap(Album::artist, a->a, maxBy(comparing(Album::sales))));
toMap(keyMapper, valueMapper, (oldVal, newVal) -> newVal) // 마지막에 쓴 값을 취하는 수집기
```

* groupingBy는 입력으로 분류 함수를 받아 원소들을 카테고리별로 모아 놓은 맵을 담은 수집기를 반환한다.
  * 분류 함수는 입력받은 원소가 속하는 카테고리를 반환한다. 그리고 이 카테고리가 해당 원소의 맵 키로 쓰인다.
  * 분류 함수 하나만 인수로 받는 경우 반환된 맵에 담긴 각각의 값은 해당 카테고리에 속하는 원소들을 모두 담은 리스트다.
  * groupingBy가 반환하는 수집기가 리스트 외의 값을 갖는 맵을 생성하게 하려면 분류 함수와 함께 다운스트림(downstream) 수집기도 명시해야 한다.
  * 다운스트림 수집기는 해당 카테고리의 모든 원소를 담은 스트림으로부터 값을 생성한다. 
  > toSet()을 사용하면 리스트가 아닌 집합을 값으로 갖는 맵을 만든다. counting()을 사용해면 해당 카테고리에 속하는 원소의 개수를 값으로 갖는 맵을 만든다.
  * toMap과 마찬가지로 맵 팩토리 지정 역시 가능하다. 단, mapFactory 매개변수가 downStream 매개변수보다 앞에 놓인다.
  * partitioningBy는 분류 함수 자리에 predicate을 받아 키가 Boolean인 맵을 반환한다.
  
* Collectors의 기타 메서드
  * minBy와 maxBy는 인수로 받은 비교자를 이용해 스트림에서 값이 가장 작은 혹은 가장 큰 원소를 찾아 반환한다.
  * joining은 원소들을 연결하는 수집기를 반환한다. CharSequence 타입의 구분문자(delimiter)를 매개변수로 받을 수도 있다. 또한 접두사와 접미사를 받을 수도 있다.