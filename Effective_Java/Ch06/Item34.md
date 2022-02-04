# int 상수 대신 열거 타입을 사용하라

* 열거 타입 : 일정 개수의 상수 값을 정의한 다음, 그 외의 값은 허용하지 않는 타입

> 가장 단순한 열거 타입
```
public enum Apple { FUJI, PIPPIN, GRANNY_SMITH }
public enum Orange { NAVEL, TEMPLE, BLOOD }
```

* 열거 타입의 특징
  * 열거 타입 자체는 클래스이며, 상수 하나당 자신의 인스턴스를 하나씩 만들어 public static final 필드로 공개한다.
  * 밖에서 접근할 수 있는 생성자를 제공하지 않으므로 사실상 final이다. 따라서 열거 타입 선언으로 만들어진 인스턴스들은 딱 하나씩만 존재한다. 싱글턴은 원소가 하나뿐인 열거 타입이라 할 수 있다.
  * 컴파일타임 타입 안전성을 제공한다. 열거 타입을 매개변수로 받는 메서드 선언 시 다른 타입의 값을 넘기려 하면 컴파일 오류가 난다.
  * 열거 타입에는 각자의 이름공간이 있어서 이름이 같은 상수도 평화롭게 공존한다.
  * 새로운 상수를 추가하거나 순서를 바꿔도 다시 컴파일하지 않아도 된다. 공개되는 것이 오직 필드의 이름뿐이라, 상수 값이 클라이언트로 컴파일되어 각인되지 않기 때문이다.
  * 열거 타입의 toString 메서드는 출력하기에 적합한 문자열을 내준다.
  * 임의의 메서드나 필드를 추가할 수 있고 임의의 인터페이스를 구현하게 할 수도 있다.
  
* 열거 타입에 메서드나 필드를 추가할 수 있다.
  * 각 상수와 연관된 데이터를 해당 상수 자체에 내재시키고 싶을 때 사용한다.
  
> 데이터와 메서드를 갖는 열거 타입
```
public enum Planet {
    MERCURY (3.302e+23, 2.439e6),
    VENUS   (4.869e+24, 6.052e6),
    EARTH   (5.975e+24, 6.378e6),
    MARS    (6.319e+23, 3.393e6),
    JUPITER (1.899e+27, 7.149e7),
    SATURN  (5.685e+26, 6.027e7),
    URANUS  (8.683e+25, 2.556e7),
    NEPTUNE (1.024e+26, 2.477e7);
    
    private final double mass; // 질량(단위: 킬로그램)
    private final double radius; // 반지름(단위: 미터)
    private final double surfaceGravity; // 표면중력(단위: m/s^2)
    
    // 중력 상수(단위: m^3 / kg s^2)
    private static final double G = 6.67300E-11;
    
    // 생성자
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        surfaceGravity = G * mass / (radius * radius);
    }
    
    public double mass() { return mass; }
    public double radius() { return radius; }
    public double surfaceGravity() { return surfaceGravity; }
    
    public double surfaceWeight(double mass) {
        return mass * surfaceGravity; // F = ma
    }
}
```

* 열거 타입 상수 각각을 특정 데이터와 연결지으려면 생성자에서 데이터를 받아 인스턴스 필드에 저장하면 된다.
  * 열거 타입은 근본적으로 불변이라 모든 필드는 final이어야 한다.
  * 필드를 public으로 선언해도 되지만, private으로 두고 별도의 public 접근자 메서드를 두는 게 낫다.

> 열거 타입을 이용하는 코드
```
public class WeightTable {
    public static void main(String[] args) {
        double earthWeight = Double.parseDouble(args[0]);
        dobule mass = earthWeight / Planet.EARTH.surfaceGravity();
        for (Planet p : Planet.values()) {
            System.out.printf("%s에서의 무게는 %f이다.%n", p, p.surfaceWeight(mass));
        }
    }
}
```

* 열거 타입은 자신 안에 정의된 상수들의 값을 배열에 담아 반환하는 정적 메서드인 values를 제공한다.
* 각 열거 타입 값의 toString 메서드는 상수 이름을 문자열로 반환한다. toString이 제공하는 이름이 내키지 않으면 원하는 대로 재정의해도 된다.

* 열거 타입에서 상수를 제거할 경우 제거한 상수를 참조하는 클라이언트는 프로그램을 다시 컴파일하면 컴파일 오류가 발생한다.

* 열거 타입을 선언한 클래스 혹은 그 패키지에서만 유용한 기능은 private이나 package-private 메서드로 구현한다.

* 널리 쓰이는 열거 타입은 톱레벨 클래스로 만들고, 특정 톱레벨 클래스에서만 쓰인다면 해당 클래스의 멤버 클래스로 만든다.

> 값에 따라 분기하는 열거 타입
```
public enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;
    
    // 상수가 뜻하는 연산 수행
    public double apply(double x, double y) {
        switch(this) {
            case PLUS: return x + y;
            case MINUS: return x - y;
            case TIMES: return x * y;
            case DIVIDE: return x / y;
        }
        throw new AssertionError("알 수 없는 연산: "+this);
    }
}
```

* 마지막 throw 문은 실제로는 도달할 일이 없지만 기술적으로는 도달할 수 있어 생략하면 컴파일조차 되지 않는다.
* 새로운 상수를 추가하면 해당 case 문도 추가해야 하기 때문에 깨지기 쉬운 코드다.

* 상수별 메서드 구현 : 상수별로 다르게 동작하는 코드를 구현하기 위해 열거 타입에 apply라는 추상 메서드를 선언하고 각 상수별 클래스 몸체, 즉 각 상수에서 자신에 맞게 재정의하는 방법
  * 추상 메서드를 구현하지 않으면 컴파일 오류로 알려준다.
  * 상수별 메서드 구현을 상수별 데이터와 결합할 수도 있다.
  * 열거 타입 상수끼리 코드를 공유하기 어렵다.

> 상수별 메서드 구현을 활용한 열거 타입
```
public enum Operation {
    PLUS("+") {public double apply(double x, double y) {return x + y;}},
    MINUS("-") {public double apply(double x, double y) {return x - y;}},
    TIMES("*") {public double apply(double x, double y) {return x * y;}},
    DIVIDE("/") {public double apply(double x, double y) {return x / y;}};
    
    private final String symbol;
    
    Operation(String symbol) { this.symbol = symbol; }
    
    @Override public String toString() { return symbol; }
    public abstract double apply(double x, double y);
}
```

> toString을 통한 계산식 출력 간편화
```
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    for (Operation op : Operation.values()) {
        System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
    }
}
```

* valueOf(String) 메서드 : 상수 이름을 입력받아 그 이름에 해당하는 상수를 반환
* fromString 메서드 : toString이 반환하는 문자열을 해당 열거 타입 상수로 변환

```
private static final Map<String, Operation> stringToEnum = 
        Stream.of(values()).collect(toMap(Object::toString, e -> e));
        
public static Optional<Operation> fromString(String symbol) {
    return Optional.ofNullable(stringToEnum.get(symbol));
}
```

> 값에 따라 분기하여 코드를 공유하는 열거 타입
```
enum PayrollDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
    
    private static final int MINS_PER_SHIFT = 8 * 60;
    
    int pay(int minutesWorked, int payRate) {
        int basePay = minutesWorked * payRate;
        
        int overtimePay;
        switch(this) {
            case SATURDAY: case SUNDAY: // 주말
                overtimePay = basePay / 2;
                break;
            default: // 주중
                overtimePay = minutesWorked <= MINS_PER_SHIFT ? 0 : (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
        }
        
        return basePay + overtimePay;
    }
}
```

* 위 코드는 간결하지만 관리 관점에서 위험한 코드다.
  * 휴가와 같은 새로운 값을 열거 타입에 추가하려면 그 값을 처리하는 case 문을 쌍으로 넣어줘야 한다.
  * 잔업수당을 계산하는 코드를 모든 상수에 중복해서 넣거나, 계산 코드를 평일용과 주말용으로 나눠 각각을 도우미 메서드로 작성한 다음 각 상수가 자신에게 필요한 메서드를 적절히 호출하는 방법이 있지만, 코드가 장황해져 가독성이 크게 떨어지고 오류 발생 가능성이 높아진다.
  
* 전략 열거 타입 패턴
  * 잔업수당 계산을 private 중첩 열거 타입으로 옮기고 PayrollDay 열거 타입의 생성자에서 이 중 적당한 것을 선택한다.
  * 이렇게 하면 PayrollDay 열거 타입은 잔업수당 계싼을 그 전략 열거 타입에 위임해 switch 문이나 상수별 메서드 구현이 필요 없다.
  
```
enum PayrollDay {
    MONDAY(WEEKDAY), TUESDAY(WEEKDAY), WEDNESDAY(WEEKDAY), THURSDAY(WEEKDAY), FRIDAY(WEEKDAY), SATURDAY(WEEKEND), SUNDAY(WEEKEND);
    
    private final PayType payType;
    
    PayrollDay(PayType payType) { this.payType = payType; }
    
    int pay(int minutesWorked, int payRate) {
        return payType.pay(minutesWorked, payRate);
    }
    
    // 전략 열거 타입
    enum PayType {
        WEEKDAY {
            int overtimePay(int minsWorked, int payRate) {
                return minutesWorked <= MINS_PER_SHIFT ? 0 : (minutesWorked - MINS_PER_SHIFT) * payRate / 2;
            }
        }, 
        WEEKEND {
            int overtimePay(int minsWorked, int payRate) {
                return minsWorked * payRate / 2;
            }
        };
        
        abstract int overtimePay(int mins, int payRate);
        private static final int MINS_PER_SHIFT = 8 * 60;
        
        int pay(int minsWorked, int payRate) {
            int basePay = minsWorked * payRate;
            return basePay + overtimePay(minsWorked, payRate);
        }
    }
}
```

* switch 문은 열거 타입의 상수별 동작을 구현하는 데 적합하지 않지만, 기존 열거 타입에 상수별 동작을 혼합해 넣을 때는 switch 문이 좋은 선택이 될 수 있다.

```
public static Operation inverse(Operation op) {
    switch(op) {
        case PLUS: return Operation.MINUS;
        case MINUS: return Operation.PLUS;
        case TIMES: return Operation.DIVIDE;
        case DIVIDE: return Operation.TIMES;
        
        default: throw new AssertionError("알 수 없는 연산: "+op);
    }
}
```

* 필요한 원소를 컴파일타임에 다 알 수 있는 상수 집합이라면 항상 열거 타입을 사용한다.
* 열거 타입에 정의된 상수 개수가 영원히 고정 불변일 필요는 없다.