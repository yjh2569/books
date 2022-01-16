# clone 재정의는 주의해서 진행하라

* Cloneable 인터페이스는 Object의 protected 메서드인 clone의 동작 방식을 결정한다.
  * Cloneable을 구현한 클래스의 인스턴스에서 clone을 호출하면 그 객체의 필드들을 하나하나 복사한 객체를 반환한다.
  * Cloneable을 구현하지 않은 클래스의 인스턴스에서 clone을 호출하면 CloneNotSupportedException을 던진다.
  * 다만 이는 상위 클래스에 정의된 메서드의 동작 방식을 변경함으로써 인터페이스를 이례적으로 사용한 예이므로 적절하지 않다.(인터페이스는 보통 클래스가 인터페이스에서 정의한 기능을 제공한다고 선언하기 위해 사용한다.)

* clone 메서드의 일반 규약
  * 객체의 복사본을 생성해 반환한다.
  * 어떤 객체 x에 대해 다음 식은 참이다. 반드시 만족할 필요는 없다.
  * x.clone() != x
  * x.clone().getClass() == x.getClass()
  * x.clone().equals(x)
  * 관례상, clone이 반환하는 객체는 super.clone을 호출해 얻어야 한다. 클래스의 Object를 제외한 모든 상위 클래스가 이 관례를 따르면 다음 식은 참이다.
  * x.clone().getClass() == x.getClass()
  * 관례상, 반환된 객체와 원본 객체는 독립적이어야 한다. 이를 만족하려면 super.clone으로 얻은 객체의 필드 중 하나 이상을 반환 전에 수정해야 할 수도 있다.
  
* clone 메서드가 super.clone이 아닌, 생성자를 호출해 얻은 인스턴스를 반환해도 컴파일러는 예외를 발생시키지 않는다.
* 하지만 이 클래스의 하위 클래스에서 super.clone을 호출하면 잘못된 클래스의 객체가 만들어져 하위 클래스의 clone 메서드가 제대로 동작하지 않는다.

* 클래스의 모든 필드가 기본 타입이거나 불변 객체를 참조한다면 super.clone을 호출하여 원본의 복사본을 예외없이 얻을 수 있다.

> 가변 상태를 참조하지 않는 클래스의 clone 메서드

```
// PhoneNumber 클래스 선언에 Cloneable을 구현한다고 추가한다.
@Override public PhoneNumber clone() {
    try {
        return (PhoneNumber) super.clone(); // 형변환을 통해 clone 메서드가 PhoneNumber를 반환하게 한다.
    } catch (CloneNotSupportedException e) {
        throw new AssertionError(); // 일어날 수 없는 일이다. 다만, Object의 clone 메서드는 검사 예외인 CloneNotSupportedException을 던지도록 선언되었기에 어쩔 수 없이 추가한다.
    }
}
```

> 가변 객체를 참조하는 클래스 Stack

```
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    
    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }
    
    public Object pop() {
        if (size == 0) throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }
    
    // 원소를 위한 공간을 적어도 하나 이상 확보한다.
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

* 위 클래스에 대해 clone 메서드가 단순히 super.clone의 결과를 그대로 반환한다면, elements 필드는 원본 Stack 인스턴스와 똑같은 배열을 참조한다.
* clone 메서드는 원본 객체에 아무런 해를 끼치지 않는 동시에 복제된 객체의 불변식을 보장해야 한다.

> Stack 클래스의 clone 메서드

```
@Override public Stack clone() {
    try {
        Stack result = (Stack) super.clone();
        result.elements = elements.clone(); // elements 배열의 clone을 재귀적으로 호출한다.
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError(); 
    }
}
```

* 배열의 clone은 런타임 타입과 컴파일타임 타입 모두가 원본 배열과 똑같은 배열을 반환하기 때문에 Object[]로 형변환할 필요는 없다.
* 만약 elements 필드가 final이었다면 새로운 값을 할당할 수 없기 때문에 위 방식은 불가능하다. 이는 '가변 객체를 참조하는 필드는 final로 선언하라'는 일반 용법과 충돌한다.(원본과 복제된 객체가 가변 객체를 공유해도 안전하다면 괜찮다.)
* 따라서 일부 필드에서 final을 제거해야 할 수도 있다.

> HashTable 클래스

```
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;
    
    // Entry는 해시테이블 내 버킷(연결 리스트 형태)의 첫 번째 엔트리다.
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
        
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    ... // 나머지 코드 생략
}
```

* 이에 대해 Stack과 같은 방식으로 clone 메서드를 구현하면 buckets 내 각 Entry가 같은 연결 리스트를 참조한다. 따라서 연결 리스트 역시 복사해야 한다.

> clone 메서드를 구현한 HashTable 클래스

```
public class HashTable implements Cloneable {
    private Entry[] buckets = ...;
    
    // Entry는 해시테이블 내 버킷(연결 리스트 형태)의 첫 번째 엔트리다.
    private static class Entry {
        final Object key;
        Object value;
        Entry next;
        
        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
        
        // 이 엔트리가 가리키는 연결 리스트를 재귀적으로 복사
        Entry deepCopy() {
            return new Entry(key, value, next == null? null : next.deepCopy());
        }
    }
    
    @Override public HashTable clone() {
        try {
            HashTable result = (HashTable) super.clone();
            result.buckets = new Entry[buckets.length];
            for (int i = 0; i < buckets.length; i++) {
                if (buckets[i] != null) {
                    result.buckets[i] = buckets[i].deepCopy();
                }
            }
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); 
        }
    }
    ... // 나머지 코드 생략
}
```

* 위 예시의 deepCopy는 버킷이 길지 않으면 괜찮으나 리스트의 원소 수만큼 스택 프레임을 소비하기 때문에 스택 오버플러를 일으킬 위험이 있다.
* 따라서 재귀 호출 대신 반복자를 써서 순회하도록 수정한다.

```
Entry deepCopy() {
    Entry result = new Entry(key, value, next);
    for (Entry p = result; p.next != null; p = p.next) {
        p.next = new Entry(p.next.key, p.next.value, p.next.next);
    }
    return result;
}
```

* clone 메서드에서는 재정의될 수 있는 메서드를 호출해서는 안 된다. 하위 클래스에서 재정의된 메서드를 호출할 경우 복제 과정에서 자신의 상태를 교정할 기회를 잃게 돼 원본과 복제본의 상태가 달라질 가능성이 크기 때문이다.
* 따라서 메서드 호출 시 final 메서드나 private 메서드를 호출하도록 한다.
* Object의 clone 메서드는 CloneNotSupportedException을 던진다고 선언했지만 재정의한 메서드는 그렇지 않기 때문에 public인 clone 메서드에서는 throws 절을 없애야 한다.

* 상속용 클래스에서는 Cloneable을 구현해서는 안 된다.
  * 이를 위해 제대로 작동하는 clone 메서드를 구현해 protected로 두고 CloneNotSupportedException도 던질 수 있다고 선언하거나, clone을 동작하지 않게 구현해놓고 하위 클래스에서 재정의하지 못하게 할 수도 있다.
  
> 하위 클래스에서 Cloneable을 지원하지 못하게 하는 clone 메서드

```
@Override
protected final Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
}
```

* Cloneable을 구현한 스레드 안전 클래스 작성 시 clone 메서드 역시 적절히 동기화해줘야 한다.

* Cloneable을 구현하는 모든 클래스는 접근 제한자를 public, 반환 타입을 클래스 자신으로 하는 clone을 재정의해야 한다.
  * clone 메서드는 가장 먼저 super.clone을 호출한 후 필요한 필드를 적절히 수정한다.
  * 기본 타입 필드와 불변 객체 참조만 갖는 클래스라면 아무 필드도 수정할 필요는 없지만, 일련번호나 고유 ID는 수정해줘야 한다.
  
* clone을 구현하지 않은 경우 복사 생성자와 복사 팩토리를 통해 객체 복사를 할 수 있다.
  * 복사 생성자 : 자신과 같은 클래스의 인스턴스를 인수로 받는 생성자
  * 복사 팩토리 : 복사 생성자를 모방한 정적 팩토리
  
> 복사 생성자와 복사 팩토리

```
public Yum(Yum yum) { ... };
public static Yum newInstance(Yum yum) { ... };
```

* 복사 생성자와 복사 팩토리는 Cloneable/clone 방식보다 나은 면이 많다.
  * 언어 모순적이고 위험천만한 객체 생성 매커니즘(생성자를 사용하지 않는 방식)을 사용하지 않는다.
  * 엉성하게 문서화된 규약에 의존하지 않는다.
  * 정상적인 final 필드 용법과 충돌하지 않는다.
  * 불필요한 검사 예외를 던지지 않는다.
  * 형변환이 필요없다.
  * 해당 클래스가 구현한 인터페이스 타입의 인스턴스를 인수로 받을 수 있다. 인터페이스 기반 복사 생성자와 복사 팩토리는 변환 생성자와 변환 팩토리라고 한다. 이를 이용하면 원본의 구현 타입에 얽매이지 않고 복제본의 타입을 직접 선택할 수 있다.