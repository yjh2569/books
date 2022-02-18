# 반환 타입으로는 스트림보다 컬렉션이 낫다

* 스트림은 반복(iteration)을 지원하지 않는다.
  * 따라서 API를 스트림으로 반환하도록 짜놓으면 반환된 스트림을 for-each로 반복할 수 없다.
  * Stream 인터페이스는 Iterable 인터페이스가 정의한 추상 메서드를 전부 포함하고, Iterable 인터페이스가 정의한 방식대로 동작하지만, Stream이 Iterable을 확장하지 ㅇ낳았기 때문에 for-each로 스트림을 반복할 수 없다.

* Stream의 iterator 메서드에 메서드 참조를 건네는 방식으로 for-each에 스트림을 사용할 수는 있다.

> 스트림 반복을 위한 우회 방법
```
for (ProcessHandle ph : (Iterable<ProcessHandle>)ProcessHandle.allProcesses()::iterator) {
    // 프로세스 처리
}
```

* 위와 같은 코드는 작동은 하지만 너무 난잡하고 직관성이 떨어진다. 어댑터 메서드를 사용하면 상황이 나아진다.
  * 이 경우에는 자바의 타입 추론이 문맥을 잘 파악해 어댑터 메서드 안에서 따로 형변환하지 않아도 된다.
  
> Stream\<E\>를 Iterable\<E\>로 중개해주는 어댑터
```
public static <E> Iterable<E> iterableOf(Stream<E> stream) {
    return stream::iterator;
}

for (ProcessHandle p : iterableOf(ProcessHandle.allProcesses())) {
    // 프로세스 처리
}
```

> Iterable\<E\>를 Stream\<E\>로 중개해주는 어댑터
```
public static <E> Stream<E> streamOf(Iterable<E> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
}
```

* 객체 시퀀스를 반환하는 메서드 작성 시 이 메서드가 오직 스트림 파이프라인에서만 쓰일 걸 안다면 스트림을 반환하게 한다.
* 반대로 반환된 객체들이 반복문에서 쓰일 걸 안다면 Iterable을 반환한다.

* Collection 인터페이스는 Iterable의 하위 타입이면서 stream 메서드도 제공해 반복과 스트림을 동시 지원한다.
  * 따라서 원소 시퀀스를 반환하는 공개 API의 반환 타입에는 Collection이나 그 하위 타입을 쓰는 게 최선이다.
  * 반환 시퀀스 크기가 작다면 ArrayList나 HashSet 같은 표준 컬렉션 구현체를 반환해도 괜찮지만, 컬렉션을 반환한다는 이유로 크기가 큰 시퀀스를 메모리에 올려서는 안 된다.
  * 반환할 시퀀스가 크지만 표현을 간결하게 할 수 있다면 전용 컬렉션을 구현한다.
  > 멱집합을 구현하는 경우 각 원소의 인덱스를 비트 벡터로 사용하는 방법이 있다.