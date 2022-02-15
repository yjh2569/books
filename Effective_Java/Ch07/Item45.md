# 스트림은 주의해서 사용하라

* 스트림 API
  * 다량의 데이터 처리 작업을 돕고자 자바 8에 추가되었다.
  * 스트림(stream) : 데이터 원소의 유한 혹은 무한 시퀀스
  * 스트림 파이프라인(stream pipeline) : 이 원소들로 수행되는 연산 단계
  * 스트림의 원소들은 컬렉션, 배열, 파일, 정규표현식 패턴 매처, 난수 생성기 등 어디로부터든 올 수 있다.
  * 스트림 안의 데이터 원소들은 객체 참조나 기본 타입 값이다.
  
* 스트림 파이프라인은 소스 스트림에서 시작해 종단 연산(terminal operation)으로 끝난다.
  * 그 사이에 하나 이상의 중간 연산(intermediate operation)이 있을 수 있다. 각 중간 연산은 스트림을 어떠한 방식으로 변환한다.
  > 각 원소에 함수를 적용하거나 특정 조건을 만족 못하는 원소를 걸러낼 수 있다.
  * 중간 연산 결과 변환된 스트림의 원소 타입은 변환 전과 달라질 수도 있다.
  * 종단 연산은 마지막 중간 연산이 내놓은 스트림에 최후의 연산을 가한다.
  > 원소를 정렬해 컬렉션에 담거나, 특정 원소 하나를 선택하거나, 모든 원소를 출력한다.
  
* 스트림 파이프라인은 지연 평가(lazy evaluation)된다.
  * 평가는 종단 연산이 호출될 때 이뤄지고, 종단 연산에 쓰이지 않는 데이터 원소는 계산에 쓰이지 않는다.
  * 지연 평가는 무한 스트림을 다룰 수 있게 해준다.
  * 종단 연산이 없는 스트림 파이프라인은 아무 일도 하지 않는 명령어인 no-op와 같다.
  
* 스트림 API는 메서드 연쇄를 지원하는 플루언트 API(fluent API)다.
  * 파이프라인 하나를 구성하는 모든 호출을 연결해 단 하나의 표현식으로 완성할 수 있다.
  * 파이프라인 여러 개를 연결해 표현식 하나로 만들 수도 있따.
  
* 스트림 파이프라인은 순차적으로 수행되나, parellel 메서드를 통해 병렬로 실행할 수도 있다.

> 아나그램(철자를 구성하는 알파벳이 같고 순서만 다른 단어) 그룹
```
public class Anagrams {
    public static void main(String[] args) throws IOException {
        File dictionary = new File(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);
        
        Map<String, Set<String>> groups = new HashMap<>();
        try (Scanner s = new Scanner(dictionary)) {
            while (s.hasNext()) {
                String word = s.next();
                // computeIfAbsent는 맵 안에 키가 있는지 찾고 있으면 단순히 그 키에 매핑된 값을 반환한다.
                // 없으면 건네진 함수 객체를 키에 적용해 값을 계산해 그 키와 값을 매핑해놓고 계산된 값을 반환한다.
                groups.computeIfAbsent(alphabetize(word), (unused) -> new TreeSet<>()).add(word);
            }
        }
        
        for (Set<String> group : groups.values()) {
            if (group.size() >= minGroupSize) {
                System.out.println(group.size() + ": " + group);
            }
        }
    }
    
    private static String alphabetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }
}
```

* 스트림은 과용할 경우 프로그램이 읽거나 유지보수하기 어려워지므로 과하게 사용하지 않는다.

> 적절한 스트림 사용
```
public class Anagrams {
    public static void main(String[] args) throws IOException {
        File dictionary = new File(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(groupingBy(word -> alphabetize(word))) // 중간 연산 없이 중단 연산에서 모든 단어를 수집해 맵으로 모은다.
                .values().stream() // 스트림의 원소는 아나그램 리스트다.
                .filter(group -> group.size() >= minGroupSize) // 리스트들 중 원소가 minGroupSize보다 적은 것은 필터링돼 무시된다.
                .forEach(g -> System.out.println(g.size() + ": " + g)); // 살아남은 리스트를 출력한다.
        }
    }
    
    ...
```

* char용 스트림을 자바가 지원하지 않기 때문에 char 값을 처리할 때는 스트림 사용을 지양한다.

* 기존 코드는 스트림을 사용하도록 리팩토링하되, 새 코드가 더 나아 보일 때만 반영한다.

* 스트림 파이프라인은 되풀이되는 계산을 함수 객체로 표현한다. 반면, 반복 코드에서는 코드 블록을 사용해 표현한다.
  * 코드 블록에서는 범위 안의 지역변수를 읽고 수정할 수 있다. 반면 람다에서는 final이거나 사실상 final인 변수만 읽을 수 있고, 지역 변수 수정이 불가능하다.
  * 코드 블록에서만 return 문을 사용해 메서드를 빠져나가거나, break, continue 문을 사용할 수 있다. 메서드 선언에 명시된 예외를 던질 수 있다.
  
* 스트림을 적용하기 좋은 경우
  * 원소들의 시퀀스를 일관되게 변환
  * 원스들의 시퀀스를 필터링
  * 원소들의 시퀀스를 하나의 연산을 사용해 결합(더하기, 연결하기, 최솟값 구하기 등)
  * 원소들의 시퀀스를 컬렉션에 모음
  * 원소들의 시퀀스에서 특정 조건을 만족하는 원소 검색
  
* 한 데이터가 파이프라인의 여러 단계를 통과할 때 이 데이터의 각 단계에서의 값들에 동시에 접근하기 어려운 경우 스트림으로 처리하기 어렵다.
  * 스트림 파이프라인은 일단 한 값을 다른 값에 매핑하면 원래 값을 잃기 때문이다.
  
> 무한 스트림을 반환하는 메서드
```
static Stream<BigInteger> primes() {
    // Stream.iterate는 첫 번째 매개변수로 첫 번째 원소, 두 번째 매개변수로 스트림에서 다음 원소를 생성해주는 함수를 받는다.
    return Stream.iterate(TWO, BigInteger::nextProbablePrime);
}
```

> 위 메서드를 활용해 20개의 메르센 소수를 출력하는 프로그램
```
public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
        .filter(mersenne -> mersenne.isProbablePrime(50)) // 50은 소수성 검사가 true를 반환할 확률 제어
        .limit(20)
        .forEach(System.out::println);
}

// 각 메르센 소수 앞에 지수를 출력하길 원하는 경우 이 값은 초기 스트림에만 나타나므로 결과를 출력하는 종단 연산에서는 접근할 수 없다.
// 대신, 숫자를 이진수로 표현한 다음 몇 비트인지를 셈으로써 지수를 출력할 수 있다.
.forEach(mp -> System.out.println(mp.bitLength() + ": " + mp));
```

* 평탄화(flattening) : 스트림의 원소 각각을 하나의 스트림으로 매핑한 다음 그 스트림들을 다시 하나의 스트림으로 합치는 과정

> 평탄화(flattening) 예시
```
private static List<Card> newDeck() {
    return Stream.of(Suit.values())
        .flatMap(suit -> Stream.of(Rank.values()).map(rank -> new Card(suit, rank)))
        .collect(toList());
}
```