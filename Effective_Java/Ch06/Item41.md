# 정의하려는 것이 타입이라면 마커 인터페이스를 사용하라

* 마커 인터페이스 : 아무 메서드도 담고 있지 않고, 단지 자신을 구현하는 클래스가 특정 속성을 가짐을 표시해주는 인터페이스
> Serializable 인터페이스가 대표적인 마커 인터페이스 예다. 자신을 구현한 클래스의 인스턴스는 ObjectOutputStream을 통해 쓸 수 있다고(직렬화) 알려준다.

* 마커 애너테이션이 등장하였으나 여전히 마커 인터페이스가 더 낫다.
  * 마커 인터페이스는 이를 구현한 클래스의 인스턴스들을 구분하는 타입으로 쓸 수 있지만, 마커 애너테이션은 그렇지 않다.
  * 적용 대상을 더 정밀하게 지정할 수 있다.
  
* 반대로 마커 애너테이션은 마커 인터페이스에 비해 거대한 애너테이션 시스템의 지원을 받는다.
  * 따라서 애너테이션을 적극 활용하는 프레임워크에서는 마커 애너테이션을 쓰는 쪽이 일관성을 지키는 데 유리하다.
  
* 마커 애너테이션과 마커 인터페이스의 적절한 사용처
  * 클래스와 인터페이스 외의 프로그램 요소에 마킹해야 할 경우 애너테이션을 쓸 수밖에 없다.
  * 마커를 클래스나 인터페이스에 적용하면서, 마킹이 된 객체를 매개변수로 받는 메서드로 작성하는 경우 마커 인터페이스를 사용한다. 이렇게 하면 그 마커 인터페이스를 해당 메서드의 매개변수 타입으로 사용해 컴파일타임에 오류를 잡아낼 수 있다.
  * 애너테이션을 활발히 활용하는 프레임워크에서 사용하려는 마커라면 마커 애너테이션을 사용한다.