# 태그 달린 클래스보다는 클래스 계층구조를 활용하라

> 태그 달린 클래스
```
class Figure {
    enum Shape {RECTANGLE, CIRCLE};
    
    // 태그 필드 - 현재 모양을 나타낸다.
    final Shape shape;
    
    // 다음 필드들은 모양이 사각형일 때만 쓰인다.
    double length;
    double width;
    
    // 다음 필드는 모양이 원일 때만 쓰인다.
    double radius;
    
    // 원용 생성자
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }
    
    // 사각형용 생성자
    Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }
    
    double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}
```

* 태그 달린 클래스는 장황하고 오류를 내기 쉬우며 비효율적이다.

* 자바와 같은 객체 지향 언어는 타입 하나로 다양한 의미의 객체를 표현하기 위해 클래스 계층구조를 활용하는 서브타이핑(subtyping)을 사용한다.

* 태그 달린 클래스를 클래스 계층구조로 바꾸는 방법
  * 계층구조의 루트가 될 추상 클래스를 정의하고 태그 값에 따라 동작이 달라지는 메서드들을 루트 클래스의 추상 메서드로 선언한다.
  * 태그 값에 상관없이 동작이 일정한 메서드들을 루트 클래스에 일반 메서드로 추가한다.
  * 모든 하위 클래스에서 공통으로 사용하는 데이터 필드들도 전부 루트 클래스로 올린다.
  * 루트 클래스를 확장한 구체 클래스를 의미별로 하나씩 정의한다.
  * 루트 클래스가 정의한 추상 메서드를 각자의 의미에 맞게 구현한다.
  
> 태그 달린 클래스를 클래스 계층구조로 변환
```
abstract class Figure {
    abstract double area();
}

class Circle extends Figure {
    final double radius;
    
    Circle(double radius) { this.radius = radius; }
    
    @Override double area() { return Math.PI * (radius * radius); }
}

class Rectangle extends Figure {
    final double length;
    final double width;
    
    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }
    
    @Override double area() { return length * width; } 
}
```
  
* 클래스 계층구조는 태그 달린 클래스의 단점을 모두 해결하면서, 유연성 역시 높일 수 있다.

> 정사각형도 지원하도록 수정하려면 Rectangle 클래스를 상속하는 Square 클래스를 만들면 된다.
  