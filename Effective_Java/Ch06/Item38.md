# 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라

* 열거 타입은 확장할 수 없다. 즉, 열거한 값들을 그대로 가져온 다음 값을 더 추가해 다른 목적으로 쓸 수 없다.

* 다만 연산 코드의 경우 API가 제공하는 기본 연산 외에 사용자 확장 연산을 추가할 수 있도록 열어줘야 할 때가 있다.
  * 열거 타입이 임의의 인터페이스를 구현할 수 있다는 사실을 이용해, 연산 코드용 인터페이스를 정의하고 열거 타입이 이 인터페이스를 구현하게 하면 된다.
  * 이때 열거 타입이 그 인터페이스의 표준 구현체 역할을 한다.
  
> 인터페이스를 이용한 확장 가능 열거 타입 모방
```
public interface Operation {
    double apply(double x, double y);
}

public enum BasicOperation implements Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }        
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };
    
    private final String symbol;
    
    BasicOperation(String symbol) {
        this.symbol = symbol;
    }
    
    @Override public String toString() {
        return symbol;
    }
}
```

* 위 예시에서 BasicOperation은 열거 타입으로 확장할 수 없지만, 인터페이스인 Operation은 확장할 수 있고, 이 인터페이스를 연산의 타입으로 사용하면 된다.
  * 이렇게 하면 Operation을 구현한 또 다른 열거 타입을 정의해 기본 타입인 BasicOperation을 대체할 수 있다.
  
> 열거 타입 확장 예시
```
public enum ExtendedOperation implements Operation {
    EXP("^") {
        public double apply(double x, double y) {
            return Math.pow(x, y);
        }
    },
    REMAINDER("%") {
        public double apply(double x, double y) {
            return x % y;
        }
    };
    
    private final String symbol;
    
    ExtendedOperation(String symbol) {
        this.symbol = symbol;
    }
    
    @Override public String toString() {
        return symbol;
    }
}
```

* Operation 인터페이스를 사용하도록 작성되어 있기만 하면 새로 작성한 연산은 기존 연산을 쓰던 곳이면 어디든 쓸 수 있다.
* apply가 인터페이스에 선언되어 있어 열거 타입에 따로 추상 메서드로 선언하지 않아도 된다.

* 기본 열거 타입 대신 확장된 열거 타입을 넘겨 확장된 열거 타입의 원소 모두를 사용하게 할 수도 있다.

```
public static void main(String[] args) {
    double x = Double.parseDouble(args[0]);
    double y = Double.parseDouble(args[1]);
    test(ExtendedOperation.class, x, y);
    test2(Arrays.asList(ExtendedOperation.values()), x, y);
}

// Class 객체가 열거 타입인 동시에 Operation의 하위 타입이어야 한다.
private static <T extends Enum<T> & Operation> void test(Class<T> opEnumType, double x, double y) {
    for (Operation op : opEnumType.getEnumConstants()) {
        System.out.printf("%f %s %f = %f%n, x, op, y, op.apply(x, y));
    }
}

// Class 객체 대신 한정적 와일드카드 타입인 Collection<? extends Operation>을 넘기는 방법
private static void test2(Collection<? extends Operation> opSet, double x, double y) {
    for (Operation op : opSet) {
        System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
    }
}
```

* 인터페이스를 이용해 확장 가능한 열거 타입을 흉내 내는 방식은 열거 타입끼리 구현을 상속할 수 없다는 문제점이 있다.
  * 아무 상태에도 의존하지 않는 경우에는 디폴트 구현을 이용해 인터페이스에 추가한다.
  * 그렇지 않은 경우 공유하는 기능이 많으면 별도의 도우미 클래스나 정적 도우미 메서드로 분리한다.