# 인터페이스는 타입을 정의하는 용도로만 사용하라

* 인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입 역할을 하는데, 오직 이 용도로만 사용해야 한다.

* 상수 인터페이스는 메서드 없이 상수를 뜻하는 static final 필드로만 가득 찬 인터페이스다.
  * 상수 인터페이스 안티패턴은 인터페이스를 잘못 사용한 예다.
  * 클래스 내부에서 사용하는 상수는 외부 인터페이스가 아니라 내부 구현에 해당한다. 상수 인터페이스로 구현하면 내부 구현을 클래스의 API로 노출하게 된다. 
  * 사용자에게 아무런 의미가 없는 상수 인터페이스는 오히려 사용자에게 혼란을 줘 클라이언트 코드에서 이 상수들을 사용할 수도 있다.
  
* 상수를 공개할 목적이라면 관련된 클래스나 인터페이스 자체에 추가한다.
  > 모든 숫자 기본 타입의 박싱 클래스에서 MIN_VALUE, MAX_VALUE 상수가 포함되어 있다.
  * 열거 타입으로 나타내기 적합한 상수라면 열거 타입으로 만들어 공개한다.
  * 인스턴스화할 수 없는 유틸리티 클래스에 담아 공개한다.

> 상수 유틸리티 클래스
```
public class PhysicalConstants {
    private PhysicalConstants() { } // 인스턴스화 방지
    
    public static final double AVOGADROS_NUMBER = 6.022_140_857e23;
    
    public static final double BOLTZMANN_CONST = 1.380_648_52e-23;
    
    public static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```

* 유틸리티 클래스에 정의된 상수를 클라이언트에서 사용하려면 클래스 이름까지 함께 명시해야 한다.
  * 유틸리티 클래스의 상수를 빈번히 사용한다면 정적 임포트하여 클래스 이름은 생략할 수 있다.
  
```
import static com.effectivejava.example.PhysicalConstants.*;

public class Test {
    double atoms(double mols) {
        return AVOGADROS_NUMBER * mols;
    }
}
```