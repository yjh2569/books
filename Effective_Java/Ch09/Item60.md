# 정확한 답이 필요하다면 float와 double은 피하라

* float와 double 타입은 이진 부동소수점 연산에 쓰이며, 널은 범위의 수를 빠르게 정밀한 근사치로 계산하도록 설계되었다.
  * 따라서 정확한 결과가 필요할 때는 사용하면 안 된다. 
  * 특히 금융 관련 계산과는 맞지 않는다.
  
* 금융 계산에는 BigDecimal, int 혹은 long을 사용해야 한다.

> BigDecimal 사용 예시
```
public static void main(String[] args) {
    final BigDecimal TEN_CENTS = new BigDecimal(".10"); // 문자열을 받는 생성자를 사용
    
    int itemsBought = 0;
    BigDecimal funds = new BigDecimal("1.00");
    for (BigDecimal price = TEN_CENTS; funds.compareTo(price) >= 0; price = price.add(TEN_CENTS)) {
        funds = funds.subtract(price);
        itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(달러): " + funds);
}
```

* BigDecimal은 기본 타입보다 쓰기가 훨씬 불편하고, 훨씬 느리다.

* BigDecimal의 대안으로 int 혹은 long 타입을 쓸 수도 있다.
  * 그럴 경우 다룰 수 있는 값의 크기가 제한되고, 소수점을 직접 관리해야 한다.

> 정수 타입 사용 예시
```
public static void main(String[] args) {
    int itemsBought = 0;
    int funds = 100;
    for (int price = 10; funds >= price; price += 10) {
        funds -= price;
        itemsBought++;
    }
    System.out.println(itemBought + "개 구입");
    System.out.println("잔돈(센트): " + funds);
}
```