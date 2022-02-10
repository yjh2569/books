# @Override 애너테이션을 일관되게 사용하라

* @Override는 메서드 선언에만 달 수 있고, 이 애너테이션이 달렸다는 것은 상위 타입의 메서드를 재정의했음을 의미한다.
  * 이 애너테이션을 일관되게 사용하면 여러 가지 버그들을 예방할 수 있다.
  
```
public class Bigram {
    private final char first;
    private final char second;
    
    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }
    
    public boolean equals(Bigram b) {
        return b.first == first && b.second == second;
    }
    
    public int hashCode() {
        return 31 * first + second;
    }
    
    public static void main(String[] args) {
        Set<Bigram> s = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                s.add(new Bigram(ch, ch));
            }
        }
        System.out.println(s.size());
    }
}
```

* 위 코드에서 Object 클래스의 equals와 hashCode 메서드를 재정의했지만, 실제로는 equals의 매개변수 타입은 Object이기 때문에 overriding이 아닌 overloading이 된다.
  * 따라서 같은 first와 second를 가진 두 객체는 위 상황에서는 같지 않다.
  * 이러한 문제를 예방하기 위해서 Object.equals를 재정의한다는 의도를 명시해야 하고, 이를 위해 @Override 애너테이션이 필요하다.
  
```
@Override public boolean equals(Bigram b) {
    return b.first == first && b.second == second;
}
```

* 위와 같이 작성하면 컴파일 오류가 발생해 overriding을 잘못했음을 알려준다.

* 정리하면, 상위 클래스의 메서드를 재정의하려는 모든 메서드에 @Override 애너테이션을 달아준다.
  * 예외로 구체 클래스에서 상위 클래스이 추상 메서드를 재정의할 때는 굳이 @Override를 달지 않아도 된다.
  
* @Override는 클래스뿐 아니라 인터페이스의 메서드를 재정의할 때도 사용할 수 있다.
  * 디폴트 메서드를 지원하기 시작하면서, 인터페이스 메서드를 구현한 메서드에도 @Override를 다는 습관을 들이면 시그니처가 올바른지 재차 확신할 수 있다.
  
* 추상 클래스나 인터페이스에서는 상위 클래스나 상위 인터페이스의 메서드를 재정의하는 모든 메서드에 @Override를 다는 것이 좋다.