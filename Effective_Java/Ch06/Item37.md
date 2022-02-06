# ordinal 인덱싱 대신 EnumMap을 사용하라

* 배열이나 리스트에서 원소를 꺼낼 때 ordinal 메서드로 인덱스를 얻는 코드가 있다.

```
class Plant {
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL }
    
    final String name;
    final LifeCycle lifeCycle;
    
    Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }
    
    @Override public String toString() {
        return name;
    }
}

Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
for (int i = 0; i < plantsByLifeCycle.length; i++) {
    plantsByLifeCycle[i] = new HashSet<>();
}

for (Plant p : garden) {
    plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);
}

for (int i = 0; i < plantsByLifeCycle.length; i++) {
    System.out.printf("%s: %s%n", Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);
}
```

* 위 코드의 문제점
  * 배열은 제네릭과 호환되지 않아 비검사 형변환을 수행해야 하고 깔끔하게 컴파일되지 않는다.
  * 배열은 각 인덱스의 의미를 모르기에 출력 결과에 직접 레이블을 달아야 한다.
  * 정확한 정숫값을 사용한다는 것을 직접 보증해야 한다. 정수는 타입 안전하지 않기에 잘못 동작하기 쉽다.
  
* 위 코드를 보완하는 방법으로 EnumMap을 사용할 수 있다.
  * 안전하지 않은 형변환을 쓰지 않아도 된다.
  * 맵의 키인 열거 타입이 그 자체로 출력용 문자열을 제공하기에 출력 결과에 직접 레이블을 달 일도 없다.

```
// EnumMap의 생성자가 받는 키 타입의 Class 객체는 한정적 타입 토큰으로 런타임 제네릭 타입 정보를 제공한다.
Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle = new EnumMap<>(Plant.LifeCycle.class); 
for (Plant.LifeCycle lc : Plant.LifeCycle.values()) {
    plantsByLifeCycle.put(lc, new HashSet<>());
}
for (Plant p : graden) {
    plantsByLifeCycle.get(p.lifeCycle).add(p);
}
System.out.println(plantsByLifeCycle);

// 스트림을 사용해 맵을 관리하면 코드를 더 줄일 수 있다. * 해당 생애주기에 속하는 식물이 있는 경우에만 중첩 맵을 만든다.
System.out.println(Arrays.stream(garden)).collect(groupingBy(p -> p.lifeCycle, () -> new EnumMap<>(LifeCycle.class), toSet()));
```

> 두 열거 타입 값들을 매핑하기 위해 ordinal을 두 번 쓴 배열들의 배열 예시
```
public enum Phase {
    SOLID, LIQUID, GAS;
    
    public enum Transition {
        MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;
        
        // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 쓴다.
        private static final Transition[][] TRANSITIONS = {
            { null, MELT, SUBLIME },
            { FREEZE, null, BOIL },
            { DEPOSIT, CONDENSE, null }
        };
        
        // 한 상태에서 다른 상태로의 전이를 반환
        public static Transition from(Phase from, Phase to) {
            return TRANSITIONS[from.ordinal()][to.ordinal()];
        }
    }
}
```

* 위 예시 역시 ordinal을 사용함으로 인해 발생하는 문제점을 그대로 가지고 있다. 따라서 위 예시 역시 EnumMap을 사용하는 편이 낫다.
  * 전이 하나를 얻으려면 이전 상태와 이후 상태가 필요하기에 맵 2개를 중첩한다.
  
```
public enum Phase {
    SOLID, LIQUID, GAS;
    
    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID), BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID), SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);
        
        private final Phase from;
        private final Phase to;
        
        Transition(Phase from, Phase to) {
            this.from = from;
            this.to = to;
        }
        
        private static final Map<Phase, Map<Phase, Transition>> m = Stream.of(values()).collect(groupingBy(t -> t.from, () -> new EnumMap<>(Phase.class), toMap(t -> t.to, t -> t, (x, y) -> y, () -> new EnumMap<>(Phase.class)));
        
        public static Transition from(Phase from, Phase to) {
            return m.get(from).get(to);
        }
    }
}
```

* 위 예시에서 맵을 초기화하기 위해 수집기 2개를 차례로 사용했다.
  * 첫 번째 수집기인 groupingBy에서는 전이를이전 상태를 기준으로 묶고, 두 번째 수집기인 toMap에서는 이후 상태를 전이에 대응시키는 EnumMap을 생성한다.
  
* 위 예시에 새로운 상태인 플라즈마(PLASMA)를 추가하려면 상태 목록에 PLASMA를 추가하고, 전이 목록에 IONIZE(GAS, PLASMA)와 DEIONIZE(PLASMA, GAS)만 추가하면 끝이다.