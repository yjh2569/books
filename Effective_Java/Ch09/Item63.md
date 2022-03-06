# 문자열 연결은 느리니 주의하라

* 문자열 연결 연산자(+)는 여러 문자열을 하나로 합쳐주는 편리한 수단이지만, 문자열 연결 연산자로 문자열 n개를 잇는 시간은 n^2에 비례하여 비효율적이다.
  * 문자열은 불변이라서 두 문자열을 연결할 경우 양쪽의 내용을 모두 복사해야하므로 성능이 낮아질 수밖에 없다.
  
> 문자열 연결을 잘못 사용한 예
```
public String statement() {
    String result = "";
    for (int i = 0; i < numItems(); i++) {
        result += lineForItem(i); // 문자열 연결
    }
    return result;
}
```

* String 대신 StringBuilder를 사용하면 성능을 개선할 수 있다.

> StringBuilder 사용 예시
```
public String statement2() {
    StringBuilder b = new StringBuilder(numItems() * LINE_WIDTH);
    for (int i = 0; i < numItems(); i++) {
        b.append(lineForItem(i));
    }
    return b.toString();
}
```

* StringBuilder를 사용하면 수행 시간이 품목 수에 대해 선형으로 늘어나기에 훨씬 효율적이다.
  * 위 예시처럼 StringBuilder를 전체 결과를 담기에 충분한 크기로 초기화함으로써 효율성을 높일 수 있다.