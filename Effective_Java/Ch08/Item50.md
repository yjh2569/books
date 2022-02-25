# 적시에 방어적 복사본을 만들라

* 클라이언트가 불변식을 깨뜨리려 한다고 가정하고 방어적으로 프로그래밍해야 한다.

* 어떤 객체든 그 객체의 허락 없이는 외부에서 내부를 수정하는 일은 불가능하다.
  * 하지만 주의를 기울이지 않으면 자기도 모르게 내부를 수정하도록 허락하는 경우가 생긴다.

> 기간을 표현하는 클래스 : 불변식을 지키지 못함
```
public final class Period {
    private final Date start;
    private final Date end;
    
    /**
     * @param start 시작 시각
     * @param end 종료 시각; 시작 시각보다 뒤여야 한다.
     * @throws IllegalArgumentException 시작 시각이 종료 시각보다 늦을 때 발생한다.
     * @throws NullPointerException start나 end가 null이면 발생한다.
    */
    public Period(Date start, Date end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException(start+"가 "+end+"보다 늦다.");
        }
        this.start = start;
        this.end = end;
    }
    
    public Date start() {
        return start;
    }
    
    public Date end() {
        return end;
    }
    
    ... // 나머지 코드 생략
}

// Date가 가변이라는 사실을 사용하면 어렵지 않게 불변식을 깨뜨릴 수 있다.
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
end.setYear(78); // p의 내부를 수정했다.
```

* 자바 8 이후로는 Date 대신 불변인 Instant(또는 LocalDateTime이나 ZonedDateTime)를 사용하면 된다.

* 외부 공격으로부터 Period 인스턴스의 내부를 보호하려면 생성자에서 받은 가변 매개변수 각각을 방어적으로 복사해야 한다.
  * 그리고 Period 인스턴스 안에서는 원본이 아닌 복사본을 사용한다.

> Period 생성자 수정
```
public Period(Date start, Date end) {
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());
    
    if (this.start.compareTo(this.end) > 0) {
        throw new IllegalArgumentException(start+"가 "+end+"보다 늦다.");
    }
}
```

* 매개변수의 유효성을 검사하기 전에 방어적 복사본을 만들고, 이 복사본으로 유효성을 검사한다.
  * 멀티스레딩 환경이라면 원본 객체의 유효성을 검사한 후 복사본을 만드는 그 찰나의 취약한 순간에 다른 스레드가 원본 객체를 수정할 수 있기 때문이다.
  * 컴퓨터 보안 커뮤니티에서는 이를 검사시점/사용시점(time-of-check/time-of-use) 공격 혹은 TOCTOU 공격이라 한다.
  
* 방어적 복사에 Date의 clone 메서드를 사용해서도 안 된다.
  * Date는 final이 아니기에, clone이 악의를 가진 하위 클래스의 인스턴스를 반환할 수도 있다.
  * 즉, 매개변수가 제3자에 의해 확장될 수 있는 타입이라면 방어적 복사본을 만들 때 clone을 사용해서는 안 된다.
  
> 접근자 메서드를 이용한 공격
```
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
p.end().setYear(78); // p의 내부를 수정했다.
```

* 이를 방어하려면 접근자가 가변 필드의 방어적 복사본을 반환하면 된다.

```
public Date start() {
    return new Date(start.getTime());
}

public Date end() {
    return new Date(end.getTime());
}
```

* 생성자와 달리 접근자 메서드에서는 방어적 복사에 clone을 사용해도 된다.
  * Period가 가지고 있는 Date 객체는 신뢰할 수 없는 하위 클래스가 아닌 java.util.Date임이 확실하기 때문이다.
  * 그렇지만 인스턴스를 복사하는 데에는 일반적으로 생성자나 정적 팩토리를 쓰는 게 좋다.
  
* 클라이언트가 제공한 객체의 참조를 내부의 자료구조에 보관해야 할 때, 그 객체가 잠재적으로 변경되면 클래스의 동작에 문제가 생기는 경우에도 매개변수를 방어적으로 복사하기도 한다.
> 클라이언트가 건네준 객체를 내부의 Set 인스턴스에 저장하거나 Map 인스턴스의 키로 사용한다면, 추후 그 객체가 변경될 경우 객체를 담고 있는 Set 혹은 Map의 불변식이 깨질 것이다.

* 내부 객체를 클라이언트에 건네주기 전에도 방어적 복사본을 만들어 반환해야 한다.
  * 길이가 1 이상인 배열은 항상 가변이므로, 내부에서 사용하는 배열을 클라이언트에 반환할 때는 항상 방어적 복사를 수행해야 한다.
  
* 방어적 복사에는 성능 저하가 따르고, 또 항상 쓸 수 있는 것은 아니다.
  * (같은 패키지에 속하는 등의 이유로) 호출자가 컴포넌트 내부를 수정하지 않으리라 확신하면 방어적 복사를 생략할 수 있다.
  * 이러한 상황이라도 호출자에서 해당 매개변수나 반환값을 수정하지 말아야 함을 명확히 문서화하는 게 좋다.
  * 때로는 메서드나 생성자의 매개변수로 넘기는 행위가 그 객체의 통제권을 명백히 이전함을 뜻하기도 한다.
  * 이처럼 통제권을 이전하는 메서드를 호출하는 클라이언트는 해당 객체를 더 이상 직접 수정하는 일이 없다고 약속해야 한다.
  * 통제권을 넘겨받기로 한 메서드나 생성자를 가진 클래스들은 악의적인 클라이언트의 공격에 취약하다.
  * 따라서 해당 클래스와 그 클라이언트를 상호 신뢰할 수 있을 때, 혹은 불변식이 깨지더라도 그 영향이 오직 호출한 클라이언트로 국한될 때만 방어적 복사를 생략한다.
