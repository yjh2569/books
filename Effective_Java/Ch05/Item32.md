# 제네릭과 가변인수를 함께 쓸 때는 신중하라

* 가변인수는 메서드에 넘기는 인수의 개수를 클라이언트가 조절할 수 있게 해주지만, 구현 방식에 허점이 있다.
  * 가변인수 메서드를 호출하면 가변인수를 담기 위한 배여이 자동으로 하나 만들어지는데, 이 배열을 클라이언트에 노출하는 문제가 있다. 이로 인해 varargs 매개변수에 제네릭이나 매개변수화 타입이 포함되면 컴파일 경고가 발생한다.
  * 메서드를 선언할 때 실체화 불가 타입으로 varargs 매개변수를 선언하면 컴파일러가 경고를 보내기 때문이다.

```
static void dangerous(List<String>... stringLists) {
    List<Integer> intList = List.of(42);
    Object[] objects = stringLists;
    object[0] = intList; // 힙 오염 발생
    String s = stringLists[0].get(0); // ClassCastException
}
```

* 위와 같은 예시처럼 타입 안전성이 깨질 수 있어 제네릭 varargs 배열 매개변수에 값을 저장하는 것은 안전하지 않다.

* 하지만 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 메서드가 실무에서 매우 유용하기 때문에 제네릭 varargs 매개변수를 받는 메서드에 대해 경고만 해 준다.

* @SafeVarargs 애너테이션은 메서드 작성자가 그 메서드가 타입 안전함을 보장하는 장치다.
  * 자바 7에서 @SafeVarargs 애너테이션이 추가되어 제네릭 가변인수 메서드 작성자가 클라이언트 측에서 발생하는 경고를 숨길 수 있게 되었다.
  
* 제네릭 가변인수 메서드의 언전 여부 판단
  * varargs 매개변수를 담는 제네릭 배열에 아무것도 저장하지 않는다(그 매개변수들을 덮어쓰지 않는다).
  * 배열의 참조가 밖으로 노출되지 않는다(신뢰할 수 없는 코드가 배열에 접근할 수 없다). 
  * 즉, varargs 매개변수 배열이 호출자로부터 그 메서드로 순수하게 인수들을 전달하는 일만 한다면 안전하다.
  
> 자신의 제네릭 매개변수 배열의 참조를 노출하므로 안전하지 않다.
```
static <T> T[] toArray(T... args) {
    return args;
}
```

> 제네릭 varargs 매개변수를 안전하게 사용하는 메서드
```
@SafeVarargs
static <T> List<T> flatten(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
        result.addAll(list);
    }
    return result;
}
```

* @SafeVarargs 애너테이션은 재정의할 수 없는 메서드에만 달아야 한다. 재정의한 메서드도 안전할지는 보장할 수 없기 때문이다.
  * 자바 8에서 이 애너테이션은 오직 정적 메서드와 final 인스턴스 메서드에만 붙일 수 있다.
  * 자바 9부터는 private 인스턴스 메서드에도 허용된다.
  
* varargs 매개변수를 List 매개변수로 바꿔서 해결할 수도 있다.

> 제네릭 varargs 매개변수를 List로 대체한 예
```
static <T> List<T> flatten(List<List<? extends T>> lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
        result.addAll(list);
    }
    return result;
}
```

* 정적 팩터리 메서드인 List.of를 활용하면 이 메서드에 임의 개수의 인수를 넘길 수 있다.
  * List.of에도 @SafeVarargs 애너테이션이 달려 있기 때문이다.
  * 컴파일러가 이 메서드의 타입 안전성을 검증할 수 있고, @SafeVarargs 애너테이션을 직접 달지 않아도 되며, 실수로 안전한다고 판단할 걱정도 없다.
  * 클라이언트 코드가 살짝 지저분해지고 속도가 조금 느려질 수 있다.
  * varargs 메서드를 안전하게 작성하는 게 불가능한 상황에서도 쓸 수 있다.
  
```
audience = flatten(List.of(friends, romans, countrymen));
```