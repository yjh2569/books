# 비검사 경고를 제거하라

* 제네릭을 사용하면 수많은 컴파일러 경고를 보게 된다.
  * 비검사 형변환 경고, 비검사 메서드 호출 경고, 비검사 매개변수화 가변인수 타입 경고, 비검사 변환 경고 등
  
* 간단한 경고부터 제거하기 훨씬 어려운 경고까지 여러 경고가 있지만 할 수 있는 한 모든 비검사 경고를 제거해야 한다.
  * 모든 경고를 해결하면 런타임에 ClassCastException이 발생할 일이 없고, 의도한 대로 잘 동작하게 된다.
  
* 경고를 제거할 수 없지만 타입 안전하다고 확신할 수 있으면 @SuppressWarnings("unchecked") 애너테이션을 달아 경고를 숨긴다.
  * @SuppressWarnings 애너테이션은 개별 지역변수 선언부터 클래스 전체까지 어떤 선언에도 달 수 있다.
  * 하지만 @SuppressWarnings 애너테이션은 항상 가능한 한 좁은 범위에 적용해야 한다.
  * 한 줄이 넘는 메서드나 생성자에 달린 @SuppressWarnings 애너테이션은 지역변수 선언 쪽으로 옮긴다.
  
> ArrayList의 toArray 메서드
```
public <T> T[] toArray(T[] a) {
    if (a.length < size) {
        return (T[]) Arrays.copyOf(elements, size, a.getClass());
    }
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size) {
        a[size] = null;
    }
    return a;
}
```

* 이를 컴파일하면 다음과 같은 경고가 발생한다.

```
ArrayList.java:305: warning: [unchecked] unchecked cast
        return (T[]) Arrays.copyOf(elements, size, a.getClass());
    required: T[]
    found:    Object[]
```

* 애너테이션은 선언에만 달 수 있기 때문에 return 문에는 @SuppressWarnings를 다는 게 불가능하다. 메서드에 달면 범위가 필요 이상으로 넓어지므로, 반환값을 담을 지역변수를 하나 선언하고 그 변수에 애너테이션을 달아준다.

```
public <T> T[] toArray(T[] a) {
    if (a.length < size) {
        @SuppressWarnings("unchecked") T[] result = (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size) {
        a[size] = null;
    }
    return a;
}
```

* @SuppressedWarnings("unchecked") 애너테이션을 사용할 떄면 그 경고를 무시해도 안전한 이유를 항상 주석으로 남겨야 한다.
  * 다른 사람이 그 코드를 이해하는 데 도움이 되며, 잘못 수정해 타입 안전성을 잃는 상황을 줄여준다.