# ordinal 메서드 대신 인스턴스 필드를 사용하라

* 모든 열거 타입은 상수가 그 열거 타입에서 몇 번째 위치인지를 반환하는 ordinal이라는 메서드를 제공한다.
  * 이 때문에 열거 타입 상수와 연결된 정수값이 필요하면 ordinal 메서드를 이용하려 할 수 있다.
  
> ordinal을 잘못 사용한 예
```
public enum Ensemble {
    SOLO, DUET, TRIO, QUARTET, QUINTET, SEXTET, SEPTET, OCTET, NONET, DECTET;
    
    public int numberOfMusicians() { return ordinal() + 1; }
}
```

* 위와 같이 ordinal을 사용하면 상수 선언 순서를 바꿀 수 없고, 이미 사용 중인 정수와 값이 같은 상수를 추가할 수 없으며, 값을 중간에 비워둘 수 없다.
  * 따라서 열거 타입 상수에 연결된 값은 ordinal 메서드로 얻지 말고 인스턴스 필드에 저장한다.
  
```
public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5), SEXTET(6), SEPTET(7), OCTET(8), NONET(9), DECTET(10), TRIPLE_QUARTET(12);
    
    private final int numberOfMusicians;
    Ensemble(int size) { this.numberOfMusicians = size; }
    public int numberOfMusicians() { return numberOfMusicians; }
}
```

* ordinal 메서드는 EnumSet과 EnumMap 같이 열거 타입 기반의 범용 자료구조에 쓸 목적으로 설계되었다.