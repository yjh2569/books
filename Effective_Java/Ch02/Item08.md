# finalizer와 cleaner 사용을 피하라

* 자바는 finalizer와 cleaner를 객체 소멸자로 제공한다. 하지만 이들은 예측할 수 없고 상황에 따라 위험할 수 있어 불필요하다.
* 이들은 즉시 수행된다는 보장이 없어 제때 실행되어야 하는 작업은 절대 할 수 없다.
* 이들을 얼마나 신속히 수행할지는 가비지 컬렉터 알고리즘에 달렸으며, 이는 가비지 컬렉터 구현마다 다르다.

* 데이터베이스 같은 공유 자원의 영구 락 해제와 같은, 상태를 영구적으로 수정하는 작업에는 절대 이들에 의존해서는 안 된다.

* 이들은 심각한 성능 문제도 동반한다. 이들을 사용하면 객체 생성 후 가비지 컬렉터가 수거하기까지 걸리는 시간이 매우 길어진다.

* finalizer를 사용한 클래스는 finalizer 공격에 노출되어 심각한 보안 문제를 일으킬 수도 있다.

* 파일이나 스레드 등 종료해야 할 자원을 담고 있는 객체의 클래스에서 AutoCloseable을 구현하고, 인스턴스를 다 쓰면 close 메서드를 호출함으로써 이들을 대신할 수 있다.
  * 일반적으로 예외가 발생해도 제대로 종료되도록 try-with-resources를 사용해야 한다.
* 각 인스턴스는 자신이 닫혔는지를 추적하는 것이 좋다. 즉, close 메서드에서 이 객체는 더 이상 유효하지 않음을 필드에 기록하고, 다른 메서드는 이 필드를 검사해서 객체가 닫힌 후에 불렸다면 IllegalStateException을 던진다.

* cleaner와 finalizer의 적절한 쓰임새
  1. 자원의 소유자가 close 메서드를 호출하지 않는 것에 대비한 안전망 역할
  > FileInputStream, FileOutputStream, ThreadPoolExecutor에서 안전망 역할의 finalizer를 제공한다.
  2. 네이티브 피어(native peer)와 연결된 객체
    * 네이티브 피어 : 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브 객체
    * 네이티브 피어는 자바 객체가 아니기 때문에 가비지 컬렉터가 그 존재를 알지 못한다. 따라서 cleaner나 finalizer가 나서서 처리하기에 적당한 작업이다.
    
> cleaner를 안전망으로 활용하는 AutoCloseable 클래스
```
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    
    // 청소가 필요한 자원. 절대 Room 인스턴스를 참조해서는 안 된다.
    private static class State implements Runnable {
        int numJunkPiles; // Room 안의 쓰레기 수
        
        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }
        
        // close 메서드나 cleaner가 호출한다.
        @Override public void run() {
            System.out.println("방 청소");
            numJunkPiles = 0;
        }
    }
    
    // 방의 상태. cleanable과 공유한다.
    private final State state;
    
    // cleanable 객체. 수거 대상이 되면 방을 청소한다.
    private final Cleaner.Cleanable cleanable;
    
    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this, state);
    }
    
    @Override public void close() {
        cleanable.clean();
    }
}
```

* cleanable 객체는 Room 생성자에서 cleaner에 Room과 State를 등록할 때 얻는다.
* run 메서드는 cleanable에 의해 딱 한 번만 호출될 것이다. 일반적인 경우 Room의 close 메서드를 호출하거나, 가비지 컬렉터가 Room을 회수할 때까지 클라이언트가 close를 호출하지 않는다면 cleaner가 State의 run 메서드를 호출한다.
* State 인스턴스는 Room 인스턴스를 참조할 경우 순환참조가 생겨 가비지 컬렉터가 Room 인스턴스를 회수해갈 기회가 오지 않기에, 절대로 Room 인스턴스를 참조해서는 안 된다. 이는 State가 정적 중첩 클래스인 이유다.
