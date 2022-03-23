# wait와 notify보다는 동시성 유틸리티를 애용하라

* wait와 notify는 올바르게 사용하기 아주 까다롭기에 고수준 동시성 유틸리티를 사용한다.

* java.util.concurrent의 고수준 유틸리티는 세 범주로 나눌 수 있다.
  * 실행자 프레임 워크
  * 동시성 컬렉션
  * 동기화 장치

* 동시성 컬렉션
  * 표준 컬렉션 인터페이스에 동시성을 가미해 구현한 고성능 컬렉션
  * 높은 동시성에 도달하기 위해 동기화를 각자의 내부에서 수행한다.
  * 따라서 동시성 컬렉션에서 동시성을 무력화하는 것은 불가능하고, 외부에서 락을 추가로 사용하면 오히려 속도가 느려진다.
  * 동시성을 무력화하지 못하므로 여러 메서드를 원자적으로 묶어 호출하는 일 역시 불가능하다.
  * 그래서 여러 기본 동작을 하나의 원자적 동작으로 묶는 상태 의존적 수정 메서드들이 추가되었다.
  * 이 메서드들은 자바 8 이후로 일반 컬렉션 인터페이스에도 디폴트 메서드로 추가되었다.
  
> Map의 putIfAbsent(key, value) 메서드는 주어진 키에 매핑된 값이 아직 없을 때만 새 값을 집어넣는다. 기존 값이 있었다면 그 값을 반환하고, 없었다면 null을 반환한다. 이 메서드 덕에 스레드 안전한 정규화 맵을 쉽게 구현할 수 있다.
```
private static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

public static String intern(String s) {
    String previousValue = map.putIfAbsent(s, s);
    return previousValue == null ? s : previousValue;
}

// 위 메서드를 개선한 결과
public static String intern(String s) {
    String result = map.get(s);
    if (result == null) {
        result = map.putIfAbsent(s, s);
        if (result == null) {
            result = s;
        }
    }
    return result;
}
```

* 위 예시처럼 동기화된 맵을 동시성 맵으로 교체하는 것만으로 동시성 애플리케이션의 성능은 극적으로 개선된다.

* 컬렉션 인터페이스 중 일부는 작업이 성공적으로 완료될 때까지 기다리도록 확장되었다.
  * Queue를 확장한 BlockingQueue에 추가된 메서드 중 take는 큐의 첫 원소를 꺼낸다.
  * 이때 만약 큐가 비었다면 새로운 원소가 추가될 때까지 기다린다.
  * 이러한 특성 덕에 작업 큐(생산자-소비자 큐)로 쓰기 적합하다.
  * 작업 큐는 하나 이상의 생산자 스레드가 작업을 큐에 추가하고, 하나 이상의 소비자 스레드가 큐에 있는 작업을 꺼내 처리하는 형태다.
  * 대부분의 실행자 서비스 구현체에서 BlockingQueue를 사용한다.
  
* 동기화 장치
  * 스레드가 다른 스레드를 기다릴 수 있게 하여, 서로 작업을 조율할 수 있게 해준다.
  * CountDownLatch와 Semaphore가 자주 쓰인다.
  * 가장 강력한 동기화 장치는 Phaser다.
  
* 카운트다운 래치
  * 일회성 장벽으로, 하나 이상의 스레드가 또 다른 하나 이상의 스레드 작업이 끝날 때까지 기다리게 한다.
  * 유일한 생성자는 int 값을 받으며, 이 값이 래치의 countDown 메서드를 몇 번 호출해야 대기 중인 스레드들을 깨우는지를 결정한다.
  
> 어떤 동작들을 동시에 시작해 모두 완료하기까지의 시간을 재는 간단한 프레임워크를 구축할 때 CountDownLatch를 쓰면 직관적으로 구현할 수 있다.
```
public static long time(Executor executor, int concurrency, Runnable action) throws InterruptedException {
    CountDownLatch ready = new CountDownLatch(concurrency);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(concurrency);
    
    for (int i = 0; i < concurrency; i++) {
        executor.execute(() -> {
            // 타이머에게 준비를 마쳤음을 알린다.
            ready.countDown();
            try {
                // 모든 작업자 스레드가 준비될 때까지 기다린다.
                start.await();
                action.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 타이머에게 작업을 마쳤음을 알린다.
                done.countDown();
            }
        });
    }
    
    ready.await(); // 모든 작업자가 준비될 때까지 기다린다.
    long startNanos = System.nanoTime();
    start.countDown(); // 작업자들을 깨운다.
    done.await(); // 모든 작업자가 일을 끝마치기를 기다린다.
    return System.nanoTime() - startNanos;
}
```
* 위 코드는 카운트다운 래치를 3개 사용한다. 
  * ready 래치는 작업자 스레드들이 준비가 완료됐음을 타이머 스레드에 통지할 때 사용한다.
  * 통지를 끝낸 작업자 스레드들은 두 번째 래치인 start가 열리기를 기다린다.
  * 마지막 작업자 스레드가 ready.countDown을 호출하면 타이머 스레드가 시작 시각을 기록하고 start.countDown을 호출하여 기다리던 작업자 스레드들을 깨운다.
  * 타이머 스레드는 세 번째 래치인 done이 열리기를 기다린다.
  * done 래치는 마지막 남은 작업자 스레드가 동작을 마치고 done.countDown을 호출하면 열린다.
  * 타이머 스레드는 done 래치가 열리자마자 깨어나 종료 시각을 기록한다.
  
* 위 코드의 세부사항
  * time 메서드에 넘겨진 실행자는 concurrency 매개변수로 지정한 동시성 수준만큼의 스레드를 생성할 수 있어야 한다. 그렇지 못하면 이 메서드는 결코 끝나지 않는다.
  * 이러한 상태를 스레드 기아 교착상태라 한다.
  * InterruptedException을 캐치한 작업자 스레드는 Thread.currentThread().interrupt() 관용구를 사용해 인터럽트를 되살리고 자신은 run 메서드에서 빠져나온다.
  * 시간 간격을 잴 때는 System.currentTimeMillis가 아닌 System.nanoTime을 사용한다.
  
* wait 메서드
  * 스레드가 어떤 조건이 충족되기를 기다리게 할 때 사용한다.
  * 락 객체의 wait 메서드는 반드시 그 객체를 잠근 동기화 영역 안에서 호출해야 한다.
  
```
synchronized (obj) {
    while (<조건이 충족되지 않았다>) {
        obj.wait(); // (락을 놓고,  깨어나면 다시 잡는다.)
    }
    ... // 조건이 충족됐을 때 동작을 수행한다.
}
```

  * wait 메서드를 사용할 때는 반드시 대기 반복문 관용구를 사용한다. 반복문 밖에서는 절대 호출해서는 안 된다.
  * 대기 전에 조건을 검사해 이미 충족되었다면 wait를 건너뛰게 함으로써 응답 불가 상태를 예방할 수 있다.
  * 대기 후에 조건을 검사해 조건이 충족되지 않았다면 다시 대기하게 하는 것은 안전 실패를 막는다.
  
* 조건이 만족되지 않아도 스레드가 깨어날 수 있는 상황
  * 스레드가 notify를 호출한 다음 대기 중이던 스레드가 깨어나는 사이에 다른 스레드가 락을 얻어 그 락이 보호되는 상태를 변경한다.
  * 조건이 만족되지 않았음에도 다른 스레드가 실수로 혹은 악의적으로 notify를 호출한다.
  * 대기 중인 스레드 중 일부만 조건이 충족되어도 notifyAll을 호출해 모든 스레드를 깨울 수도 있다.
  * 대기 중인 스레드가 notify 없이도 깨어나는 경우가 있다.(허위 각성)
  
* notifyAll을 사용하는 것이 더 합리적이고 안전하다.
  * 모든 스레드가 같은 조건을 기다리고, 조건이 한 번 충족될 때마다 단 하나의 스레드만 혜택을 받을 수 있다면 notify를 사용해 최적화할 수 있다.
  * 하지만 notify 대신 notifyAll을 사용하면 관련없는 스레드가 실수로 혹은 악의적으로 wait를 호출하는 공격으로부터 보호할 수 있다.