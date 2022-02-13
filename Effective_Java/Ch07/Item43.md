# 람다보다는 메서드 참조를 사용하라

> 임의의 키와 Integer 값의 매핑을 관리하는 프로그램의 일부
```
map.merge(key, 1, (count, incr) -> count + incr);
```

* Map의 merge 메서드는 키, 값, 함수를 인수로 받아 주어진 키가 아직 없으면 주어진 {키, 값} 쌍을 그대로 저장하고, 키가 있으면 함수를 현재 값과 주어진 값에 적용한 다음 그 결과로 현재 값을 덮어쓴다.

* 위 코드는 함수 인수를 건네줄 때 람다를 사용했으나, 람다 대신 메서드 참조를 사용해 매개변수를 사용하지 않고 함수를 전달할 수 있다.

```
// sum은 Integer의 정적 메서드로 위 람다와 기능이 같다.
map.merge(key, 1, Integer::sum);
```

* 메서드 참조를 사용하면 더 짧고 간결하게 표현할 수 있어 람다의 좋은 대안이 될 수 있다.
  * 어떤 람다에서는 매개변수의 이름 자체가 프로그래머에게 좋은 가이드가 되기도 하기에 람다를 사용하기도 한다.
  * 메서드와 람다가 같은 클래스에 있는 경우에는 람다를 사용하는 편이 낫다.
  
> 메서드 참조보다 람다가 더 간결한 경우
```
// 다음 코드가 GoshThisClassNameIsHumonogous 클래스 안에 있는 경우
service.execute(GoshThisClassNameIsHumonogous::action); // 메서드 참조
service.execute(() -> action()); // 람다

// java.util.function 패키지의 Function.identity()보다 x -> x가 더 짧고 명확하다.
```

* 메서드 참조의 유형
  * 정적 메서드를 가리키는 메서드 참조
  * 수신 객체(receiving object; 참조 대상 인스턴스)를 특정하는 한정적 인스턴스 메서드 참조
  * 수신 객체를 특정하지 않는 비한정적 인스턴스 메서드 참조 : 주로 스트림 파이프라인에서의 매핑과 필터 함수에 쓰인다.
  * 클래스 생성자를 가리키는 메서드 참조
  * 배열 생성자를 가리키는 메서드 참조
  
> 메서드 참조 유형 예시
```
// 정적
Integer::parseInt
// 같은 기능을 하는 람다
str -> Integer.parseInt(str)

// 한정적 (인스턴스)
Instance.now()::isAfter
// 같은 기능을 하는 람다
Instant then = Instant.now();
t -> then.isAfter(t)

// 비한정적 (인스턴스)
String::toLowerCase
// 같은 기능을 하는 람다
str -> str.toLowercase()

// 클래스 생성자
TreeMap<K, V>::new
// 같은 기능을 하는 람다
() -> new TreeMap<K, V>()

// 배열 생성자
int[]::new
// 같은 기능을 하는 람다
len -> new int[len]
```