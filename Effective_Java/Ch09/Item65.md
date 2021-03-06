# 리플렉션보다는 인터페이스를 사용하라

* 리플렉션 기능(java.lang.reflect)을 이용하면 프로그램에서 임의의 클래스에 접근할 수 있다.
  * Class 객체가 주어지면 그 클래스의 생성자, 메서드, 필드에 해당하는 Constructor, Method, Field 인스턴스를 가져올 수 있다.
  * 이 인스턴스들로는 그 클래스의 멤버 이름, 필드 타입, 메서드 시그니처 등을 가져올 수 있다.
  * 이 인스턴스를 이용해 각각에 연결된 실제 생성자, 메서드, 필드를 조작할 수도 있다. 즉, 해당 클래스의 인스턴스를 생성하거나, 메서드를 호출하거나, 필드에 접근할 수 있다.
  > Method.invoke는 어떤 클래스의 어떤 객체가 가진 어떤 메서드라도 호출할 수 있게 해준다.
  * 리플렉션을 이용하면 컴파일 당시에 존재하지 않던 클래스도 이용할 수 있다.
  
* 리플렉션의 단점
  * 컴파일타임 타입 검사가 주는 이점을 하나도 누릴 수 없다. 
    * 프로그램이 리플렉션 기능을 써서 존재하지 않는 혹은 접근할 수 없는 메서드를 호출하려 시도하면 런타임 오류가 발생한다.
  * 리플렉션을 이용하면 코드가 지저분하고 장황해진다.
  * 성능이 떨어진다.
  
* 리플렉션은 아주 제한된 형태로만 사용해야 그 단점을 피하고 이점만 취할 수 있다.
  * 컴파일타임에 이용할 수 없는 클래스를 사용해야만 하는 프로그램은 비록 컴파일타임이라도 적절한 인터페이스나 상위 클래스를 이용할 수는 있을 것이다.
  * 이런 경우라면 리플렉션은 인스턴스 생성에만 쓰고, 이렇게 만든 인스턴스는 인터페이스나 상위 클래스로 참조해 사용한다.
  
> 리플렉션으로 생성하고 인터페이스로 참조해 활용하는 예시
```
public static void main(String[] args) {
    // 클래스 이름을 Class 객체로 변환
    Class<? extends Set<String>> cl = null;
    try {
        cl = (Class<? extends Set<String>>) Class.forName(args[0]); // 비검사 형변환
    } catch(ClassNotFoundException e) {
        fatalError("클래스를 찾을 수 없습니다.");
    }
    
    // 생성자를 얻는다.
    Constructor<? extends Set<String>> cons = null;
    try {
        cons = cl.getDeclaredContructor();
    } catch (NoSuchMethodException e) {
        fatalError("매개변수 없는 생성자를 찾을 수 없습니다.");
    }
    
    // 집합의 인스턴스를 만든다.
    Set<String> s = null;
    try {
        s = cons.newInstance();
    } catch (IllegalAccessException e) {
        fatalError("생성자에 접근할 수 없습니다.");
    } catch (InstantiationException e) {
        fatalError("클래스를 인스턴스화할 수 없습니다.");
    } catch (InvocationTargetException e) {
        fatalError("생성자가 예외를 던졌습니다: " + e.getCause());
    } catch (ClassCastException e) {
        fatalError("Set을 구현하지 않은 클래스입니다.");
    }
    
    // 생성한 집합을 사용한다.
    s.addAll(Arrays.asList(args).subList(1, args.length));
    System.out.println(s);
}

private static void fatalError(String msg) {
    System.err.println(msg);
    System.exit(1);
}
```

* 위 예시에서의 리플렉션의 단점
  * 런타임에 총 여섯 가지나 되는 예외를 던질 수 있다. 그 모두는 인스턴스를 리플렉션 없이 생성했다면 컴파일타임에 잡아낼 수 있었던 예외들이다.
  * 클래스 이름만으로 인스턴스를 생성해내기 위해 무려 25줄이나 되는 코드를 작성했다. 리플렉션이 아니라면 생성자 호출 한 줄로 끝났을 것이다.
  
* 리플렉션은 런타임에 존재하지 않을 수도 있는 다른 클래스, 메서드, 필드와의 의존성을 관리할 때 적합하다.
  * 이 기법은 버전이 여러 개 존재하는 외부 패키지를 다룰 때 유용하다.
  * 가동할 수 있는 최소한의 환경, 즉 주로 가장 오래된 버전만을 지원하도록 컴파일한 후, 이후 버전의 클래스와 메서드 등은 리플렉션으로 접근하는 방식이다.
  * 이렇게 하려면 접근하려는 새로운 클래스나 메서드가 런타임에 존재하지 않을 수 있다는 사실을 반드시 감안해야 한다.