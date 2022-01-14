# equals는 일반 규약을 지켜 재정의하라

* equals 메서드는 다음과 같은 상황에서는 재정의하지 않는 것이 낫다.
  * 각 인스턴스가 본질적으로 고유 : 값 표현이 아닌 동작하는 개체를 표현하는 클래스가 여기 해당한다. ex) Thread
  * 인스턴스의 논리적 동치성(logical equality)을 검사할 일이 없음 : java.util.regex.Pattern에서 equals를 재정의하여 두 Pattern의 인스턴스가 같은 정규표현식을 나타내는지를 검사하는 것이 논리적 동치성을 검사하는 예시라 할 수 있다.
  * 상위 클래스에서 재정의한 equals가 하위 클래스에도 들어맞음
  * 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없음
  
> equals가 실수로라도 호출되는 것을 막기 위한 코드
```
@Override public boolean equals(Object o) {
    throw new AssertionError(); // 호출 금지
}
```

* equals를 재정의하는 경우 : 논리적 동치성을 확인해야 하는데, 상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않은 경우. 주로 값 클래스들이 해당한다.
* 값 클래스 : Integer나 String처럼 값을 표현하는 클래스
* Enum과 같이 값 클래스라 해도 값이 같은 인스턴스가 둘 이상 만들어지지 않음이 보장되는 인스턴스 통제 클래스라면 equals를 재정의하지 않아도 된다.
* equals 메서드 재정의 시 따라야 하는 일반 규약
  * 반사성(reflexivity) : x.equals(x) = true
  * 대칭성(symmetry) : x.equals(y) = true ↔ y.equals(x) = true
  * 추이성(transitivity) : x.equals(y) = y.equals(z) = true → x.equals(z) = true
  * 일관성(consistency) : x.equals(y)는 항상 true이거나 false다.
  * not-null : x.equals(null) = false
  * 단, 위에서 나온 모든 x, y, z는 null이 아닌 참조 값이다.
  
> 대칭성 위배 코드
```
public final class CaseInsensitiveString {
    private final String s;
    
    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }
    
    @Override public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString) return s.equalsIgnoreCase((CaseInsensitiveString o).s);
        if (o instanceof String) return s.equalsIgnoreCase((String) o); // 한 방향으로만 작동한다. 따라서 이 코드를 제거하면 대칭성을 만족한다.
        return false;
    }
}
```

* 추이성은 상위 클래스에는 없는 새로운 필드를 하위 클래스에 추가하면서 위배하기 쉽다.
  * 단순히 상위 클래스의 equals 메서드와 새로운 필드에 대해 두 객체의 값이 같은지만 비교할 경우 하위 클래스의 equals와 상위 클래스의 equals가 달라 대칭성을 위배할 수 있다.
  * 그렇다고 비교 대상이 상위 클래스인 경우 상위 클래스의 equals를 활용하면 추이성을 위배한다.(상위 클래스 멤버가 같은 상위 클래스 - 하위 클래스 - 상위 클래스 순으로 비교할 경우)
  * 또한 instanceof 대신 getClass 메서드를 사용해 클래스가 온전히 같은 경우에만 비교하면 리스코프 치환 원칙을 위배한다.
  * 리스코프 치환 원칙(Liskov substitution principle) : 어떤 타입에 있어 중요한 속성이라면 그 하위 타입에서도 중요하다. 따라서 그 타입의 모든 메서드가 하위 타입에서도 마찬가지로 중요하다.
  * 이를 우회하기 위한 방법으로 상속 대신 컴포지션을 사용한다. 즉, 하위 클래스의 private 멤버로 상위 클래스의 객체를 두고, 상위 클래스 객체를 반환하는 view 메서드를 public으로 추가한다.
  * 상위 클래스를 직접 인스턴스로 만드는 게 불가능하다면(ex) 추상 클래스) 위 문제들은 발생하지 않는다.

* 일관성은 두 객체가 같다면 객체가 수정되지 않는 한 영원히 같아야 한다는 규칙이다.
  * 따라서 equals의 판단에 신뢰할 수 없는 자원이 끼워들게 해서는 안 된다.
  > java.net.URL의 equals는 주어진 URL과 매핑된 호스트의 IP 주소를 이용해 비교한다. 호스트 이름을 IP 주소로 바꾸려면 네트워크를 통해야 하는데 그 결과가 항상 같다고 보장할 수 없다.
  * equals는 항시 메모리에 존재하는 객체만을 사용한 결정적(deterministic) 계산만 수행해야 한다.
  
* not-null은 모든 객체가 null가 같지 않아야 한다는 규칙이다.
  * null과 같은지 직접 검사하기 보다는 instanceof 연산자로 입력 매개변수가 올바른 타입인지 검사하면서 암묵적으로 입력 매개변수가 null인지 같이 검사한다.
  
* equals 메서드 구현 방법
  1. == 연산자를 사용해 입력이 자기 자신의 참조인지 확인
  2. instanceof 연산자로 입력이 올바른 타입인지 확인 : 타입이 equals가 정의된 클래스가 아닌 클래스가 구현한 인터페이스인 경우 equals에서 해당 인터페이스를 사용해야 한다.
  3. 입력을 올바른 타입으로 형변환한다.
  4. 입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.
  
* float와 double은 Float.compare(float, float), Double.compare(double, double)을 이용해 비교한다. NaN, -0.0f, 특수한 부동소수 값 등을 다뤄야 하기 때문이다.
* 배열 필드는 원소 각각을 비교한다. 배열의 모든 원소가 핵심 필드라면 Arrays.equals 메서드들 중 하나를 사용한다.
* null도 정상 값으로 취급하는 참조 타입 필드의 경우 정적 메서드인 Object.equals(Object, Object)로 비교해 NullPointerException 발생을 예방한다.
* 앞의 CaseInsensitiveString과 같이 비교하기 복잡한 필드를 가진 클래스의 경우 필드의 표준형을 저장해둔 후 표준형끼리 비교하면 훨씬 경제적이다.
* 되도록이면 다를 가능성이 더 크거나 비교하는 비용이 더 싼 필드를 먼저 비교한다.
* 동기화용 락 필드와 같이 객체의 논리적 상태와 관련 없는 필드는 비교하면 안 된다.
* equals를 재정의할 때는 hashCode도 반드시 재정의한다.
* 너무 복잡하게 해결하려 하지 않는다.
* Object 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 않는다. 즉, Object 타입으로 매개변수를 받는다.
* AutoValue 프레임워크는 equals를 대신 작성하고 테스트해준다.