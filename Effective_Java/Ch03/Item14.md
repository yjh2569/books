# Comparable을 구현할지 고려하라

* Comparable 인터페이스는 compareTo 메서드만 가지고 있다.
  * 다른 메서드들과 달리 compareTo는 Object의 메서드가 아니다.
  * equals와 거의 유사하지만 순서까지 비교할 수 있고, 제네릭하다.
* Comparable을 구현한 객체들의 배열은 Arrays.sort 메서드를 이용해 손쉽게 정렬할 수 있다.
* 검색, 극단값 계산, 자동 정렬 컬렉션 관리도 쉽게 할 수 있다.
* 자바 플랫폼 라이브러리의 모든 값 클래스와 열거 타입이 Comparable을 구현했다.
* 알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스 작성 시 반드시 Comparable 인터페이스를 구현한다.

* compareTo 메서드의 일반 규약
  * 객체와 주어진 객체의 순서를 비교해 객체가 주어진 객체보다 작으면 음의 정수, 같으면 0, 크면 양의 정수를 반환한다.
  * 객체와 비교할 수 없는 타입의 객체가 주어지면 ClassCastException을 던진다.
  * sgn(표현식) : 부호 함수를 말한다. 표현식의 값이 음수, 0, 양수일 때 -1, 0, 1을 반환한다.
  * sgn(x.compareTo(y)) == -sgn(y.compareTo(x)) (한 쪽이 예외를 던지면 다른 쪽도 예외를 던져야 한다.)
  * 추이성 : (x.compareTo(y) > 0 && (y.compareTo(z)) > 0 이면 x.compareTo(z) > 0
  * x.compareTo(y) == 0 이면 sgn(x.compareTo(z)) == sgn(y.compareTo(z))
  * (x.compareTo(y) == 0) == (x.equals(y)) : 필수는 아니지만 지키는 게 좋다. 이를 지키지 않는 클래스는 그 사실을 명시해야 한다.

* 타입이 다른 객체가 비교 대상으로 주어지면 ClassCastException을 던지거나 공통 인터페이스를 매개로 이뤄진다.

> 비교를 활용하는 클래스의 예로는 정렬된 컬렉션인 TreeSet과 TreeMap, 검색과 정렬 알고리즘을 활용하는 유틸리티 클래스인 Collections와 Arrays가 있다.

* compareTo 메서드 작성은 equals와 유사하나 몇 가지 차이점만 주의하면 된다.
  * Comparable은 타입을 인수로 받는 제네릭 인터페이스이므로 compareTo 메서드의 인수 타입은 컴파일타임에 정해진다. 
  * 입력 인수의 타입을 확인하거나 형변환할 필요가 없다는 뜻이다. 인수의 타입이 잘못됐다면 컴파일 자체가 되지 않는다.
  * null을 인수로 넣어 호출하면 NullPointerException을 던져야 한다.
  
* 객체 참조 필드 비교 시 compareTo 메서드를 재귀적으로 호출한다.
* Comparable을 구현하지 않은 필드나 표준이 아닌 순서로 비교해야 할 경우 Comparator를 대신 사용한다.

> CaseInsensitiveString 클래스에서의 compareTo
```
public final class CaseInsensitiveString implements Comparable<CaseInsensitiveString> {
    public int compareTo(CaseInsensitiveString cis) {
        return String.CASE_INSENSITIVE_ORDER.compare(s, cis.s); // 자바에서 제공하는 비교자를 사용한다.
    }
}
```

* 자바 7부터 박싱된 기본 타입 클래스에서 정적 메서드 compare를 제공하기에 compareTo 메서드에서 관계 연산자 <와 >를 가급적 사용하지 않는다.
* 클래스에 핵심 필드가 여러 개면 가장 핵심적인 필드부터 비교한다. 순차적으로 비교하다가 비교 결과가 0이 아닌 경우 그 결과를 바로 반환한다.

> PhoneNumber 클래스에서의 compareTo
```
public int compareTo(PhoneNumber pn) {
    int result = Short.compare(areaCode, pn.areaCode); // 가장 중요한 필드
    if (result == 0) {
        result = Short.compare(prefix, pn.prefix); // 두 번째로 중요한 필드
        if (result == 0) {
            result = Short.compare(lineNum, pn.lineNum); // 세 번째로 중요한 필드
        }
    }
    return result;
}
```

* 자바 8에서는 비교자 생성 메서드를 통한 메서드 연쇄 방식으로 비교자를 생성할 수 있게 되어 깔끔한 코드를 작성할 수 있다.

> PhoneNumber 클래스에서의 비교자 생성 메서드를 활용한 비교자
```
private static final Comparator<PhoneNumber> COMPARATOR = 
        comparingInt((PhoneNumber pn) -> pn.areaCode)
            .thenComparingInt(pn -> pn.prefix)
            .thenComparingInt(pn -> pn.lineNum);

public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
}
```

* comparingInt는 객체 참조를 int 타입 키에 매핑하는 키 추출 함수를 인수로 받아, 그 키를 기준으로 순서를 정하는 비교자를 반환하는 정적 메서드다.
* 자바의 타입 추론 능력이 이 상황에서 타입을 알아낼 만큼 강력하지 않기 때문에 람다에서 입력 인수의 타입을 명시한다. 
* thenComparingInt는 Comparator의 인스턴스 메서드로 int 키 추출자 함수를 입력받아 다시 비교자를 반환한다.
* long과 double에 대해서도 comparingInt와 유사한 메서드를 이용해 비교자를 만들 수 있다. 이를 통해 모든 숫자용 기본 타입은 커버한다.

* 객체 참조용 비교자 생성 메서드도 있다.
  * comparing이라는 정적 메서드가 2개 정의되어 있다. 하나는 키 추출자를 받아서 그 키의 자연적 순서를 이용하고, 다른 하나는 키 추출자 하나와 추출된 키를 비교할 비교자까지 총 2개의 인수를 받는다.
  * thenComparing이라는 인스턴스 메서드가 3개 정의되어 있다. 하나는 비교자 하나만 인수로 받아 그 비교자로 부차 순서를 정하고, 다른 하나는 키 추출자를 인수로 받아 그 키의 자연적 순서로 보조 순서를 정하며, 나머지 하나는 키 추출자와 추출된 키를 비교할 비교자까지 총 2개의 인수를 받는다.
  
* 이따금 값의 차를 기준으로 비교하는 compareTo와 compare 메서드가 있는데, 이러한 방식은 사용하면 안 된다. 정수 오버플로를 일으키거나 부동소수점 계산 방식에 따른 오류를 낼 수 있기 때문이다.

> 해시코드 값의 차를 기준으로 하는 비교자(추이성을 위배한다.)

```
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
};
```

> 정적 compare 메서드를 활용한 비교자

```
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
} 
```

> 비교자 생성 메서드를 활용한 비교자

```
static Comparator<Object> hashCodeOrder = Comparator.comparingInt(o -> o.hashCode());
```
