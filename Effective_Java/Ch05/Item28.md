# 배열보다는 리스트를 사용하라

* 배열은 공변(covariant)인 반면, 제네릭은 불공변(invariant)이다.
  * Sub가 Super의 하위 타입이면 배열 Sub[]는 배열 Super[]의 하위 타입이 된다.
  * 서로 다른 타입 Type1과 Type2가 있을 때, List<Type1>은 List<Type2>의 하위 타입도 아니고 상위 타입도 아니다.
  
```
// 런타임에 실패한다.
Object[] objectArray = new Long[1];
objectArray[0] = "타입이 달라 넣을 수 없다."; // ArrayStoreException

// 컴파일되지 않는다.
List<Object> ol = new ArrayList<Long>(); // 호환되지 않는 타입
ol.add("타입이 달라 넣을 수 없다.");
```

* 배열은 실체화(reify)된다.
  * 배열은 런타임에도 자신이 담기로 한 원소의 타입을 인지하고 확인한다.
  * 제네릭은 원소 타입을 컴파일타임에만 검사하고 런타임에는 타입 정보가 소거된다.

* 배열은 제네릭 타입, 매개변수화 타입, 타입 매개변수로 사용할 수 없다.
> new List\<E>[], new List\<String>[], new E[] 식으로 작성하면 컴파일할 때 제네릭 배열 생성 오류를 일으킨다.
  * 타입 안전하지 않기 때문이다. 이를 허용하면 컴파일러가 자동 생성한 형변환 코드에서 런타임에 ClassCastException이 발생할 수 있다.
  
> 제네릭 배열 생성을 혀용하지 않는 이유
```
List<String>[] stringLists = new List<String>[1]; // (1)
List<Integer> intList = List.of(42); // (2)
Object[] objects = stringLists; // (3)
object[0] = intList; // (4)
String s = stringLists[0].get(0); // (5)
```

* 위 코드에서 (1)이 허용되면 발생하는 상황
  * (3)에서 List<String>의 배열을 Object 배열에 할당하는 것은 배열이 공변이기에 가능하다.
  * (4)에서 List\<Integer>의 인스턴스를 Object 배열의 첫 원소로 저장하는 것은 제네릭이 소거 방식으로 구현되었기에 가능하다. 즉, 런타임에는 List\<Integer> 인스턴스의 타입이 단순히 List가 되고, List\<Integer>[] 인스턴스의 타입은 List[]가 된다.
  * (5)에서 배열의 처음 리스트에서 첫 원소를 꺼낼 때 컴파일러는 꺼낸 원소를 자동으로 String으로 형 변환하는데 이 원소는 Integer이므로 런타임에 ClassCastException이 발생한다.
  * 따라서 (1)에서 제네릭 배열이 생성되지 않도록 컴파일 오류를 내야 한다.
  
* E, List\<E>, List\<String> 같은 타입을 실체화 불가 타입이라 한다.
  * 실체화되지 않아서 런타임에는 컴파일타임보다 타입 정보를 적게 가진다.
  
* 배열을 제네릭으로 만들 수 없기 때문에 생기는 불편함
  * 제네릭 컬렉션에서는 자신의 원소 타입을 담은 배열을 반환하는 게 보통 불가능하다.
  * 제네릭 타입과 가변인수 메서드를 함께 쓰면 해석하기 어려운 경고 메시지를 받게 된다. 가변인수 메서드를 호출할 때 가변인수 매개변수를 담을 배열을 생성하는데, 그 배열의 원소가 실체화 불가 타입이기 때문이다.
  * 따라서 배열인 E[] 대신 List\<E>를 사용하면 위와 같은 문제를 해결할 수 있다.

> Chooser 클래스
```
public class Chooser {
    private final Object[] choiceArray;
    
    public Chooser(Collection choices) {
        choiceArray = choices.toArray();
    }
    
    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```

* 위 클래스를 사용하려면 choose 메서드를 호출할 때마다 반환된 Object를 원하는 타입으로 형변환해야 한다.
  * 혹시나 타입이 다른 원소가 들어 있었다면 런타임에 형변환 오류가 날 것이다.
  * 그렇다고 배열의 원소 타입을 제네릭으로 선언해서는 안 되므로, 배열 대신 리스트를 사용한다.
  
> 리스트 기반 Chooser
```
public class Chooser<T> {
    private final List<T> choiceList;
    
    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choices);
    }
    
    public T choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }
}
```