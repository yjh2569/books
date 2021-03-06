# toString을 항상 재정의하라

* Object의 기본 toString은 클래스_이름@16진수로_표시한_해시코드를 반환한다.
* toString을 잘 구현한 클래스는 사용하기에 훨씬 즐겁고, 그 클래스를 사용한 시스템은 디버깅하기 쉽다.
* toString 메서드는 객체를 println, printf, 문자열 연결 연산자(+), assert 구문에 넘길 때, 디버거가 객체를 출력할 때 자동으로 불린다.
* toString은 그 객체가 가진 주요 정보 모두를 반환하는 게 좋다. 모두 반환하기 쉽지 않다면 요약 정보를 담아야 한다.
* toString을 구현할 때 반환값의 포맷을 문서화할지 정해야 한다. 값 클래스라면 문서화를 권한다.
  * 포맷을 명시하면 그 객체는 표준적이고, 명확하고, 사람이 읽을 수 있게 된다.
  * 포맷을 명시하기로 했다면, 포맷에 맞는 문자열과 객체를 상호 전환할 수 있는 정적 팩토리나 생성자를 함께 제공해주면 좋다.
  * 포맷을 한번 명시하면 평생 그 포맷에 얽매이게 된다. 반대로 포맷을 명시하지 않으면 향후 릴리스에서 정보를 더 넣거나 포맷을 개선할 수 있는 유연성을 얻게 된다.
* 포맷을 명시하든 아니든 의도는 명확히 밝혀야 한다.

```
/**
 * 이 전화번호의 문자열 표현을 반환한다.
 * 이 문자열은 "XXX-YYY-ZZZZ" 형태의 12글자로 구성된다.
 * XXX는 지역 코드, YYY는 프리픽스, ZZZZ는 가입자 번호다.
 * 각각의 대문자는 10진수 숫자 하나를 나타낸다.
 * 
 * 전화번호의 각 부분의 값이 너무 작아서 자릿수를 채울 수 없다면,
 * 앞에서부터 0으로 채워나간다. 예컨대 가입자 번호가 123이라면
 * 전화번호의 마지막 네 문자는 "0123"이 된다.
 */
@Override public String toString() {
    return String.format("%03d-%03d-%04d", areaCode, prefix, lineNum);
}
```

> 포맷을 명시하지 않는 경우

```
/**
 * 이 약물에 관한 대략적인 설명을 반환한다.
 * 다음은 이 설명의 일반적인 형태이나, 
 * 상세 형식은 정해지지 않았으며 향후 변경될 수 있다.
 *
 * "[약물 #9: 유형=사랑, 냄새=테레빈유, 겉모습=먹물]"
 */
@Override public String toString() { ... }
```

* 포맷 명시 여부와 상관없이 toString이 반환한 값에 포함된 정보를 얻어올 수 있는 API를 제공하자. 그렇지 않으면 toString의 반환값을 파싱해야 한다.
> PhoneNumber 클래스는 지역 코드, 프리픽스, 가입자 번호용 접근자를 제공해야 한다.

* 정적 유틸리티 클래스는 toString을 제공할 이유가 없다.
* Enum 역시 toString을 제공하니 따로 재정의하지 않아도 된다.
* 하위 클래스들이 공유해야 할 문자열 표현이 있는 추상 클래스라면 toString을 재정의해야 한다.
> 대다수의 컬렉션 구현체는 추상 컬렉션 클래스들의 toString 메서드를 상속해 쓴다.

* 구글의 AutoValue 프레임워크는 toString을 자동 생성해 준다. 다만 자동 생성이다 보니 클래스의 의미까지 파악하지는 못한다.
