# 가변인수는 신중히 사용하라

* 가변인수 메서드는 명시한 타입의 인수를 0개 이상 받을 수 있다.
  * 가변인수 메서드 호출 시 가장 먼저 인수의 개수와 길이가 같은 배열을 만들고 인수들을 이 배열에 저장해 가변인수 메서드에 건네준다.
  
> 간단한 가변인수 활용 예
```
static int sum(int... args) {
    int sum = 0;
    for (int arg: args) {
        sum += arg;
    }
    return sum;
}
```

* 인수를 1개 이상 받아야 하는 경우에는 매개변수를 2개 받아 첫 번째는 평범한 매개변수를, 두 번째는 가변인수를 받도록 한다.

```
static int min(int firstArg, int... remainingArgs) {
    int min = firstArg;
    for (int arg : remainingArgs) {
        if (arg < min) {
            min = arg;
        }
    }
    return min;
}
```

* 성능에 민감한 상황일 경우 가변인수 메서드는 호출될 때마다 배열을 새로 하나 할당하고 초기화하므로, 가변인수의 개수가 적은 경우 인수가 여러 개인 메서드로 다중정의한다.