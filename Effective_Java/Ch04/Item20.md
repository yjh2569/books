# 추상 클래스보다는 인터페이스를 우선하라

* 추상 클래스가 정의한 타입을 구현하는 클래스는 반드시 추상 클래스의 하위 클래스가 되어야 해서 새로운 타입을 정의하는데 커다란 제약이 생긴다.
* 반면 인터페이스가 선언한 메서드를 모두 저으이하고 그 일반 규약을 잘 지킨 클래스라면 다른 어떤 클래스를 상속했든 같은 타입으로 취급된다.

* 기존 클래스에도 손쉽게 새로운 인터페이스를 구현해 넣을 수 있다.
  * 인터페이스가 요구하는 메서드를 추가하고 클래스 선언에 implements 구문을 추가하면 된다.
  * 반면 기존 클래스 위에 새로운 추상 클래스를 끼워넣을려면 계층 구조상 두 클래스의 공통 조상으로 만들어야 하고, 이로 인해 추상 클래스의 모든 자손이 이를 상속하게 된다.
  
* 인터페이스는 믹스인(mixin) 정의에 좋다.
  * 믹스인 : 클래스가 구현할 수 있는 타입으로 믹스인을 구현한 클래스에 원래의 '주된 타입' 외에도 특정 선택적 행위를 제공한다고 선언하는 효과를 준다.
  > Comparable은 자신을 구현한 클래스의 인스턴스들끼리 순서를 정할 수 있다고 선언하는 믹스인 인터페이스다.
  * 추상 클래스의 경우 다중 상속이 불가능하고, 클래스 계층구조에서 믹스인을 삽입하기에 합리적인 위치가 없다. 따라서 추상 클래스는 믹스인을 정의할 수 없다.
  
* 인터페이스로는 계층 구조가 없는 타입 프레임워크를 만들 수 있다.

> 가수와 작곡가의 경우 계층을 구분하기 어려운데, 이를 인터페이스로 만든다. 싱어송라이터 클래스를 만들 때 가수와 작곡가 인터페이스를 구현하도록 만들면 된다.

* 래퍼 클래스 관용구와 함께 사용하면 인터페이스는 기능을 향상시키는 안전하고 강력한 수단이 된다.

* 인터페이스의 메서드 중 구현 방법이 명백한 것이 있다면 그 구현을 디폴트 메서드로 제공한다.
  * 디폴트 메서드 : default 제어자를 추가한, 인터페이스 내에서 이미 정의가 된 메서드
  * 디폴트 메서드 제공 시 상속하려는 사람을 위한 설명을 @implSpec 자바독 태그를 붙여 문서화한다.
  * equals나 hashCode를 디폴트 메서드로 제공해서는 안 된다.
  * 인터페이스는 인스턴스 필드를 가질 수 없고 public이 아닌 정적 멤버도 가질 수 없다.(private 정적 메서드는 예외)
  * 직접 만들지 않은 인터페이스에 디폴트 메서드를 추가할 수 없다.
  
* 템플릿 메서드 패턴
  * 인터페이스와 추상 골격 구현 클래스를 함께 제공하는 방법
  * 인터페이스로는 타입을 정의하고(디폴트 메서드도 제공), 골격 구현 클래스는 나머지 메서드들까지 구현한다.
  * 인터페이스 이름이 Interface라면 골격 구현 클래스의 이름은 AbstractInterface로 짓는다.
  
> 골격 구현을 사용해 완성한 구체 클래스
```
// List 구현체를 반환하는 정적 팩토리 메서드
static List<Integer> intArrayAsList(int[] a) {
    Objects.requireNonNull(a);
    
    // 익명 클래스를 사용했다.
    return new AbstractList<>() {
        @Override public Integer get(int i) {
            return a[i]; // 오토박싱
        }
        
        @Override public Integer set(int i, Integer val) {
            int oldVal = a[i];
            a[i] = val; // 오토언박싱
            return oldVal; // 오토박싱
        }
        
        @Override public int size() {
            return a.length;
        }
    }
}
```

* 구조상 골격 구현을 확장하지 못한다면 인터페이스를 직접 구현해야 한다.
* 골격 구현 클래스를 우회적으로 이용할 수도 있다.
  * 시뮬레이트한 다중 상속 : 인터페이스를 구현한 클래스에서 해당 골격 구현을 확장한 private 내부 클래스를 정의하고, 각 메서드 호출을 내부 클래스의 인스턴스에 전달한다.

* 골격 구현 작성법
  * 인터페이스에서 다른 메서드들의 구현에 사용되는 기반 메서드들을 선정한다. 이 기반 메서드들은 골격 구현에서는 추상 메서드가 될 것이다.
  * 기반 메서드들을 사용해 직접 구현할 수 있는 메서드를 모두 디폴트 메서드로 제공한다.
  * 기반 메서드나 디폴트 메서드로 만들지 못한 메서드가 남아 있으면 인터페이스를 구현하는 골격 구현 클래스를 하나 만들어 남은 메서드들을 작성해 넣는다.
  
> 골격 구현 클래스 예시
```
public abstract class AbstractMapEntry<K, V> implements Map.Entry<K, V> {
    
    // 변경 가능한 엔트리는 이 메서드를 반드시 재정의해야 한다.
    @Override public V setValue(V value) {
        throw new UnsupportedOperationException();
    }
    
    // Map.Entry.equals의 일반 규약을 구현한다.
    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry<?, ?> e = (Map.Entry) o;
        return Objects.equals(e.getKey(), getKey()) && Objects.equals(e.getValue(), getValue());
    }
    
    // Map.Entry.hashCode의 일반 규약을 구현한다.
    @Override public int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }
    
    @Override public String toString() {
        return getKey() + "=" + getValue();
    }
}
```

* Map.Entry는 getKey, getValue(, setValue)는 기반 메서드이므로 추상 메서드이고, equals, hashCode, toString은 인터페이스에서 정의할 수 없으므로 골격 구현 클래스에 정의했다.

* 단순 구현(simple implementation) : 골격 구현의 변종으로 상속을 위해 인터페이스를 구현한 것이지만 추상 클래스가 아니다. 그래서 그대로 써도 되고 필요에 따라 확장해도 된다.