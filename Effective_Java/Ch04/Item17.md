# 변경 가능성을 최소화하라

* 불변 클래스 : 인스턴스의 내부 값을 수정할 수 없는 클래스
> String, 기본 타입의 박싱된 클래스들, BigInteger, BigDecimal

* 클래스를 불변으로 만들기 위한 규칙
  * 객체의 상태를 변경하는 메서드를 제공하지 않는다.
  * 클래스를 확장할 수 없도록 한다. 즉, 상속을 막아야 한다. 클래스를 final로 선언하는 방법이 있다.
  * 모든 필드를 final로 선언한다.
  * 모든 필드를 private으로 선언한다.
  * 자신 외에는 내부의 가변 컴포넌트에 접근할 수 없도록 한다.
  
> 불변 복소수 클래스

```
public final class Complex {
    private final double re;
    private final double im;
    
    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }
    
    public double realPart() { return re; }
    public double imaginaryPart() { return im; }
    
    public Complex plus(Complex c) {
        return new Complex(re + c.re, im + c.im);
    }
    
    public Complex minus(Complex c) {
        return new Complex(re - c.re, im - c.im);
    }
    
    public Complex times(Complex c) {
        return new Complex(re * c.re - im * c.im, re * c.im + im * c.re);
    }
    
    public Complex dividedBy(Complex c) {
        double tmp = c.re * c.re + c.im * c.im;
        return new Complex((re * c.re + im * c.im) / tmp, im * c.re - re * c.im) / tmp); 
    }
    
    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Complex)) return false;
        Complex c = (Complex) o;
        
        // == 대신 compare를 사용한다.
        return Double.compare(c.re, re) == 0 && Double.compare(c.im, im) == 0;
    }
    
    @Override public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }
    
    @Override public String toString() {
        return "(" + re + " + " + im + "i)";
    }
}
```

* 위 예시에서 사칙연산 메서드들이 모두 인스턴스 자신은 수정하지 않고 새 인스턴스를 만들어 반환한다.
* 이처럼 피연산자에 함수를 적용해 그 결과를 반환하지만, 피연산자 자체는 그대로인 프로그래밍 패턴을 함수형 프로그래밍이라고 한다.
  * 절차적 혹은 명령형 프로그래밍은 메서드에서 피연산자인 자신을 수정해 자신의 상태가 변한다.

* 불변 객체는 근본적으로 스레드 안전하여 따로 동기화할 필요가 없다.
  * 여러 스레드가 동시에 사용해도 절대 훼손되지 않는다.
  * 불변 객체는 그 어떤 스레드도 다른 스레드에 영향을 줄 수 없어 불변 객체는 안심하고 공유할 수 있다.

* 자주 쓰이는 값을 상수로 제공해 한번 만든 인스턴스를 최대한 재활용할 수 있다.
* 자주 쓰이는 인스턴스를 캐싱해 같은 인스턴스를 중복 생성하지 않게 해주는 정적 팩토리를 제공할 수도 있다.

* 불변 객체는 자유롭게 공유할 수 있고, 이로 인해 방어적 복사도 필요없다. 아무리 복사해도 원본과 똑같기 때문이다.
  * 그러므로 불변 클래스는 clone 메서드나 복사 생성자를 제공하지 않는 게 좋다.

* 불변 객체는 자유롭게 공유할 수 있을 뿐만 아니라 불변 객체끼리 내부 데이터를 공유할 수 있다.
> BigInteger의 경우 값의 부호와 크기를 각각 int 변수와 int 배열을 사용하는데, negate 메서드로 크기가 같고 부호만 반대인 새로운 BigInteger를 생성할 때, 크기(배열)는 비록 가변이지만 복사하지 않고 원본 인스턴스와 공유한다.

* 객체를 만들 때 다른 불변 객체들을 구성요소로 사용하면 이점이 많다.
  * 불변 요소들로 이루어진 객체라면 그 구조가 아무리 복잡해도 불변식을 유지하기 훨씬 쉽기 때문이다.
  > 불변 객체는 맵의 키와 집합의 원소로 쓰기에 좋다.

* 불변 객체는 그 자체로 실패 원자성을 제공한다.
  * 실패 원자성(failure atomicity) : 메서드에서 예외가 발생한 후에도 그 객체는 여전히 (메서드 호출 전과 똑같은) 유효한 상태여야 한다.

* 불변 클래스의 단점
  * 값이 다르면 반드시 독립된 객체로 만들어야 한다. 따라서 값의 가짓수가 많으면 이들을 만드는데 큰 비용을 치러야 한다.
  * 원하는 객체를 완성하기까지의 단계가 많고, 그 중간 단계에서 만들어진 객체들이 모두 버려진다면 성능 문제가 더 불거진다.
  * 위 문제에 대처하기 위해 다단계 연산들을 예측하여 기본 기능으로 제공한다.
  > BigInteger는 다단계 연산 속도를 높여주는 가변 동반 클래스를 package-private으로 두고 있다. String은 public 가변 동반 클래스로 StringBuilder, StringBuffer를 두고 있다.
  
* 불변 클래스를 만드는 또 다른 방법으로 모든 생성자를 private 또는 package-private으로 만들고 public 정적 팩토리를 제공하는 방법이 있다.

* 불변 클래스의 규칙 중 모든 필드가 final이고 어떤 메서드도 그 객체를 수정할 수 없다고 했었는데, 이는 "어떤 메서드도 객체 상태 중 외부에 비치는 값을 변경할 수 없다."로 완화할 수 있다.
  * 계산 비용이 큰 값을 나중에 (처음 쓰일 때) 계산하고 이를 final이 아닌 필드에 캐시할 때 유용하다.

* 클래스는 꼭 필요한 경우가 아니면 불변이어야 한다.
  * 단순 값 객체는 항상 불변으로 만든다.
  * 무거운 값 객체의 경우 성능이 문제가 되면 불변 클래스와 쌍을 이루는 가변 동반 클래스를 public 클래스로 제공한다.
  * 불변으로 만들 수 없는 클래스라도 변경할 수 있는 부분을 최소한으로 줄인다.
  * 다른 합당한 이유가 없다면 모든 필드는 private final이어야 한다.
  * 생성자는 불변식 설정이 모두 완료된, 초기화가 완벽히 끝난 상태의 객체를 생성해야 한다.