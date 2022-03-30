# readObject 메서드는 방어적으로 작성하라

* 불변인 날짜 범위 클래스를 만들 때 가변인 Date 필드를 사용하는 경우
  * 불변식을 지키고 불변을 유지하기 위해 생성자와 접근자에서 Date 객체를 방어적으로 복사해야 한다.
  
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
        this.start = new Date(start.getTime());
        this.end = new Date(end.getTime());
        if (this.start.compareTo(this.end) > 0) {
            throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
        }
    }
    
    public Date start() { return new Date(start.getTime()); }
    public Date end() { return new Date(end.getTime()); }
    public String toString() { return start + "..." + end; }
    
    ... // 나머지 코드는 생략
}
```

* 이 클래스를 직렬화하는 경우
  * Period 객체의 물리적 표현이 논리적 표현과 부합하므로 기본 직렬화 형태를 사용해도 나쁘지 않다.
  * 하지만 이 클래스 선언에 implements Serializable을 추가하면 이 클래스의 주요한 불변식을 더는 보장하지 못하게 된다.
  * readObject 메서드가 실질적으로 또 다른 public 생성자이기 때문이다. 
  * 따라서 다른 생성자와 똑같은 수준으로 주의를 기울여야 한다.
    * 인수가 유효한지 검사해야 하고 필요하다면 매개변수를 방어적으로 복사해야 한다.

* readObject는 매개변수로 바이트 스트림을 받는 생성자다.
  * 보통의 경우 바이트 스트림은 정상적으로 생성된 인스턴스를 직렬화해 만들어진다.
  * 하지만 불변식을 깨뜨릴 의도로 임의 생성한 바이트 스트림을 건네면 문제가 발생한다. 정상적으로 생성자로는 만들어낼 수 없는 객체를 생성해낼 수 있기 때문이다.
  
* 이를 해결하기 위해서는 Period의 readObject 메서드가 defaultReadObject를 호출한 다음 역직렬화된 객체가 유효한지 검사해야 한다.
  * 이 유효성 검사에 실패하면 InvalidObjectException을 던져 잘못된 역직렬화가 일어나는 것을 막을 수 있다.
  
```
private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    
    // 불변식을 만족하는지 검사한다.
    if (start.compareTo(end) > 0) {
        throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
    }
}
```

* 위 작업으로 공격자가 허용되지 않는 Period 인스턴스를 생성하는 일을 막을 수 있지만, 아직 문제가 하나 숨어 있다.
  * 정상 Period 인스턴스에서 시작된 바이트 스트림 끝에 private Date 필드로의 참조를 추가하면 가변 Period 인스턴스를 만들어낼 수 있다.
  * 공격자는 ObjectInputStream에서 Period 인스턴스를 읽은 후 스트림 끝에 추가된 이 악의적인 객체 참조를 읽어 Period 객체의 내부 정보를 얻을 수 있다.
  * 이제 이 참조로 얻은 Date 인스턴스들을 수정할 수 있으니 Period 인스턴스는 더는 불변이 아니게 된다.
  
> 가변 공격 예시
```
public class MutablePeriod {
    // Period 인스턴스
    public final Period period;
    
    // 시작 시각 필드 - 외부에서 접근할 수 없어야 한다.
    public final Date start;
    
    // 종료 시각 필드 - 외부에서 접근할 수 없어야 한다.
    public final Date end;
    
    public MutablePeriod() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOuputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            
            // 유효한 Period 인스턴스를 직렬화한다.
            out.writeObject(new Period(new Date(), new Date()));
            
            /**
             * 악의적인 이전 객체 참조, 즉 내부 Date 필드로의 참조를 추가한다.
             */
            byte[] ref = { 0x71, 0, 0x7e, 0, 5 };
            bos.write(ref); // 시작 필드
            ref[4] = 4;
            bos.write(ref); // 종료 필드
            
            // Period 역직렬화 후 Date 참조를 훔친다.
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            period = (Period) in.readObject();
            start = (Date) in.readObject();
            end = (Date) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
}

public static void main(String[] args) {
    MutablePeriod mp = new MutablePeriod();
    Period p = mp.period;
    Date pEnd = mp.end;
    
    // 시간을 되돌린다.
    pEnd.setYear(78);
    System.out.println(p);
    
    // 60년대로 변경
    pEnd.setYear(69);
    System.out.println(p);
}
```

* 위 문제는 Period의 readObject 메서드가 방어적 복사를 충분히 하지 않아서 발생한다.
  * 객체를 역직렬화할 때는 클라이언트가 소유해서는 안 되는 객체 참조를 갖는 필드를 모두 방어적으로 복사해야 한다.
  * 따라서 readObject에서는 불변 클래스 안의 모든 private 가변 요소를 방어적으로 복사해야 한다.
  
```
private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    
    // 가변 요소들을 방어적으로 복사한다.
    start = new Date(start.getTime());
    end = new Date(end.getTime());
    
    // 불변식을 만족하는지 검사한다.
    if (start.compareTo(end) > 0) {
        throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
    }
}
```

* 방어적 복사를 유효성 검사보다 앞서 수행하고, Date의 clone 메서드는 사용하지 않는다.
  * Period를 공격으로부터 보호하는 데 필요하다.
  * final 필드는 방어적 복사가 불가능하므로 readObject 메서드를 사용하려면 start와 end 필드에서 final 한정자를 제거해야 한다.
  
* 기본 readObject 메서드를 써도 좋을지 판단하는 방법
  * transient 필드를 제외한 모든 필드의 값을 매개변수로 받아 유효성 검사 없이 필드에 대입하는 public 생성자를 추가해도 되는지 생각해 보면 된다.
  * 만약 추가할 수 없다면 커스텀 readObject 메서드를 만들어 모든 유효성 검사와 방어적 복사를 수행해야 한다.
  * 혹은 직렬화 프록시 패턴을 사용하는 방법도 있다. 이 패턴은 역직렬화를 안전하게 만드는 데 필요한 노력을 상당 부분 경감해준다.
  
* final이 아닌 직렬화 가능 클래스라면 readObject와 생성자는 재정의 가능 메서드를 호출해서는 안 된다.
  * 이 규칙을 어긴 상황에서 해당 메서드가 재정의되면 하위 클래스의 상태가 완전히 역직렬화되기 전에 하위 클래스에서 재정의된 메서드가 실행되고, 결국 프로그램 오작동으로 이어질 것이다.
