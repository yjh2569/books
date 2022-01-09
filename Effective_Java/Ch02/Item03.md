# private 생성자나 열거 타입으로 싱글턴임을 보증하라

* 싱글턴(singleton) : 인스턴스를 오직 하나만 생성할 수 있는 클래스. 함수와 같은 무상태(stateless) 객체나 설계상 유일해야 하는 시스템 컴포넌트가 이에 해당한다.
* 싱글턴은 세 가지 생성 방법이 있다. 그 중 두 방법은 생성자를 private으로 감춰두고, 유일한 인스턴스에 접근할 수 있는 수단으로 public static 멤버를 하나 마련해둔다.

1. public static 멤버가 final 필드인 방식

```
public class Example {
    public static final Example INSTANCE = new Example();
    private Example() {...}
    
    public void someMethod() {...}
}
```

* private 생성자는 public static final 필드인 Example.INSTANCE를 초기화할 때 딱 한 번만 호출된다. public이나 protected 생성자가 없기 때문에 생성된 인스턴스가 전체 시스템에서 하나뿐임이 보장된다.
* 해당 클래스가 싱글턴임이 API에 명백히 드러나고, 코드가 간결하다.
* 다만 클라이언트가 리플렉션 API인 AccessibleObject.setAccessible을 사용하면 private 생성자를 호출할 수 있기에 이를 방지하기 위해 두 번쨰 객체가 생성되려 할 때 예외를 던지게 한다.

2. public static 멤버가 정적 팩토리 메서드인 방식

```
pubilc class Example {
    private static final Example INSTANCE = new Example();
    private Example() {...}
    public static Example getInstance() { return INSTANCE; }
    
    public void someMethod() {...}
}
```

* Example.getInstance는 항상 같은 객체의 참조를 반환하기 때문에 새 인스턴스가 만들어지지 않는다. (다만, 리플렉션을 통한 예외는 똑같이 적용된다.)
* API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있다. 유일한 인스턴스를 반환하던 팩토리 메서드를 (예컨대) 호출하는 스레드별로 다른 인스턴스를 넘겨주는 메서드로 바꿀 수 있다.
* 정적 팩토리를 제네릭 싱글턴 팩토리로 만들 수 있다.
* 정적 팩토리의 메서드 참조를 공급자(supplier)로 사용할 수 있다.

* public 필드 방식의 경우 직렬화할 때 모든 인스턴스 필드를 일시적(transient)이라고 선언하고 readResolve 메서드를 제공해 역직렬화시 새 인스턴스가 만들어지는 것을 막아야 한다.

3. 원소가 하나인 열거 타입을 선언하는 방식

```
public Enum Example {
    INSTANCE;
    
    public void someMethod() {...}
}
```

* 더 간결하고, 추가 노력 없이 직렬화할 수 있으며 복잡한 직렬화 상황이나 리플렉션 공격에서도 새 인스턴스가 생기는 일을 완벽히 막아준다.
* 대부분의 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법이다.
* 만들려는 싱글턴이 Enum외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없다.