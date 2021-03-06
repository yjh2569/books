# 익명 클래스보다는 람다를 사용하라

* 자바에서 함수 타입을 표현할 때 추상 메서드를 하나만 담은 인터페이스를 사용했다. 
  * 이런 인터페이스의 인스턴스를 함수 객체라 하고, 특정 함수나 동작을 나타내는 데 썼다.
  * JDK 1.1 이후로는 함수 객체를 만들기 위해 익명 클래스를 사용했다.
  
> 익명 클래스의 인스턴스를 함수 객체로 사용하는 예
```
Collections.sort(words, new Comparator<String>() {
    public int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
})
```

* 자바 8에 와서 함수형 인터페이스들의 인터페이스를 람다식을 사용해 만들 수 있게 되었다.

> 람다식을 함수 객체로 사용하는 예시
```
Collections.sort(words, (s1, s2) -> Integer.compare(s1,length(), s2.length()));
```

* 람다, 매개변수 반환값의 타입은 컴파일러가 문맥을 살펴 타입을 추론해준다.
  * 타입을 명시해야 코드가 더 명확할 때만 제외하고는, 람다의 모든 매개변수 타입은 생략한다.
  * 컴파일러가 타입을 알 수 없다는 오류를 낼 때만 해당 타입을 명시하면 된다.
  
> 람다 자리에 비교자 생성 메서드를 사용하는 예
```
Collections.sort(words, comparingInt(String::length));
```

* 람다를 이용하면 열거 타입의 인스턴스 필드를 이용하는 방식으로 상수별로 다르게 동작하는 코드를 쉽게 구현할 수 있다.

```
public enum Operation {
    PLUS ("+", (x, y) -> x + y);
    MINUS ("-", (x, y) -> x - y);
    TIMES ("*", (x, y) -> x * y);
    DIVIDE ("/", (x, y) -> x / y);
    
    private final String symbol;
    private final DoubleBinaryOperator op;
    
    Operation(String symbol, DoubleBinaryOperator op) {
        this.symbol = symbol;
        this.op = op;
    }
    
    @Override public String toString() { return symbol; }
    
    public double apply(double x, double y) {
        return op.applyAsDouble(x, y);
    }
}
```

* 람다는 이름도 없고 문서화도 못 한다. 따라서 코드 자체로 동작이 명확히 설명되지 않거나 코드 줄 수가 많아지면 람다를 쓰면 안 된다.
  * 람다는 한 줄일 때 가장 좋고 길어야 세 줄 안에 끝내는 게 좋다.
  * 열거 타입의 인스턴스는 런타임에 만들어지기 때문에 열거 타입 생성자 안의 람다는 인수들의 타입을 컴파일타임에 추론할 수 없어 열거 타입의 인스턴스 멤버에 접근할 수 없다.
  * 따라서 상수별 동작을 길게 구현해야 하거나, 인스턴스 필드나 메서드를 사용해야 한다면 상수별 클래스 몸체를 사용해야 한다.
  
* 아래와 같은 경우에는 람다가 아닌 익명 클래스를 써야 한다.
  * 추상 클래스의 인스턴스를 만드는 경우
  * 추상 메서드가 여러 개인 인터페이스의 인스턴스를 만드는 경우
  * 함수 객체가 자신을 참조해야 하는 경우(람다는 자신을 참조할 수 없다. 람다에서 this는 바깥 인스턴스를 가리킨다.)
  
* 람다는 익명 클래스처럼 직렬화 형태가 구현별로 다를 수 있기에 람다를 직렬화하면 안 된다.
  * 직렬화해야만 하는 함수 객체가 있다면 private 정적 중첩 클래스를 사용한다.