# Raw Type은 사용하지 말라

* 제네릭과 관련된 용어
  * 제네릭 클래스(인터페이스) : 선언에 타입 매개변수가 쓰인 클래스(인터페이스), 제네릭 클래스와 제네릭 인터페이스를 통틀어 제네릭 타입이라 한다.
  * 매개변수화 타입 : 클래스이름<타입매개변수1(,타입매개변수2,...)> 꼴의 타입
  > List\<String>은 원소의 타입이 String인 리스트를 뜻하는 매개변수화 타입이다.
  * 로 타입(raw type) : 제네릭 타입에서 타입 매개변수를 전혀 사용하지 않은 경우
  > List\<E>의 로 타입은 List
  
* 제네릭을 지원하기 전에는 컬렉션을 raw type으로 선언하면서 다음과 같은 코드를 작성할 위험이 있었다.
  * 특히 아래 코드는 컴파일 중에 오류가 발견되지 않고 실행 중에 발견되기 때문에 원인을 찾기 어려워진다.

```
private final Collection stamps = ...;
// 실수로 동전을 넣는다.
stamps.add(new Coin(...)); // "unchecked call" 경고를 내뱉는다.
for (Iterator i = stamps.iterator(); i.hasNext(); ) {
    Stamp stamp = (Stamp) i.next(); // ClassCastException을 던진다.
    stamp.cancel();
}
```

* 여기에 제네릭을 활용하면 stamps에 Coin이 들어가는 것을 컴파일 시점에 막을 수 있다.

```
private final Collection<Stamp> stamps = ...;
```

* raw type을 쓰는 걸 제한하지는 않았지만 절대 써서는 안 된다.
  * raw type은 제네릭이 안겨주는 안전성과 표현력을 모두 잃게 된다.
  * raw type을 여전히 지원하는 이유는 이전 코드와의 호환성 때문이다.
  
* 임의 객체를 허용하는 매개변수화 타입은 괜찮다.
  * raw type인 List과 같아 보이지만, List\<Object>는 모든 타입을 허용한다는 의사를 컴파일러에 명확히 전달한다.
  * List\<String>은 List를 매개변수로 받는 메서드에 넘길 수 있지만 List\<Object>를 받는 메서드에는 넘길 수 없다. 제네릭의 하위 타입 규칙 때문이다. 즉, List\<String>은 List\<Object>의 하위 타입이 아니다.
  * 따라서 raw type을 사용하면 타입 안전성을 잃게 된다.
  

```
public static void main(String[] args) {
    List<String> strings = new ArrayList<>();
    unsafeAdd(strings, Integer.valueOf(42));
    String s = strings.get(0);
}

private static void unsafeAdd(List list, Object o) {
    list.add(o);
}
```

* 이 코드는 컴파일은 되지만 raw type인 List를 사용해 경고가 발생한다.
  * 이 프로그램을 실행하면 strings.get(0)의 결과를 형변환하려 할 때 ClassCastException을 던진다. Integer를 String으로 변환하려 시도했기 때문이다.
  
* raw type인 List를 매개변수화 타입인 List\<Object>로 바꾸면 컴파일조차 되지 않는다.

```
// 2개의 집합을 받아 공통 원소를 반환하는 메서드를 raw type을 이용해 작성했다.
static int numElementsInCommon(Set s1, Set s2) {
    int result = 0;
    for (Object o1 : s1) {
        if (s2.contains(o1)) {
            result++;
        }
    }
    return result;
}
```

* 이 메서드는 동작은 하지만 raw type을 사용해 안전하지 않다. 따라서 비한정적 와일드카드 타입을 사용한다.
  * 제네릭 타입을 쓰고 싶지만 실제 타입 매개변수가 무엇인지 신경 쓰고 싶지 않다면 '?'을 사용한다.
  * raw type 컬렉션에는 아무 원소나 넣을 수 있어 타입 불변식을 훼손하기 쉬운 반면, Collection\<?>에는 null 외에는 어떤 원소도 넣을 수 없어 안전성이 보장된다.
  
* raw type을 쓰지 말라는 규칙에도 몇 가지 예외가 있다.
  * class 리터럴에는 raw type을 써야 한다.
  > List.class, String[].class, int.class는 허용하나 List\<String>.class, List\<?>.class는 허용하지 않는다.
  * instanceof 연산자는 비한정적 와일드카드 이외의 매개변수화 타입에는 적용할 수 없다. 그리고 raw type이든 비한정적 와일드카드 타입이든 instanceof는 완전히 똑같이 동작하기에, raw type을 쓰는 게 낫다.