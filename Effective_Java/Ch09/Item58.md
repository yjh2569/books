# 전통적인 for 문보다는 for-each 문을 사용하라

* for 문에서 반복자와 인덱스 변수는 모두 코드를 지저분하게 할 뿐 실제로 필요한 건 원소들뿐이다.
  * 1회 반복에서 반복자는 세 번, 인덱스는 네 번이나 등장해 변수를 잘못 사용할 틈새가 넓어진다. 
  * 잘못된 변수를 사용했을 때 컴파일러가 잡아주리라는 보장도 없다.
  * 컬렉션인지 배열인지에 따라 코드 형태가 상당히 달라지므로 주의해야 한다.
  
* 위 문제는 for-each 문을 사용하면 모두 해결된다.
  * for-each의 정식 이름은 향상된 for 문(enhanced for statement)이다.
  * 반복자와 인덱스 변수를 사용하지 않아 코드가 깔끔해지고 오류가 날 일도 없다.
  * 어떤 컨테이너를 다루는지는 신경 쓰지 않아도 된다.
  
> 컬렉션과 배열을 순회하는 올바른 관용구
```
for (Element e : elements) { // ':'은 in이라고 읽는다.
    ... // e로 무언가를 한다.
}
```

* 컬렉션을 중첩해 순회해야 한다면 for-each 문의 이점이 더욱 커진다.

```
enum Suit { CLUB, DIAMOND, HEART, SPADE }
enum Rank { ACE, DEUCE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }

...

static Collection<Suit> suits = Arrays.asList(Suit.values());
static Collection<Rank> ranks = Arrays.asList(Rank.values());

List<Card> deck = new ArrayList<>();
for (Iterator<Suit> i = suits.iterator(); i.hasNext(); ) {
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext(); ) {
        deck.add(new Card(i.next(), j.next()));
    }
}
```

* 위 코드에서 i.next()를 안쪽 반복문에서 호출하면서 카드 하나당 한 번씩 불리면서 버그가 발생한다. 따라서 숫자가 바닥나면 NoSuchElementException을 던진다.
  * 만약 바깥 컬렉션의 크기가 안쪽 컬렉션 크기의 배수라면 이 반복문은 예외를 던지지 않고 종료해 더 큰 문제를 야기할 수도 있다.
  
> 위 문제를 해결하는 방법 1
```
for (Iterator<Suit> i = suits.iterator(); i.hasNext(); ) {
    Suit suit = i.next();
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext(); ) {
        deck.add(new Card(suit, j.next()));
    }
}
```

> 위 문제를 해결하는 방법 2. for-each 문을 사용함으로써 훨씬 간결해졌다.
```
for (Suit suit : suits) {
    for (Rank rank : ranks) {
        deck.add(new Card(suit, rank));
    }
}
```

* for-each 문을 사용할 수 없는 상황
  * 파괴적인 필터링(destructive filtering) : 컬렉션을 순회하면서 선택된 원소를 제거해야 하는 경우 반복자의 remove 메서드를 호출해야 한다. 자바 8부터는 Collection의 removeIf 메서드를 사용해 컬렉션을 명시적으로 순회하는 일을 피할 수 있다.
  * 변형(transforming) : 리스트나 배열을 순회하면서 그 원소의 값 일부 혹은 전체를 교체해야 한다면 리스트의 빈복자나 배열의 인덱스를 사용해야 한다.
  * 병렬 반복(parallel iteration) : 여러 컬렉션을 병렬로 순회해야 한다면 각각의 반복자와 인덱스 변수를 사용해야 한다.
  * 위 세 가지 상황에서는 일반적인 for 문을 사용하되 for 문의 문제점을 경계해야 한다.
  
* for-each 문은 컬렉션과 배열은 물론 Iterable 인터페이스를 구현한 객체라면 무엇이든 순회할 수 있다.