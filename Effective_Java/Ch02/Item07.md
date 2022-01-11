# 다 쓴 객체 참조를 해제하라

* 자바는 가비지 컬렉터를 가지고 있기 때문에 메모리 관리를 자동으로 해준다. 하지만 그렇다고 해서 자바를 사용할 때 메모리 관리에 더 이상 신경 쓰지 않아도 되는 것은 아니다.

> 스택을 간단히 구현한 코드
```
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    
    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }
    
    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return elements[--size];
    }
    
    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보한다.
     * 배열 크기를 늘려야 할 때마다 대략 두 배씩 늘린다.
     */
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

* 위 코드에서는 스택이 커졌다가 줄어들었을 때 스택에서 꺼낸 객체들을 가비지 컬렉터가 회수하지 않는다. 스택이 그 객체들의 참조를 여전히 가지고 있기 때문이다.
* 객체 참조 하나를 살려두면 가비지 컬렉터는 그 객체뿐 아니라 그 객체가 참조하는 모든 객체를 회수해가지 못한다. 이로 인해 단 몇 개의 객체가 매우 많은 객체를 회수되지 못하게 할 수 있어 성능에 악영향을 줄 수 있다.
* 따라서 해당 참조를 다 쓰면 null 처리(참조 해제)를 해 줘야 한다.

> Stack의 pop 메서드를 메모리 누수를 막기 위해 수정한 코드
```
public Object pop() {
    if (size == 0) throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}

```

* 이를 통해 null 처리한 참조를 실수로 사용하려 해도 프로그램이 NullPointerException을 던지며 종료되기 때문에 부가적인 안전성을 더할 수 있다.
* 실제로 객체 참조를 null 처리하는 일은 예외적인 경우로 생각해야 하고, 일반적으로 다 쓴 참조를 해제하는 가장 좋은 방법은 그 참조를 담은 변수를 유효 범위 밖으로 밀어내는 것이다. 이는 변수의 범위를 최소가 되게 정의하면 자연스럽게 이뤄진다.
* 일반적으로 자기 메모리를 직접 관리하는 클래스라면 항시 메모리 누수에 주의해야 한다. Stack 예시처럼 원소를 다 사용한 즉시 그 원소가 참조한 객체들을 다 null 처리해줘야 한다.

* 캐시도 메모리 누수를 일으킨다. 객체 참조를 캐시에 넣어두고 그 객체를 다 쓴 뒤에도 남겨놓는 경우가 이에 해당한다.

* 리스너(listener)와 콜백(callback)도 메모리 누수를 일으킨다. 콜백을 등록만 하고 명확히 해지하지 않으면 조치하지 않는 한 콜백은 계속 쌓이게 된다.