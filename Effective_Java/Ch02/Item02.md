# 생성자에 매개변수가 많다면 빌더를 고려하라

## 점층적 생성자 패턴

* 정적 팩토리 메서드와 생성자의 경우 선택적 매개변수가 많을 때 적절히 대응하기 어렵다. 
* 이에 대응하기 위해 점층적 생성자 패턴을 사용했다. 이는 매개변수 개수를 점차 늘려가며 생성자나 정적 팩토리 메서드를 만드는 방식으로 이렇게 해도 매개변수 중간에 불필요한 매개변수가 있을 경우 결국 그 매개변수에 기본값을 직접 대입해 줄 수밖에 없다.

> 점층적 생성자 패턴 예시

```
public class Example {
    private final int A; // 필수
    private final int B; // 필수
    private final int C; // 선택
    private final int D; // 선택
    private final int E; // 선택
    
    public Example(int A, int B) {
        this(A, B, 0);
    }
    
    public Example(int A, int B, int C) {
        this(A, B, C, 0);
    }
    
    public Example(int A, int B, int C, int D) {
        this(A, B, C, D, 0);
    }
    
    public Example(int A, int B, int C, int D, int E) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.E = E;
    }
}
```

> 이 클래스의 인스턴스를 만들 때 A와 B, C, E에 대한 정보만 알고 있을 때 D에 기본값인 0을 직접 대입할 수밖에 없다.

```
Example ex = new Example(1, 2, 3, 0, 5);
```

* 즉, 점층적 생성자 패턴을 쓸 수도 있지만 매개변수의 개수가 많아지면 클라이언트 코드를 작성하거나 읽기 어렵다. 각 값의 의미도 파악하기 어렵고, 매개변수가 몇 번째 매개변수인지도 주의해서 세어야 한다.

## 자바빈즈 패턴

* 또 다른 방법으로 자바빈즈 패턴을 이용했다. 이 방법은 매개변수가 없는 생성자로 객체를 먼저 만들고 setter 메서드들을 호출해 원하는 매개변수의 값을 설정하는 방식이다.

> 자바빈즈 패턴 예시

```
public class Example {
    private int A = -1;
    private int B = -1;
    private int C = 0;
    private int D = 0;
    private int E = 0;
    
    public Example() {}
    // setter 메서드들
    public void setA(int val) { A = val; }
    public void setB(int val) { B = val; }
    public void setC(int val) { C = val; }
    public void setD(int val) { D = val; }
    public void setE(int val) { E = val; }
}
```

> 자바빈즈 패턴에서 인스턴스 만드는 방법

```
Example ex = new Example();
ex.setA(1);
ex.setB(2);
ex.setC(3);
ex.setD(5);
```

* 이로써 점층적 생성자 패턴의 단점은 해결했으나 객체 하나를 만들기 위해 여러 개의 메서드를 호출해야 하기에 객체가 완전히 생성되기 전까지는 일관성이 무너진 상태에 놓이게 된다. 일관성이 무너지는 문제로 인해 자바빈즈 패턴에서는 클래스를 불변으로 만들 수 없다.

## 빌더 패턴

* 그래서 점층적 생성자 패턴의 안전성과 자바빈즈 패턴의 가독성을 겸비한 빌더 패턴을 사용한다.
* 빌더 패턴은 다음과 같이 설계한다.
  1. 필수 매개변수마능로 생성자(정적 팩토리 메서드)를 호출해 빌더 객체를 얻는다.
  2. 빌더 객체가 제공하는 setter 메서드들로 원하는 선택 매개변수를 설정한다.
  3. 매개변수가 없는 build 메서드를 호출해 필요한 객체를 얻는다.
* 빌더는 보통 생성할 클래스 안에 정적 멤버 클래스로 만들어둔다.

> 빌더 패턴 예시

```
public class Example {
    private final int A;
    private final int B;
    private final int C;
    private final int D;
    private final int E;
    
    public static class Builder {
        // 필수 매개변수
        private final int A;
        private final int B;
        
        // 선택 매개변수 - 기본값으로 초기화
        private int C = 0;
        private int D = 0;
        private int E = 0;
        
        public Builder(int A, int B) {
            this.A = A;
            this.B = B;
        }
        
        public Builder C(int val) {
            C = val; return this;
        }
        
        public Builder D(int val) {
            D = val; return this;
        }
        
        public Builder E(int val) {
            E = val; return this;
        }
        
        public Exmaple build() {
            return new Example(this);
        }
    }
    
    private Example(Builder builder) {
        A = builder.A;
        B = builder.B;
        C = builder.C;
        D = builder.D;
        E = builder.E;
    }
}
```

* 빌더의 setter 메서드들은 빌더 자신을 반환하기 때문에 연쇄적으로 호출할 수 있다. 이런 방식을 fluent API 혹은 메서드 연쇄(chaining)라 한다.

> 위 예시 클래스를 사용하는 코드 예시

```
Example ex = new Example.Builder(1, 2).C(3).E(5).build();
```

* 빌더 패턴은 파이썬과 스칼라에 있는 명명된 선택적 매개변수(named optional parameters)를 흉내낸 것이다.

### 빌더 패턴의 장점

* 빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기 좋다.
* 가변인수 매개변수를 여러 개 사용할 수 있다. 각각을 적절한 메서드로 나눠 선언하거나, 메서드를 여러 번 호출하도록 하고 각 호출 때 넘겨진 매개변수들을 하나의 필드로 모을 수도 있다.
* 빌더 하나로 여러 객체를 순회하면서 만들 수 있고, 빌더에 넘기는 매개변수에 따라 다른 객체를 만들 수도 있다.
* 특정 필드는 빌더가 알아서 채우도록 할 수도 있다.

### 빌더 패턴의 단점

* 객체를 만들기 위해서 빌더부터 만들어야 하는데, 빌더 생성 비용이 크지는 않지만 성능이 민감한 상황에서는 문제가 될 수 있다.
* 점층적 생성자 패턴보다 코드가 장황해서 매개변수가 4개 이상은 되어야 값어치를 한다. 다만, 이는 API가 시간이 지남에 따라 매개변수가 많아지는 경향을 고려하면 처음부터 빌더 패턴으로 시작하는 것이 나을 수 있다.
