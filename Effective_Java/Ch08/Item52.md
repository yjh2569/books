# 다중정의는 신중히 사용하라

> 컬렉션 분류기 예시
```
public class CollectionClassifier {
    public static String classify(Set<?> s) {
        return "집합";
    }
    
    public static String classify(List<?> lst) {
        return "리스트";
    }
    
    public static String classify(Collection<?> c) {
        return "그 외";
    }
    
    public static void main(String[] args) {
        Collection<?>[] collections = {
            new HashSet<String>(),
            new ArrayList<BigInteger>(),
            new HashMap<String, String>().values()
        };
        
        for (Collection<?> c : collections) System.out.println(classify(c));
    }
}
```

* 위 예시에서 "집합", "리스트", "그 외"가 출력될 것으로 예상했으나, 실제로는 "그 외"만 세 번 연달아 출력한다.
  * 이는 다중정의(overloading)된 세 classify 중 어느 메서드를 호출할지가 컴파일타임에 정해지기 때문이다.
  * 컴파일타임에는 for 문 안의 c는 항상 Collection<?> 타입이다. 런타임에는 타입이 매번 달라지지만, 호출할 메서드를 선택하는 데는 영향을 주지 못한다.
  
* 재정의한 메서드는 동적으로 선택되고, 다중정의한 메서드는 정적으로 선택된다.
  * 메서드를 재정의했다면 해당 객체의 런타임 타입이 어떤 메서드를 호출할지의 기준이 된다.
  * 메서드 재정의 후 하위 클래스의 인스턴스에서 그 메서드를 호출하면, 컴파일타임에 그 인스턴스의 타입이 무엇이었냐는 상관없이 재정의한 메서드가 실행된다.
  * 반면 다중정의된 메서드 사이에서는 객체의 런타임 타입은 전혀 중요치 않다. 선택은 컴파일타임에, 오직 매개변수의 컴파일타임 타입에 의해 이뤄진다.
  
* 위 예시의 문제는 모든 classify 메서드를 하나로 합친 후 instanceof로 명시적으로 검사하면 해결할 수 있다.

```
public static String classify(Collection<?> c) {
    return c instanceof Set ? "집합": c instanceof List ? "리스트" : "그 외";
}
```

* 위 상황처럼 다중정의는 어떤 메서드가 호출될지를 알 수 없는 경우가 발생한다. 따라서 다중정의가 혼동을 일으키는 상황을 피해야 한다.
  * 안전하고 보수적으로 가려면 매개변수 수가 같은 다중정의는 만들지 않는다.
  * 특히 가변인수를 사용하는 메서드라면 다중정의는 아예 하지 않는 게 좋다.
  * 다중정의하는 대신 메서드 이름을 다르게 지어주는 방법도 있다.
  
> ObjectOutputStream 클래스의 경우 write 메서드와 read 메서드는 모든 기본 타입과 일부 참조 타입용 변형을 가지고 있다. 그러나 다중정의가 아닌, writeBoolean, writeInt, writeLong과 같이 모든 메서드에 다른 이름을 지어주는 길을 택했다.

* 매개변수 수가 같은 다중정의 메서드가 많더라도, 그중 어느 것이 주어진 매개변수 집합을 처리할지가 명확히 구분된다면 헷갈릴 일은 없을 것이다.
  * 즉, 매개변수 중 하나 이상이 근본적으로 다르면(두 타입의 값을 서로 어느 쪽으로든 형변환할 수 없으면) 헷갈릴 일이 없다.
  
> List의 경우 자바 5 이후 오토박싱의 도입으로 인해 remove(0 또는 양수)를 호출하면 remove(int index)를 호출하는데, 이 대신 List 내 존재하는 정수를 제거하려면 해당 정수를 Integer로 형변환해야 한다.

* 메서드를 다중정의할 때, 서로 다른 함수형 인터페이스라도 같은 위치의 인수로 받아서는 안 된다.