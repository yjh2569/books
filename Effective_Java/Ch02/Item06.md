# 불필요한 객체 생성을 피하라

* 똑같은 기능의 객체를 매번 생성하는 것보다 객체 하나를 재사용하는 편이 나을 때가 많다.
* 아이템 1에서 다루었듯이 생성자 대신 정적 팩토리 메서드를 제공하는 불변 클래스에서는 정적 팩토리 메서드 사용을 통해 불필요한 객체 생성을 피할 수 있다.
> Boolean(String) 생성자 대신 Boolean.valueOf(String) 팩토리 메서드를 사용하는 것이 좋다.

* 생성 비용이 아주 비싼 객체도 있는데, 이런 비싼 객체가 반복해서 필요하다면 캐싱해서 재사용한다.
> 주어진 문자열이 유효한 로마 숫자인지를 확인하는 메서드를 작성할 때, String.matches를 사용하면 메서드 내부에서 정규표현식용 Pattern 인스턴스를 한 번 쓰고 버려져서 가비지 컬렉션 대상이 된다. Pattern은 입력받은 정규표현식에 해당하는 유한 상태 머신을 만들기 때문에 인스턴스 생성 비용이 높다. 따라서 필요한 정규표현식을 표현하는 Pattern 인스턴스를 클래스 초기화 과정에서 직접 생성해 캐싱해두고, 나중에 isRomanNumeral 메서드가 호출될 때까지 이 인스턴스를 재사용한다.

```
public class RomanNumrals {
    private static final Pattern ROMAN = Pattern.compile("...");
    
    static boolean isRomanNumeral(String s) {
        return ROMAN.matcher(s).matches();
    }
}
```

* 불필요한 객체를 만들어내는 또 다른 예로 오토박싱이 있다.
* 오토박싱(auto boxing) : 프로그래머가 기본 타입과 박싱된 기본 타입을 섞어 쓸 때 자동으로 상호 변환해주는 기술
* 오토박싱은 기본 타입과 그에 대응하는 박싱된 기본 타입의 구분을 흐려주지만, 완전히 없애주는 것은 아니다.

> 다음은 모든 양의 정수의 총합을 구하는 코드로, sum 변수를 박싱된 기본 타입인 Long으로 정의해서 불필요한 인스턴스를 약 2^31개나 만든다.

```
private static long sum() {
    Long sum = 0L;
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i;
    }
    return sum;
}
```

* 따라서 박싱된 기본 타입보다는 기본 타입을 사용하고, 의도치 않은 오토박싱이 숨어들지 않도록 주의해야 한다.

* 그렇다고 객체 생성이 항상 비싸기에 피해야 할 필요는 없다. 요즘의 JVM에서는 별다른 일을 하지 않는 작은 객체를 생성하고 회수하는 일이 크게 부담되지 않기 때문에 프로그램의 명확성, 간결성, 기능을 위해 객체를 추가로 생성하는 것은 오히려 바람직하다.
* 아주 무거운 객체가 아니고서야 단순히 객체 생성을 피할려고 자신만의 객체 풀을 만들 필요는 없다.
* 이에 대조적으로 방어적 복사의 경우 새로운 객체를 만들어야 하는 상황에서는 기존 객체를 재사용하면 안 된다는 원칙을 가지고 있다. 이는 필요 없는 객체를 반복 생성할 때보다 더 큰 피해를 초래할 수도 있다.