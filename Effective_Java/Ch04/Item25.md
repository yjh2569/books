# 톱레벨 클래스는 한 파일에 하나만 담으라

* 소스 파일 하나에 톱레벨 클래스를 여러 개 선언해도 자바 컴파일러는 코드가 잘못됐다고 하지는 않지만, 한 클래스가 여러 가지로 정의되어 어느 것을 사용할지는 어느 소스 파일을 먼저 컴파일하냐에 따라 달라지기에 위험이 뒤따를 수 있다.

> 하나의 파일에 두 클래스가 정의된 예
```
// Main.java
public class Main {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
}

// Utensil.java
class Utensil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}

// Dessert.java
class Utensil {
    static final String NAME = "pot";
}

class Dessert {
    static final String NAME = "pie";
}
```

* Utensil.java만 있었다면 Main을 실행했을 때 pancake를 출력하지만, Dessert.java도 있었다면 컴파일 순서에 따라 다른 결과가 나올 수 있다.
  * javac Main.java Dessert.java 명령으로 컴파일하면 Utensil.java 파일을 먼저 살핀 뒤 Dessert.java를 처리하면서 컴파일 오류가 발생한다.
  * javac Main.java나 javac Main.java Utensil.java 명령으로 컴파일하면 Dessert.java 파일을 작성하기 전처럼 pancake를 출력한다.
  * javac Dessert.java Main.java 명령으로 컴파일하면 potpie를 출력한다.
  
* 따라서 톱레벨 클래스들(Utensil과 Dessert)을 서로 다른 소스 파일로 분리해야 한다.
  * 굳이 여러 톱레벨 클래스를 한 파일에 담고 싶으면 정적 멤버 클래스를 사용한다.

> 톱레벨 클래스들을 정적 멤버 클래스로 바꿔본 모습
```
public class Test {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
    
    private static class Utensil {
        static final String NAME = "pan";
    }
    
    private static class Dessert {
        static final String NAME = "cake";
    }
}
```

