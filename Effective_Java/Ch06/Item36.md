# 비트 필드 대신 EnumSet을 사용하라

* 열거한 값들이 주로 집합으로 사용될 경우 예전에는 각 상수에 서로 다른 2의 거듭제곱 값을 할당한 정수 열거 패턴을 사용했다.

> 비트 필드 열거 상수
```
public class Text {
    public static final int STYLE_BOLD = 1 << 0;
    public static final int STYLE_ITALIC = 1 << 1;
    public static final int STYLE_UNDERLINE = 1 << 2;
    public static final int STYLE_STRIKETHROUGH = 1 << 3;
    
    // 매개변수 styles는 0개 이상의 STYLE_ 상수를 비트별 OR한 값이다.
    public void applyStyles(int styles) { ... }
}

// 비트 필드 : 비트별 OR을 사용해 여러 상수를 하나의 집합으로 모을 수 있다.
text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
```

* 비트 필드의 단점
  * 정수 열거 상수의 단점을 그대로 지닌다.
  * 비트 필드 값이 그대로 출력되면 단순한 정수 열거 상수를 출력할 때보다 해석하기 훨씬 어렵다.
  * 비트 필드 하나에 녹아 있는 모든 원소를 순회하기 까다롭다.
  * 최대 몇 비트가 필요한지 API 작성 시 미리 예측해 적절한 타입(int 또는 long)을 선택해야 한다. API를 수정하지 않고는 비트 수(32비트 또는 64비트)를 더 늘릴 수 없기 때문이다.
  
* 비트 필드 대신 java.util 패키지의 EnumSet 클래스를 이용해 열거 타입 상수의 값으로 구성된 집합을 효과적으로 표현한다.
  * Set 인터페이스를 완벽히 구현하고, 타입 안전하며, 다른 어떤 Set 구현체와도 함께 사용할 수 있다.
  * EnumSet 내부는 비트 벡터로 구현해 원소가 총 64개 이하라면 EnumSet 전체를 long 변수 하나로 표현해 비트 필드에 비견되는 성능을 보여준다.
  * 동시에 비트를 직접 다룰 때 난해한 작업들은 EnumSet이 다 처리해준다.
  
> EnumSet 사용 예시
```
public class Text {
    public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }
    
    // 어떤 Set을 넘겨도 되나, EnumSet이 가장 좋다.
    public void applyStyles(Set<Style> styles) { ... }
}

// applyStyles 메서드에 EnumSet 인스턴스를 건네는 클라이언트 코드
text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC)); 
```