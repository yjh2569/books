# equals를 재정의하려거든 hashCode도 재정의하라

* equals를 재정의한 클래스 모두에서 hashCode도 재정의해야 한다. 그렇지 않으면 hashCode 일반 규약을 어기게 되어 해당 클래스의 인스턴스를 HashMap이나 HashSet 같은 컬렉션의 원소로 사용할 때 문제를 일으킬 수 있다.
* Object 명세의 규약
  * equals 비교에 사용되는 정보가 변경되지 않는 한 hashCode 메서드는 항상 같은 값을 반환해야 한다.
  * equals가 두 객체를 같다고 판단한 경우 hashCode는 똑같은 값을 반환해야 한다.
  * equals가 두 객체가 다르다고 판단해도 hashCode 역시 달라야 할 필요는 없다. 단, 다른 값을 반환해야 해시테이블 성능이 좋아진다.

* hashCode 재정의를 잘못했을 때 크게 문제가 되는 조항은 두 번째다. 즉, 논리적으로 같은 객체는 hashCode가 같아야 한다.

> hashCode를 제대로 구현하지 않은 PhoneNumber 클래스에서 발생할 수 있는 문제 상황

```
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867,5309), "제니");
m.get(new PhoneNumber(707, 867,5309)); // 원하는 결과는 "제니"지만 실제로는 null을 반환한다. hashCode를 재정의하지 않았기 때문이다.
```

* 가장 간단한 hashCode 구현 방법은 모든 객체에 같은 해시코드를 반환하는 것이다. 하지만 이렇게 할 경우 해시테이블의 버킷 하나에만 계속 객체가 담겨 연결 리스트처럼 동작한다. 이로 인해 해시 테이블이 O(n) 수준으로 느려진다.
* 이 때문에 세 번째 규약이 필요하다. 이상적인 해시 함수는 주어진 인스턴스들을 32비트 정수 범위에 균일하게 분배해야 한다.
* hashCode 작성 요령
  1. int 변수 result 선언 후 값 c로 초기화(c는 해당 객체의 첫 번째 핵심 필드를 단계 2.a 방식으로 계산한 해시코드)
  2. 해당 객체의 나머지 핵심 필드 f 각각에 대해 다음 작업 수행
    a. 해당 필드의 해시 코드 c 계산
      * 기본 타입 필드 : Type.hashCode(f) 수행(Type : 기본 타입의 박싱 클래스)
      * 참조 타입 필드면서 이 클래스의 equals가 이 필드의 equals를 재귀적으로 호출해 비교 : 이 필드의 hashCode를 재귀적으로 호출(계산이 복잡해질 경우 이 필드의 표준형을 만들어 그 표준형의 hashCode 호출)(필드 값이 null이면 0을 사용)
      * 필드가 배열인 경우 : 핵심 원소 각각을 별도 필드처럼 취급하고, 위 규칙을 재귀적으로 적용해 각 원소의 해시코드를 계산한 뒤 2.b 방식으로 갱신(모든 원소가 핵심 원소면 Arrays.hashCode 사용)
    b. result 갱신 : result = 31*result + c;
  3. result 반환

* hashCode 구현 후 동치인 인스턴스에 대해 똑같은 해시코드를 반환할지 자문한다.
* 파생 필드는 해시코드 계산에서 제외해도 된다.
* equals 비교에 사용되지 않은 필드는 반드시 제외한다.

* 31 * result를 사용하는 이유
  1. 클래스에 비슷한 필드가 여러 개일 때 해시 효과를 크게 높여준다.
  > String의 hashCode를 곱셈 없이 구현하면 모든 아나그램(구성 철자가 같고 순서만 다른 문자열)의 해시코드가 같아진다.
  2. 31은 홀수이면서 소수이고, 시프트 연산과 뺄셈으로 대체해 최적화할 수 있다.((i << 5) - i)

> PhoneNumber 클래스에 대한 hashCode 메서드

```
@Override public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```

* 해시 충돌이 더 적은 방법을 쓰고 싶다면 com.google.common.hash.Hashing을 참고하자.

* Objects 클래스는 임의의 개수만큼 객체를 받아 해시코드로 계산해주는 정적 메서드인 hash를 제공한다. 다만 입력 인수를 담기 위한 배열을 만들고, 기본 타입의 경우 박싱과 언박싱을 거치기에 속도는 더 느리다.

> PhoneNumber의 hashCode를 Object.hash 메서드를 사용해 구현한 예시

```
@Override public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```

* 클래스가 불변이고 해시코드 게산 비용이 크다면 캐싱하는 방식을 고려해야 한다.
  * 해당 타입의 객체가 해시의 키로 사용될 것 같다면 인스턴스가 만들어질 때 해시코드를 계산해둬야 한다.
  * 해시의 키로 사용되지 않는 경우 hashCode가 처음 불릴 때 계산하는 지연 초기화 전략을 이용한다. 단, 필드를 지연 초기화할 때 그 클래스를 스레드 안전하게 만들도록 신경 써야 한다.
  
> PhoneNumber 클래스에서 해시코드를 지연 초기화하는 hashCode 메서드

```
private int hashCode; // 자동으로 0으로 초기화된다.

@Override public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        int result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```

* hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 말아야 한다. 그래야 클라이언트가 이 값에 의지하지 않고, 추후에 계산 방식을 바꿀 수도 있다.