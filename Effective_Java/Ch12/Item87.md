# 커스텀 직렬화 형태를 고려해보라

* 클래스가 Serializable을 구현하고 기본 직렬화 형태를 사용한다면 다음 릴리스 때 버리려 한 현재의 구현에 발이 묶일 수 있다.
  * 기본 직렬화 형태를 버릴 수 없기 때문이다.
  
* 따라서 괜찮다고 판단될 때만 기본 직렬화 형태를 사용한다.
  * 기본 직렬화 형태는 유연성, 성능, 정확성 측면에서 신중히 고민한 후 합당할 때만 사용해야 한다.
  * 직접 설계하더라도 기본 직렬화 형태와 거의 같은 결과가 나올 경우에만 기본 형태를 써야 한다.
  * 어떤 객체의 기본 직렬화 형태는 그 객체를 루트로 하는 객체 그래프의 물리적 모습을 나름 효율적으로 인코딩한다.
  * 객체가 포함한 데이터들과 그 객체로부터 시작해 접근할 수 있는 모든 객체를 담아내며, 심지어 이 객체들이 연결된 위상까지 기술한다.
  * 이상적인 직렬화 형태라면 물리적인 모습과 독립된 논리적인 모습만을 표현해야 한다.
  
* 객체의 물리적 표현과 논리적 내용이 같다면 기본 직렬화 형태라도 무방하다.

> 기본 직렬화 형태에 적합한 후보
```
public class Name implements Serializable {
    /**
     * 성. null이 아니어야 함.
     * @serial
     */
    private final String lastName;
    
    /**
     * 이름. null이 아니어야 함
     * @serial
     */
    private final String firstName;
     
    /**
     * 중간이름. 중간이름이 없다면 null.
     * @serial
     */
    private final String middleName;
    
    ... // 나머지 코드는 생략
}
```

* 기본 직렬화 형태가 적합하다고 결정했더라도 불변식 보장과 보안을 위해 readObject 메서드를 제공해야 할 때가 많다.
  * Name 클래스의 경우 readObject 메서드가 lastName과 firstName 필드가 null이 아님을 보장해야 한다.
  
> 기본 직렬화 형태에 적합하지 않은 클래스
```
public final class StringList implements Serializable {
    private int size = 0;
    private Entry head = null;
    private static class Entry implements Serializable {
        String data;
        Entry next;
        Entry previous;
    }
    
    ... // 나머지 코드는 생략
}
```

* 논리적으로 이 클래스는 일련의 문자열을 표현한다.
  * 물리적으로는 문자열들을 이중 연결 리스트로 연결했다.
  * 이 클래스에 기본 직렬화 형태를 사용하면 각 노드의 양방향 연결 정보를 포함해 모든 엔트리를 철두철미하게 기록한다.
  
* 객체의 물리적 표현과 논리적 표현의 차이가 클 때 기본 직렬화 형태를 사용하면 크게 네 가지 면에서 문제가 생긴다.
  * 공개 API가 현재의 내부 표현 방식에 영구히 묶인다.
    * StringList의 경우 Entry가 공개 API가 되어 내부 표현 방식을 바꾸더라도 StringList 클래스는 여전히 연결 리스트로 표현된 입력도 처리할 수 있어야 한다.
  * 너무 많은 공간을 차지할 수 있다.
    * 위 예시에서 엔트리와 연결 정보는 내부 구현에 해당하므로 직렬화 형태에 포함할 필요가 없다.
  * 시간이 너무 많이 걸릴 수 있다.
    * 직렬화 로직은 객체 그래프의 위상에 관한 정보가 없으니 그래프를 직접 순회해볼 수밖에 없다.
  * 스택 오버플로를 일으킬 수 있다.
    * 기본 직렬화 과정은 객체 그래프를 재귀 순회하는데, 이로 인해 스택 오버플로가 발생할 수 있다.
    
* StringList를 위한 합리적인 직렬화 형태
  * 리스트가 포함한 문자열의 개수를 적은 다음, 그 뒤로 문자열들을 나열하는 수준이면 된다.
  * 물리적인 상세 표현은 배제한 채 논리적인 구성만 담는다.
  * writeObject와 readObject가 직렬화 형태를 처리한다.
  
```
public final class StringList implements Serializable {
    private transient int size = 0;
    private transient Entry head = null;
    
    // 이제는 직렬화되지 않는다.
    private static class Entry {
        String data;
        Entry next;
        Entry previous;
    }
    
    // 지정한 문자열을 이 리스트에 추가한다.
    public final void add(String s) { ... }
    
    /** 
     * 이 {@code StringList} 인스턴스를 직렬화한다.
     * 
     * @serialData 이 리스트의 크기(포함된 문자열의 개수)를 기록한 후
     * ({@code int}), 이어서 모든 원소를(각각은 {@code String})
     * 순서대로 기록한다.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        
        // 모든 원소를 올바른 순서로 기록한다.
        for (Entry e = head; e != null; e = e.next) {
            s.writeObject(e.data);
        }
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int numElements = s.readInt();
        
        // 모든 원소를 읽어 이 리스트에 삽입한다.
        for (int i = 0; i < numElements; i++) {
            add((String) s.readObject());
        }
    }
    ... // 나머지 코드는 생략
```
* StringList의 필드 모두가 transient더라도 writeObject와 readObject는 각각 가장 먼저 defaultWriteObject와 defaultReadObject를 호출한다.
  * 이렇게 해야 향후 릴리스에서 transient가 아닌 인스턴스 필드가 추가되더라도 상호 호환되기 때문이다.
  * 신버전 인스턴스를 직렬화한 후 구버전으로 역직렬화하면 새로 추가된 필드들은 무시될 것이다.
  
* 해시테이블의 경우 
  * 물리적으로 키-값 엔트리들을 담은 해시 버킷을 차례로 나열한 형태다.
  * 어떤 엔트리를 어떤 버킷에 담을지는 키에서 구한 해시코드가 결정하는데, 그 계산 방식은 구현에 따라 달라질 수 있다.
  * 따라서 해시테이블에 기본 직렬화를 사용하면 심각한 버그로 이어질 수 있다.
  
* defaultWriteObject 메서드를 호출하면 transient로 선언하지 않은 모든 인스턴스 필드가 직렬화된다.
  * 따라서 transient로 선언해도 되는 인스턴스 필드에는 모두 transient 한정자를 붙여야 한다.
  * 캐시된 해시 값처럼 다른 필드에서 유도되는 필드도 여기 해당한다.
  * JVM을 실행할 때마다 값이 달라지는 필드도 마찬가지다.
  * 해당 객체의 논리적 상태와 무관한 필드라고 확신할 때만 transient 한정자를 생략해야 한다.
  
* 기본 직렬화를 사용한다면 transient 필드들은 역직렬화될 때 기본값으로 초기화된다.
  * 기본값을 그대로 사용해서는 안 된다면 readObject 메서드에서 defaultReadObject를 호출한 다음, 해당 필드를 원하는 값으로 복원한다. 혹은 그 값을 처음 사용할 때 초기화하는 방법도 있다.
  
* 객체의 전체 상태를 읽는 메서드에 적용해야 하는 동기화 매커니즘을 직렬화에도 적용해야 한다.
  * 모든 메서드를 synchronized로 선언하여 스레드 안전하게 만든 객체에서 기본 직렬화를 사용하려면 writeObject도 synchronized로 선언해야 한다.
  * writeObject 메서드 안에서 동기화하고 싶다면 클래스의 다른 부분에서 사용하는 락 순서를 똑같이 따라야 한다.
  
* 어떤 직렬화 형태를 택하든 직렬화 가능 클래스 모두에 직렬 버전 UID를 명시적으로 부여한다.
  * 이렇게 하면 직렬 버전 UID가 일으키는 잠재적인 호환성 문제가 사라진다.
  * 직렬 버전 UID를 명시하면 런타임에 이 값을 생성하기 위해 복잡한 연산을 하지 않아도 돼 성능이 조금 나아진다.
  * 어떤 long 값을 선택해도 상관없다. 고유할 필요도 없다.
  * 직렬 버전 UID가 없는 기존 클래스를 구버전으로 직렬화된 인스턴스와 호환성을 유지한 채 수정하고 싶다면 구버전에서 사용한 자동 생성된 값을 그대로 사용해야 한다.
  
* 기본 버전 클래스와의 호환성을 끊고 싶다면 단순히 직렬 버전 UID의 값을 바꿔주면 된다.
  * 이렇게 하면 기존 버전의 직렬화된 인스턴스를 역직렬화할 때 InvalidClassException이 던져질 것이다.
  * 구버전으로 직렬화된 인스턴스들과의 호환성을 끊으려는 경우를 제외하고는 직렬 버전 UID를 절대 수정하지 않는다.