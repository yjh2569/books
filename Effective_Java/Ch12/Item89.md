# 인스턴스 수를 통제해야 한다면 readResolve보다는 열거 타입을 사용하라

* 싱글턴 패턴을 적용한 클래스에 implements Serializable을 추가하면 더 이상 싱글턴이 아니게 된다.
  * 기본 직렬화를 쓰지 않더라도, 그리고 명시적인 readObject를 제공하더라도 readObject를 사용하면 이 클래스가 초기화될 때 만들어진 인스턴스와는 별개인 인스턴스가 반환된다.
  
* readResolve 기능을 이용하면 readObject가 만들어낸 인스턴스를 다른 것으로 대체할 수 있다.
  * 역직렬화한 객체의 클래스가 readResolve 메서드를 적절히 정의해뒀다면, 역직렬화 후 새로 생성된 객체를 인수로 이 메서드가 호출되고, 이 메서드가 반환한 객체 참조가 새로 생성된 객체를 대신해 반환한다.
  * 대부분의 경우 이때 새로 생성된 객체의 참조는 유지하지 않으므로 바로 가비지 컬렉션 대상이 된다.
  
> readResolve 메서드 추가를 통한 싱글턴 속성 유지
```
public class Elvis implements Serializable {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() { ... }
    
    public void leaveTheBuilding() { ... }
    
    // 인스턴스 통제를 위한 readResolve
    private Object readResolve() {
        // 진짜 Elvis를 반환하고, 가짜 Elvis는 가비지 컬렉터에 맡긴다. 
        return INSTANCE;
    }
}
```

* 위 메서드는 역직렬화한 객체는 무시하고 클래스 초기화 때 만들어진 Elvis 인스턴스를 반환한다.
  * 따라서 Elvis 인스턴스의 직렬화 형태는 아무런 실 데이터를 가질 이유가 없어 모든 인스턴스 필드를 transient로 선언해야 한다.
  * readResolve를 인스턴스 통제 목적으로 사용한다면 객체 참조 타입 인스턴스 필드는 모두 transient로 선언해야 한다.
  * 그렇지 않으면 readResolve 메서드가 수행되기 전에 역직렬화된 객체의 참조를 공격할 여지가 남는다.
    * transient가 아닌 참조 필드를 가지고 있다면 그 필드의 내용은 readResolve 메서드가 실행되기 전에 역직렬화된다.
    * 잘 조작된 스트림을 써서 해당 참조 필드의 내용이 역직렬화되는 시점에 그 역직렬화된 인스턴스의 참조를 훔쳐올 수 있다.
    
* readResolve 메서드가 수행되기 전 역직렬화된 객체의 참조를 공격하는 구체적인 방식
  * readResolve 메서드와 인스턴스 필드 하나를 포함한 클래스를 작성한다.
  * 인스턴스 필드는 클래스가 숨길 직렬화된 싱글턴을 참조하는 역할을 한다.
  * 직렬화된 스트림에서 싱글턴의 비휘발성 필드를 이 클래스의 인스턴스로 교체한다.
  * 그러면 싱글턴은 클래스를 참조하고 클래스는 싱글턴을 참조하는 순환고리가 만들어진다.
  * 싱글턴이 클래스를 포함하므로 싱글턴이 역직렬화될 때 클래스의 readResolve 메서드가 먼저 호출된다.
  * 그 결과, 클래스의 readResolve 메서드가 수행될 때 클래스의 인스턴스 필드에는 역직렬화 도중인 싱글턴의 참조가 담겨 있게 된다.
  * 클래스의 readResolve 메서드는 이 인스턴스 필드가 참조한 값을 정적 필드로 복사해 readResolve가 끝난 후에도 참조할 수 있도록 한다.
  * 그런 다음 이 메서드는 클래스가 숨긴 transient가 아닌 필드의 원래 타입에 맞는 값을 반환한다.
    * 이 과정을 생략하면 직렬화 시스템이 클래스의 참조를 이 필드에 저장하려 할 때 VM이 ClassCastException을 던진다.
    
```
public class Elvis implements Serializable {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() { }
    
    private String[] favoriteSongs = { "Hound Dog", "Heartbreak Hotel" };
    public void printFavorites() {
        System.out.println(Arrays.toString(favoriteSongs));
    }
    
    private Object readResolve() {
        return INSTANCE;
    }
}

public class ElvisStealer implements Serializable {
    static Elvis impersonator;
    private Elvis payload;
    
    private Object readResolve() {
        // resolve되기 전의 Elvis 인스턴스의 참조를 저장한다.
        impersonator = payload;
        
        // favoriteSongs 필드에 맞는 타입의 객체를 반환한다.
        return new Stirng[] { "A Fool Such as I" };
    }
    
    private static final long serialVersionUID = 0;
}

public class ElvisImpersonator {
    // 진짜 Elvis 인스턴스로는 만들어질 수 없는 바이트 스트림
    private static final byte[] serializedForm = { ... };
    
    public static void main(String[] args) {
        // ElvisStealer.impersonator를 초기화한 다음,
        // 진짜 Elvis(Elvis.INSTANCE)를 반환한다.
        Elvis elvis = (Elvis) deserializable(serializedForm);
        Elvis impersonator = ElvisStealer.impersonator;
        
        elvis.printFavorites();
        impersonator.printFavorites();
    }
}
```

* 위 프로그램을 실행하면 서로 다른 2개의 Elvis 인스턴스를 생성한다.

* 위 문제를 해결하려면 Elvis를 원소 하나짜리 열거 타입으로 바꾼다.
  * 직렬화 가능한 인스턴스 통제 클래스를 열거 타입을 이용해 구현하면 선언한 상수 외의 다른 객체는 존재하지 않음을 자바가 보장해준다.

```
public enum Elvis {
    INSTANCE;
    private String[] favoriteSongs = { "Hound Dog", "Heartbreak Hotel" };
    public void printFavorites() {
        System.out.println(Arrays.toString(favoriteSongs));
    }
}
```

* 컴파일타임에는 어떤 인스턴스들이 있는지 알 수 없는 상황이라면 열거 타입으로 표현하는 게 불가능하다. 따라서 인스턴스 통제를 위해 readResolve를 사용해야 한다.

* readResolve 메서드의 접근성은 매우 중요하다.
  * final 클래스에서라면 readResolve 메서드는 private이어야 한다.
  * final이 아닌 클래스인 경우
    * private으로 선언하면 하위 클래스에서 사용할 수 없다.
    * package-private으로 선언하면 같은 패키지에 속한 하위 클래스에서만 사용할 수 있다.
    * protected나 public으로 선언하면 이를 재정의하지 않은 모든 하위 클래스에서 사용할 수 있다.
    * protected나 public이면서 하위 클래스에서 재정의하지 않았다면, 하위 클래스의 인스턴스를 역직렬화하면 상위 클래스의 인스턴스를 생성하여 ClassCastException을 일으킬 수 있다.
  