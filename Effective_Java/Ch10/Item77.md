# 예외를 무시하지 말라

* API 설계자가 메서드 선언에 예외를 명시하는 것은 그 메서드를 사용할 때 적절한 조치를 취해야 함을 알리는 것이다.
  * 해당 메서드 호출을 try 문으로 감싼 후 catch 블록에서 아무 일도 하지 않으면 예외를 무시하게 되는데, 이러면 예외가 존재할 이유가 없어진다.
  * 이렇게 예외를 무시하면 나중에 끔찍한 결과를 불러올 수도 있기에 주의해야 한다.
  
* 예외를 무시하는 경우도 있다.
  * FileInpuStream을 닫을 때 파일의 상태를 변경하지 않았으니 복구할 것이 없으며, 필요한 정보는 이미 다 읽었다는 뜻이니 남은 작업을 중단할 이유도 없다.
  * 같은 예외가 자주 발생했다면 조사해보는 것이 좋을 테니 파일을 닫지 못했다는 사실을 로그로 남기는 것도 좋다.
  * 예외를 무시하기로 했다면 catch 블록 안에 그렇게 결정한 이유를 주석으로 남기고 예외 변수의 이름도 ignored로 바꾼다.
  
```
Future<Integer> f = exec.submit(planarMap::chromaticNumber);
int numColors = 4; // 기본값. 어떤 지도라도 이 값이면 충분하다.
try {
    numColors = f.get(1L, TimeUnit.SECONDS);
} catch (TimeoutException || ExecutionException ignored) {
    // 기본값을 사용한다(색상 수를 최소화하면 좋지만, 필수는 아니다).
}
```