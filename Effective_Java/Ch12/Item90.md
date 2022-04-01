# 직렬화된 인스턴스 대신 직렬화 프록시 사용을 검토하라

* 직렬화 프록시 패턴
  * 바깥 클래스의 논리적 상태를 정밀하게 표현하는 중첩 클래스
  * private static으로 선언한다.
  * 중첩 클래스의 생성자는 단 하나여야 하며, 바깥 클래스를 매개변수로 받아야 한다.
    * 이 생성자는 단순히 인수로 넘어온 인스턴스의 데이터를 복사한다.
  * 일관성 검사나 방어적 복사도 필요없다.
  * 직렬화 프록시의 기본 직렬화 형태는 바깥 클래스의 직렬화 형태로 쓰기에 이상적이다.
  * 바깥 클래스와 직렬화 프록시 모두 Serializable을 구현했다고 선언해야 한다.
  
> Period 클래스용 직렬화 프록시
```
private static class SerializationProxy implements Serializable {
    private final Date start;
    private final Date end;
    
    SerializationProxy(Period p) {
        this.start = p.start;
        this.end = p.end;
    }
    
    private static final long serialVersionUID = ... // 아무 값이나 상관없다.
}

// 바깥 클래스에 writeReplace 메서드를 추가한다. 
// 이 메서드는 범용적이니 직렬화 프록시를 사용하는 모든 클래스에 그대로 복사해 쓰면 된다.
private Object writeReplace() {
    return new SerializationProxy(this);
}
```

* 위 메서드는 자바의 직렬화 시스템이 바깥 클래스의 인스턴스 대신 SerializationProxy의 인스턴스를 반환하게 하는 역할을 한다.
  * 즉, 직렬화가 이뤄지기 전에 바깥 클랫의 인스턴스를 직렬화 프록시로 변환해준다.
  * writeReplace 덕분에 직렬화 시스템은 결코 바깥 클래스의 직렬화된 인스턴스를 생성해낼 수 없다.
  * 하지만 공격자는 불변식을 훼손하고자 이런 시도를 해볼 수 있는데, 다음의 readObject 메서드를 바깥 클래스에 추가하면 이 공격을 가볍게 막아낼 수 있다.
  
```
// 직렬화 프록시 패턴용 readObject 메서드
private void readObject(ObjectInputStream stream) throws InvalidObjectException {
    throw new InvalidObjectException("프록시가 필요합니다.");
}
```

* 마지막으로, 바깥 클래스와 논리적으로 동일한 인스턴스를 반환하는 readResolve 메서드를 SerializationProxy 클래스에 추가한다.
  * 이 메서드는 역직렬화 시에 직렬화 시스템이 직렬화 프록시를 다시 바깥 클래스의 인스턴스로 변환하게 해준다.
  
* readResolve 메서드는 공개된 API만을 사용해 바깥 클래스의 인스턴스를 생성하는데, 이 패턴이 아름다운 이유가 바로 여기 있다.
  * 직렬화는 생성자를 이용하지 않고도 인스턴스를 생성하는 기능을 제공하는데, 이 패턴은 직렬화의 이런 언어도단적 특성을 상당 부분 제거한다.
  * 즉, 일반 인스턴스를 만들 때와 똑같은 생성자, 정적 팩토리, 혹은 다른 메서드를 사용해 역직렬화된 인스턴스를 생성한다.
  * 따라서 역직렬화된 인스턴스가 해당 클래스의 불변식을 만족하는지 검사할 또 다른 수단을 강구하지 않아도 된다.
  * 그 클래스의 정적 팩토리나 생성자가 불변식을 확인해주고 인스턴스 메서드들이 불변식을 잘 지켜주기만 하면 된다.

> Period.SerializationProxy 용 readResolve 메서드
```
private Object readResolve() {
    return new Period(start, end); // public 생성자를 사용한다.
}
```

* 직렬화 프록시 패턴은 가짜 바이트 스트림 공격과 내부 필드 탈취 공격을 프록시 수준에서 차단해준다.
  * 직렬화 프록시는 Period의 필드를 final로 선언해도 되기에 Period 클래스를 진정한 불변으로 만들 수도 있다.
  * 어떤 필드가 기만적인 직렬화 공격의 목표가 될지 고민하지 않아도 되고, 역직렬화 때 유효성 검사를 수행하지 않아도 된다.
  
* 직렬화 프록시 패턴은 역직렬화한 인스턴스와 원래의 직렬화된 인스턴스의 클래스가 달라도 정상 작동한다.
  * EnumSet의 경우 팩토리가 열거 타입의 크기에 따라 RegularEnumSet 또는 JumboEnumSet 중 한 클래스의 인스턴스를 반환한다.
  * 원소 64개짜리 열거 타입을 가진 EnumSet을 직렬화한 다음 원소 5개를 추가하고 역직렬화하면 RegularEnumSet 인스턴스로 직렬화됐다가 JumboEnumSet 인스턴스로 역직렬화하면 좋은 상황이 발생한다.
  * 이를 위해 EnumSet은 직렬화 프록시 패턴을 사용했다.
  
> EnumSet의 직렬화 프록시
```
private static class SerializationProxy <E extends Enum<E>> implements Serializable {
    private final Class<E> elementType; // 이 EnumSet의 원소 타입
    
    private final Enum<?>[] elements; // 이 EnumSet 안의 원소들
    
    SerializationProxy(EnumSet<E> set) {
        elementType = set.elementType;
        elements = set.toArray(new Enum<?>[0]);
    }
    
    private Object readResolve() {
        EnumSet<E> result = EnumSet.noneOf(elementType);
        for (Enum<?> e : elements) result.add((E) e);
        return result;
    }
    
    private static final long serialVersionUID = ...;
}
```

* 직렬화 프록시 패턴의 한계
  * 클라이언트가 멋대로 확장할 수 있는 클래스에는 적용할 수 없다.
  * 객체 그래프에 순환이 있는 클래스에 적용할 수 없다.
  * 이런 객체의 메서드를 직렬화 프록시의 readResolve 안에서 호출하려 하면 ClassCastException이 발생한다.
  * 직렬화 프록시만 가졌을 뿐 실제 객체는 아직 만들어진 것이 아니기 때문이다.
  * 방어적 복사에 비해 느리다.