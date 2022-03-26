# 스레드 안전성 수준을 문서화하라

* 메서드 선언에 synchronized 한정자를 선언할지는 구현 이슈일 뿐 API에 속하지 않는다.
  * 따라서 이것만으로는 그 메서드가 스레드 안전하다고 믿기 어렵다.
  * 멀티스레드 환경에서도 API를 안전하게 사용하게 하려면 클래스가 지원하는 스레드 안전성 수준을 정확히 명시해야 한다.
  
* 스레드 안전성이 높은 순서
  * 불변 : 이 클래스의 인스턴스는 마치 상수와 같아서 외부 동기화도 필요없다. String, Long, BigInteger가 대표적이다.
  * 무조건적 스레드 안전 : 이 클래스의 인스턴스는 수정될 수 있으나 내부에서 충실히 동기화하여 별도의 외부 동기화 없이 동시에 사용해도 안전하다. AtomicLong, ConcurrentHashMap이 여기에 속한다.
  * 조건부 스레드 안전 : 무조건적 스레드 안전과 같으나 일부 메서드는 동시에 사용하려면 외부 동기화가 필요하다. Collections.synchronized 래퍼 메서드가 반환한 컬렉션들이 여기에 속한다.
  * 스레드 안전하지 않음 : 이 클래스의 인스턴스는 수정될 수 있고, 동시에 사용하려면 각각의 메서드 호출을 클라이언트가 선택한 외부 동기화 매커니즘으로 감싸야 한다. ArrayList, HashMap 같은 기본 컬렉션이 여기 속한다.
  * 스레드 적대적 : 이 클래스는 모든 메서드 호출을 외부 동기화로 감싸더라도 멀티스레드 환경에서 안전하지 않다.
  * 이 분류는 스레드 안전성 애너테이션(@Immutable, @ThreadSafe, @NotThreadSafe)과 대략 일치한다. 무조건적 스레드 안전과 조건부 스레드 안전은 모두 @ThreadSafe 애너테이션 밑에 속한다.
  
* 조건부 스레드 안전한 클래스는 주의해서 문서화해야 한다.
  * 어떤 순서로 호출할 때 외부 동기화가 필요한지, 그리고 그 순서로 호출하려면 어떤 락 혹은 락들을 얻어야 하는지 알려줘야 한다. 
  * 일반적으로 인스턴스 자체를 락으로 얻는다.
  * 클래스의 스레드 안전성은 보통 클래스의 문서화 주석에 기재하지만, 독특한 특성의 메서드라면 해당 메서드의 주석에 기재한다.
  * 열거 타입은 굳이 불변이라고 쓰지 않아도 된다.
  * 반환 타입만으로는 명확히 알 수 없는 정적 팩토리라면 자신이 반환하는 객체의 스레드 안전성을 반드시 문서화해야 한다.
  
* 클래스가 외부에서 사용할 수 있는 락을 제공하면 클라이언트에서 일련의 메서드 호출을 원자적으로 수행할 수 있다.
  * 하지만 내부에서 처리하는 고성능 동시성 제어 매커니즘과 혼용할 수 없게 된다.
  * 또한 클라이언트가 공개된 락을 오래 쥐고 놓지 않는 서비스 거부 공격(denial-of-service attack)을 수행할 수도 있다.
  * 서비스 거부 공격을 막으려면 synchronized 메서드 대신 비공개 락 객체를 사용해야 한다.
  
```
private final Object lock = new Object();

public void foo() {
    synchronized(lock) {
        ...
    }
}
```

* 비공개 락 객체는 클래스 바깥에서 볼 수 없어 클라이언트가 그 객체의 동기화에 관여할 수 없다.
  * 단, 락 필드는 항상 final로 선언한다. 락 객체가 교체되는 일이 발생할 수도 있기 때문이다.
  * 무조건적 스레드 안전 클래스에서만 사용할 수 있다.
  * 조건부 스레드 안전 클래스에서는 특정 호출 순서에 필요한 락이 무엇인지를 클라이언트에게 알려줘야 하므로 이 관용구를 사용할 수 없다.
  * 상속용으로 설계한 클래스에 특히 잘 맞는다. 상속용 클래스에서 자신의 인스턴스를 락으로 사용하면 하위 클래스에서 아주 쉽게, 그리고 의도치 않게 기반 클래스의 동작을 방해할 수 있기 때문이다.