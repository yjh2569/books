# 공유 중인 가변 데이터는 동기화해 사용하라

* synchronized 키워드는 해당 메서드나 블록을 한 번에 한 스레드씩 수행하도록 보장한다.
  * 보통 동기화를 배타적 실행, 즉 한 스레드가 변경하는 중이라 상태가 일관되지 않은 순간의 객체를 다른 스레드가 보지 못하게 막는 용도로 생각한다.
  * 맞는 설명이지만 동기화는 추가적으로 메서드나 블록에 들어간 스레드가 같은 락의 보호 하에 수행된 모든 이전 수정의 최종 결과를 보게 해준다.

* long과 double 외의 변수를 읽고 쓰는 동작은 원자적이다.
  * 여러 스레드가 같은 변수를 동기화 없이 수정하더라도 항상 어떤 스레드가 정상적으로 저장한 값을 온전히 읽어올 수 있다.
  * 그러나 자바 언어 명세는 위를 보장하지만 한 스레드가 저장한 값이 다른 스레드에게 보이는지는 보장하지 않는다.
  * 따라서 동기화는 배타적 실행뿐 아니라 스레드 사이의 안정적인 통신에 꼭 필요하다.
  
* 스레드를 멈추는 작업에는 Thread.stop은 사용하지 않는다.
  * 다른 스레드를 멈추는 방법은 아래와 같다.
  * 첫 번째 스레드는 자신의 boolean 필드를 폴링하면서 그 값이 true가 되면 멈춘다.
  * 이 필드를 false로 초기화해놓고, 다른 스레드에서 이 스레드를 멈추고자 할 때 true로 변경한다.

* 공유 중인 가변 데이터를 비록 원자적으로 읽고 쓸 수 있더라도 동기화에 실패하면 좋지 않은 결과로 이어질 수 있다.
  * 스레드를 멈추는 작업에서 boolean 필드를 읽고 쓰는 작업이 원자적이기 때문에 이 필드에 접근 시 동기화를 제거하기도 한다.
```
public class StopThread {
    private static boolean stopRequested;
    
    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested) i++;
        });
        backgroundThread.start();
        
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```
  * 그러나 실제로 이 코드를 수행하면 1초 후에 종료되지 않고 계속 수행된다.
  * 이는 동기화를 하지 않아 메인 스레드가 수정한 값을 백그라운드 스레드가 언제쯤에나 보게 될지 보증할 수 없기 때문이다.
  * 동기화가 빠지면 가상머신이 다음과 같은 최적화를 수행할 수도 있다.
```
// 원래 코드
while (!stopRequested) i++;

// 최적화된 코드
if (!stopRequested) while (true) i++;
```
  * 이는 OpenJDK 서버 VM이 실제로 적용하는 hoisting이라는 최적화 기법이다.
  * 따라서 stopRequested 필드는 동기화해야 한다.
```
public class StopThread {
    private static boolean stopRequested;
    
    private static synchronized void requestStop() {
        stopRequested = true;   
    }
    
    private static synchronized boolean stopRequested() {
        return stopRequested;
    }
    
    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested()) i++;
        });
        backgroundThread.start();
        
        TimeUnit.SECONDS.sleep(1);
        requestStop();
    }
}
```
  * 위와 같이 쓰기와 읽기 메서드 모두 동기화되지 않으면 동작을 보장하지 않는다.
  * 이 코드에서 동기화는 통신 목적으로만 사용된다.
  
* stopRequest 필드를 volatile으로 선언하면 동기화를 생략해도 된다. volatile 한정자는 배타적 수행과는 상관없지만 항상 가장 최근에 기록된 값을 읽게 됨을 보장한다.
```
public class StopThread {
    private static volatile boolean stopRequested;
    
    public static void main(String[] args) throws InterruptedException {
        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested) i++;
        });
        backgroundThread.start();
        
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```
  * volatile은 주의해서 사용해야 한다.
```
private static volatile int nextSerialNumber = 0;

public static int generateSerialNumber() {
    return nextSerialNumber++;
}
```
  * 증가 연산자(++)는 코드상으로는 하나지만 실제로는 nextSerialNumber 필드에 두 번 접근한다.
  * 따라서 두 번 접근하는 사이에 다른 스레드가 이 필드에 접근하면 똑같은 값을 돌려받을 수 있다. 
  * 이런 방식으로 프로그램이 잘못된 결과를 계산해내는 오류를 안전 실패라 한다.
  * generateSerialNumber 메서드에 synchronized 한정자를 붙이면 이 문제가 해결된다. 단, 메서드에 synchronized를 붙였다면 nextSerialNumber 필드에서는 volatile을 제거해야 한다.
  * 이 메서드를 더욱 견고히 하려면 int 대신 long을 사용하거나 nextSerialNumber가 최댓값에 도달하면 예외를 던지게 한다.
  
* java.util.concurrent.atomic 패키지는 락 없이도(lock-free) 스레드 안전한 프로그래밍을 지원하는 클래스들이 담겨 있다.
  * volatile은 동기화의 두 효과 중 통신 쪽만 지원하지만 이 패키지는 원자성까지 지원한다.
  * 성능도 동기화 버전보다 우수하다.
```
// java.util.concurrent.atomic 패키지의 AtomicLong을 사용했다.
private static final AtomicLong nextSerialNum = new AtomicLong();

public static long generateSerialNumber() {
    return nextSerialNum.getAndIncrement();
}
```

* 동기화로 인한 문제들을 피하는 가장 좋은 방법은 가변 데이터를 스레드 간에 공유하지 않는 것이다.
  * 불변 데이터만 공유하거나 아무 것도 공유하지 않는 게 좋다.
  * 사용하려는 프레임워크나 라이브러리를 깊게 이해하여 외부 코드가 인지하지 못한 스레드를 수행하지는 않는지도 주의해야 한다.
  
* 한 스레드가 데이터를 다 수정한 후 다른 스레드에 공유할 때는 해당 객체에서 공유하는 부분만 동기화해도 된다.
  * 그러면 그 객체를 다시 수정할 일이 생기기 전까지 다른 스레드들은 동기화 없이 자유롭게 값을 읽어갈 수 있다. 
  * 이러한 객체를 사실상 불변(effectively immutable)이라 한다.
  * 다른 스레드에 이런 객체를 건네는 행위를 안전 발행(safe publication)이라 한다.
  * 객체를 안전하게 발행하는 방법에는 여러 가지가 있다.
    * 클래스 초기화 과정에서 객체를 정적 필드, volatile 필드, final 필드, 혹은 보통의 락을 통해 접근하는 필드에 저장해도 된다.
    * 동시성 컬렉션에 저장하는 방법도 있다.  