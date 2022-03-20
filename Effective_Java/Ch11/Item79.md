# 과도한 동기화는 피하라

* 과도한 동기화는 성능을 떨어뜨리고, 교착상태에 빠트리며, 심지어 예측할 수 없는 동작을 낳기도 한다.

* 응답 불가와 안전 실패를 피하려면 동기화 메서드나 동기화 블록 안에서는 제어를 절대로 클라이언트에 양도하면 안 된다.
  * 동기화된 영역 안에서는 재정의할 수 있는 메서드는 호출하면 안 된다.
  * 클라이언트가 넘겨준 함수 객체를 호출해서는 안 된다.
  * 이러한 메서드가 무슨 일을 할지 알 수 없기 때문이다.
  
```
public class ObservableSet<E> extends ForwardingSet<E> {
    public ObservableSet(Set<E> set) { super(set); }
    
    private final List<SetObserver<E>> observers = new ArrayList<>();
    
    public void addObserver(SetObserver<E> observer) {
        synchronized(observers) {
            observers.add(observer);
        }
    }
    
    public boolean removeObserver(SetObserver<E> observer) {
        synchronized(observers) {
            return observers.remove(observer);
        }
    }
    
    private void notifyElementAdded(E element) {
        synchronized(observers) {
            for (SetObserver<E> observer : observers) observer.added(this.element);
        }
    }
    
    @Override public boolean add(E element) {
        boolean added = super.add(element);
        if (added) notifyElementAdded(element);
        return added;
    }
    
    @Override public boolean addAll(Colleciton<? extends E> c) {
        boolean result = false;
        for (E element : c) result |= add(element); // notifyElementAdded를 호출한다.
        return result;
    }
}

@FunctionalInterface public interface SetObserver<E> {
    // ObservableSet에 원소가 더해지면 호출된다.
    void added(ObservableSet<E> set, E element);
}
```

* 위 코드에 대한 설명
  * 관찰자들은 addObserver와 removeObserver 메서드를 호출해 구독을 신청하거나 해지한다.
  * 두 경우 모두 아래에 있는 콜백 인터페이스의 인스턴스를 메서드에 건넨다.
  * 아래의 코드는 0부터 99까지를 출력한다.
```
public static void main(String[] args) {
    ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());
    
    set.addObserver((s, e) -> System.out.println(e));
    
    for (int i = 0; i < 100; i++) {
        set.add(i);
    }
}
```
  * 다만, 만약 아래와 같이 set.addObserver 부분을 바꾸면 23까지 출력한 뒤 ConcurrentModificationException을 던진다.
```
set.addObserver(new SetObserver<>() {
    public void added(ObservableSet<Integer> s, Integer e) {
        System.out.println(e);
        if (e == 23) s.removeObserver(this);
    }
});
```
  * 관찰자의 added 메서드 호출이 일어난 시점이 notifyElementAdded가 관찰자들의 리스트를 순회하는 도중이기 때문이다.
  * added 메서드는 ObservableSet의 removeObserver 메서드를 호출하고, 이 메서드는 다시 observers.remove 메서드를 호출하는데, 리스트를 순회하는 도중에 리스트에서 원소를 제거하려 했기에 문제가 발생한 것이다.
  * notifyElementAdded 메서드에서 수행하는 순회는 동기화 블록 안에 있으므로 동시 수정이 일어나지 않도록 보장하지만, 정작 자신이 콜백을 거쳐 되돌어와 수정하는 것까지 막지는 못한다.
  * 이를 해결하기 위해 removeObserver를 직접 호출하지 않고 실행자 서비스를 사용해 다른 스레드에게 부탁하면 교착 상태에 빠진다.

* 만약 똑같은 상황이지만 불변식이 임시로 깨진 경우
  * 자바 언어의 락은 재진입을 허용하므로 교착 상태에 빠지지는 않는다.
  * 다만 이로 인해 응답 불가가 될 상황을 안전 실패로 변모시킬 수도 있다.
  
* 위 문제는 메서드 호출을 동기화 블록 바깥으로 옮기면 된다.
  * 이를 열린 호출(open call)이라 한다.
  * notifyElementAdded 메서드에서라면 관찰자 리스트를 복사해 쓰면 락 없이도 안전하게 순회할 수 있다.
  * 이는 예외 발생과 교착 상태 증상을 없애준다.
  
```
private void notifyElementAdded(E element) {
    List<SetObserver<E>> snapshot = null;
    synchronized(observers) {
        snapshot = new ArrayList<>(observers);
    }
    for (SetObserver<E> observer : snapshot) {
        observer.added(this, element);
    }
}
```

* 자바의 동시성 컬렉션 라이브러리의 CopyOnWriteArrayList를 사용하면 메서드 호출을 동기화 블록 바깥으로 옮길 수 있다.
  * ArrayList를 구현한 클래스로, 내부를 변경하는 작업은 항상 깨끗한 복사본을 만들어 수행하도록 구현했다.
  * 수정할 일이 드물고 순회만 빈번히 일어나는 관찰자 리스트 용도로 최적이다.
  
```
private final List<SetObserver<E>> observers = new CopyOnWriteArrayList<>();
    
public void addObserver(SetObserver<E> observer) {
    observers.add(observer);
}
    
public boolean removeObserver(SetObserver<E> observer) {
    return observers.remove(observer);
}
    
private void notifyElementAdded(E element) {
    for (SetObserver<E> observer : observers) observer.added(this.element);
}
```

* 기본 규칙은 동기화 영역에서는 가능한 한 일을 적게 하는 것이다.
  * 락을 얻고, 공유 데이터를 검사하고, 필요하면 수정하고, 락을 놓는다.
  * 오래 걸리는 작업이라면 동기화 영역 바깥으로 옮기는 방법을 찾아본다.
  
* 과도한 동기화로 인해 비용이 증가할 수 있다.
  * 병렬로 실행할 기회를 잃고, 모든 코어가 메모리를 일관되게 보기 위한 지연시간이 이에 해당한다.
  * 가상머신의 코드 최적화를 제한한다.

* 가변 클래스를 작성할 때 두 선택지 중 하나를 따른다.
  * 동기화를 전혀 하지 마록, 그 클래스를 동시에 사용해야 하는 클래스가 외부에서 알아서 동기화하게 한다.
  * 동기화를 내부에서 수행해 스레드 안전한 클래스로 만든다. 단, 클라이언트가 외부에서 객체 전체에 락을 거는 것보다 동시성을 월등히 개선할 수 있을 때만 시도한다.
  * java.util은 첫 번째 방식을, java.util.concurrent는 두 번째 방식을 취했다.
  
* 클래스를 내부에서 동기화하기로 했다면, 락 분할(lock splitting), 락 스트라이핑(lock striping), 비차단 동시성 제어(nonblocking concurrency control) 등 다양한 기법을 통해 동시성을 높여줄 수 있다.

* 여러 스레드가 호출할 가능성이 있는 메서드가 정적 필드를 수정한다면 그 필드를 사용하기 전에 반드시 동기화해야 한다.
  * 그러나 클라이언트가 여러 스레드로 복제돼 구동되는 상황이라면 다른 클라이언트에서 이 메서드를 호출하는 것을 막을 수 없으니 외부에서 동기화할 방법이 없다.
  * 결과적으로, 이 정적 필드가 심지어 private라도 서로 관련 없는 스레드들이 동시에 읽고 수정할 수 있게 된다.