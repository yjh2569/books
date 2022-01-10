# 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

* 맞춤법 검사기를 예시로 들면, 맞춤법 검사기는 사전이라는 자원에 의존한다. 이런 클래스를 정적 유틸리티 클래스로 구현하는 경우가 많다.

```
public class SpellChecker {
    private static final Lexicon dictionary = ...;
    
    private SpellChecker() {} // 객체 생성 방지
}
```

> 싱글턴으로 구현하는 경우

```
public class SpellChecker {
    private final Lexicon dictionary = ...;
    
    private SpellChecker(...) {}
    public static SpellChecker INSTANCE = new SpellChecker(...);
}
```

* 위 방식으로 구현한다는 것은 사전을 단 하나만 사용한다고 가정한다는 의미이다. 하지만 실전에서는 사전이 언어별로 있을 수도 있고, 특수 어휘용 사전, 테스트용 사전이 필요할 수도 있다.
* 따라서 SpellChecker가 여러 사전을 사용할 수 있도록 만드는 것이 좋다. 
* 단순히 final 한정자를 제거하고 다른 사전으로 교체하는 메서드를 추가할 수도 있지만, 이 방식은 어색하고 오류를 내기 쉬우며 멀티스레드 환경에서는 쓸 수 없다.
* 사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글턴 방식이 적합하지 않다.

* 클래스가 여러 자원 인스턴스를 지원해야 하고, 클라이언트가 원하는 자원을 사용해야 한다.
* 이를 위해 인스턴스 생성 시 생성자에 필요한 자원을 넘겨준다. 이는 의존 객체 주입의 한 형태다.

```
public class SpellChecker {
    private final Lexicon dictionary;
    
    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }
}
```

* 이러한 방식은 가장 단순하게 생성자를 정의하는 방법이나 자원에 final 한정자를 붙임으로써 여러 클라이언트가 의존 객체들을 안심하고 공유할 수 있도록 한다.
* 의존 객체 주입은 생성자, 정적 팩토리, 빌더에 모두 똑같이 응용할 수 있다.

* 이를 변형해 생성자에 자원 팩토리를 넘겨주는 방식이 있다. 이는 팩토리 메서드 패턴을 구현한 것이다.
* 팩토리 : 호출할 때마다 특정 타입의 인스턴스를 반복해서 만들어주는 객체

* 의존 객체 주입은 유연성과 테스트 용이성을 개선해주지만 의존성이 수천 개나 되는 큰 프로젝트에서는 코드를 어지럽게 만들기도 한다.
* Dagger, Juice, Spring 같은 의존 객체 주입 프레임워크를 사용하면 이를 해소할 수 있다.