# 이왕이면 제네릭 타입으로 만들라

> 단순한 스택 코드
```
public class Stack {
    private Object[] elements;
    private int size;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    
    public void push(Object o) {
        ensureCapacity();
        elements[size++] = o;
    }
    
    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        Object result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private void ensureCapacity() {
        if (elements.length == size) elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

* 위 클래스를 제네릭 타입으로 변환한다.

* 먼저 클래스 선언에 타입 매개변수를 추가한다.
  * 그런 다음 코드에 쓰인 Object를 적절한 타입 매개변수로 바꾼다.
  
```
public class Stack<E> {
    private E[] elements;
    private int size;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY]; // (1) E와 같은 실체화 불가 타입으로는 배열을 만들 수 없다.
    }
    
    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }
    
    public E pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        E result = elements[--size];
        elements[size] = null; // 다 쓴 참조 해제
        return result;
    }
    
    ...
}
```

* 위 코드에서 (1) 문제를 해결하기 위해서는 두 가지 방법이 있다.
  * 제네릭 배열 생성을 금지하는 제약을 대놓고 우회한다. 즉, Object 배열을 생성한 다음 제네릭 배열로 형변환한다. 이와 같은 방법은 일반적으로 타입 안전하지 않지만 문제의 배열 elements는 private 필드에 저장되고, 클라이언트로 반환되거나 다른 메서드에 전달되는 일이 전혀 없으며, push 메서드만을 통해 E 타입의 원소만 배열에 저장된다. 따라서 이 비검사 형변환은 확실히 안전하므로 @SuppressWarnings 애너테이션으로 해당 경고를 숨긴다.
  * elements 필드 타입을 E[]에서 Object[]로 바꾼다. 이 경우 pop 메서드에서 elements 배열로부터 원소를 가져와 result에 저장할 때 명시적 형변환이 필요하다. 하지만 이 경우 역시 E가 실체화 불가 타입이라 경고가 뜨는데, 위에서 설명했던 것처럼 절대 배열에 E 타입 외 다른 타입의 원소가 들어올 일이 없으므로 경고를 숨긴다.
  
> 해결 방법 두 가지
```
// 첫 번째 방법
@SuppressWarnings("unchecked")
public Stack() {
    elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
}

// 두 번째 방법
private Object[] elements;

...

public E pop() {
    if (size == 0) {
        throw new EmptyStackException();
    }
    @SuppressWarnings("unchecked") E result = (E) elements[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```

* 제네릭 배열 생성을 제거하는 두 방법 모두 나름의 지지를 얻고 있다.
  * 첫 번째 방법은 가독성이 더 좋고, 배열의 타입을 E[]로 선언해 오직 E 타입 인스턴스만 받음을 확실히 어필한다. 
  * 형변환을 배열 생성 시 단 한 번만 해도 된다. 
  * 하지만 배열의 런타임 타입이 컴파일타임 타입과 달라 힙 오염을 일으킨다.

* 지금까지의 Stack 예는 "배열보다는 리스트를 우선하라"는 아이템 28과 모순돼 보인다. 
  * 제네릭 타입 안에서 리스트를 사용하는 게 항상 가능하지도, 꼭 더 좋은 것도 아니다. 
  * 자바가 리스트를 기본 타입으로 제공하지 않기에, ArrayList 같은 제네릭 타입도 결국은 기본 타입인 배열을 사용해 구현해야 한다.
  * HashMap 같은 제네릭 타입은 성능을 높이기 위해 배열을 사용하기도 한다.
  
* Stack 예처럼 대다수의 제네릭 타입은 타입 매개변수에 아무런 제약을 두지 않는다.
> Stack\<Object>, Stack\<int[]>, Stack\<List\<String>>, Stack 등 어떤 참조 타입으로도 Stack을 만들 수 있다.
  * 단, 기본 타입은 박싱된 기본 타입으로 사용해야 한다.
  * 타입 매개변수에 제약을 두는 제네릭 타입도 있다. 이를 한정적 타입 매개변수라고 한다.
  > class DelayQueue\<E extends Delayed> implements BlockingQueue\<E>에서 \<E extends Delayed>는 java.util.concurrent.Delayed의 하위 타입만 받는다는 의미다.