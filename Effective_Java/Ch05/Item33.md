# 타입 안전 이종 컨테이너를 고려하라

* 타입 안전 이종 컨테이너 패턴(type safe heterogeneous container pattern)
  * 컨테이너 대신 키를 매개변수화한 다음, 컨테이너에 값을 넣거나 뺄 때 매개변수화한 키를 함께 제공한다.
  * 제네릭 타입 시스템이 값의 타입이 키와 같음을 보장한다.
  * 각 타입의 Class 객체를 매개변수화한 키 역할로 사용하면 되는데, 이 방식이 동작하는 이유는 class의 클래스가 제니릭이기 때문이다.
  * class 리터럴의 타입은 Class가 아닌 Class\<T>다.
  > String.class의 타입은 Class\<String>이고 Integer.class의 타입은 Class\<Integer>이다.
  * 타입 토큰 : 컴파일타임 타입 정보와 런타임 타입 정보를 알아내기 위해 메서드들이 주고받는 class 리터럴
  
> 타입 안전 이종 컨테이너 패턴 예시
```
// API
public class Favorites {
    public <T> void putFavorite(Class<T> type, T instance);
    public <T> T getFavorite(Class<T> type);
}

// 클라이언트
public static void main(String[] args) {
    Favorites f = new Favorites();
    
    f.putFavorite(String.class, "Java");
    f.putFavorite(Integer.class, 0xcafebabe);
    f.putFavorite(Class.class, Favorites.class);
    
    String favoriteString = f.getFavorite(String.class);
    int favoriteInteger = f.getFavorite(Integer.class);
    Class<?> favoriteClass = f.getFavorite(Class.class);
    
    System.out.printf("%s %x %s%n", favoriteString, favoriteInteger, favoriteClass.getName());
}

// 구현
public class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();
    
    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNonNull(type), instance);
    }
    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }
}
```

* Favorites 인스턴스는 타입 안전하다.
  * 모든 키의 타입이 제각각이라 일반적인 맵과 달리 여러 가지 타입의 원소를 담을 수 있다.
  
* Favorites 구현의 특징
  * 와일드카드 타입이 중첩되어 맵이 아니라 키가 와일드카드 타입이기 때문에 모든 키가 서로 다른 매개변수화 타입일 수 있다.
  * favorites 맵의 값 타입은 Object로 맵이 키와 값 사이의 타입 관계를 보증하지 않는다. 하지만 실제로는 이 관계가 성립함을 알고 있고, 즐겨찾기를 검색할 때 그 이점을 누릴 수 있다.
  * putFavorite 구현 시 키와 값 사이의 타입 링크 정보는 사라진다. 즉, 그 값이 그 키 타입의 인스턴스라는 정보는 사라진다.
  * getFavorite 구현에서 Object 타입의 값을 Class 객체가 가리키는 타입으로 동적 형변환하기 위해 Class의 cast 메서드를 사용한다.
  * cast 메서드는 주어진 인수가 Class 객체가 알려주는 타입의 인스턴스인지 검사하고, 맞다면 그 인수를 그대로 반환하지만 아니면 ClassCastException을 던진다.
  * cast 메서드의 시그니처가 Class 클래스가 제네릭이라는 이점을 완벽히 활용한다. cast의 반환 타입은 Class 객체의 타입 매개변수와 같다.
  
```
public class Class<T> {
    T cast(Object obj);
}
```

* Favorites 클래스의 제약
  * 악의적인 클라이언트가 Class 객체를 로 타입으로 넘기면 Favorites 인스턴스의 타입 안전성이 쉽게 깨진다. 이러한 일이 없도록 하기 위해서는 putFavorite 메서드에서 인수로 주어진 instance의 타입이 type으로 명시한 타입과 같은지 확인한다. 동적 형변환을 쓰면 된다.
  * 실체화 불가 타입에는 사용할 수 없다. String이나 String[]은 저장할 수 있지만 List\<String>은 저장할 수 없다. List\<String>용 Class 객체를 얻을 수 없기 때문이다. 이에 대한 완벽한 우회로는 없다. 슈퍼 타입 토큰으로 해결하려는 시도가 있었다.

> 동적 형변환으로 런타임 타입 안전성 확보
```
public <T> void putFavorite(Class<T> type, T instance) {
    favorites.put(Objects.requireNonNull(type), type.cast(instance));
}
```

* Favorites가 사용하는 타입 토큰은 비한정적이지만, 메서드들이 허용하는 타입을 제한하고 싶은 경우 한정적 타입 토큰을 활용하면 된다.