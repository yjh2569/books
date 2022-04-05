# 스프링 DI

## 의존

* 의존 : 객체 간의 의존을 의미한다.
  * 한 클래스가 다른 클래스의 메서드를 실행할 때 이를 의존한다고 표현한다.
  * 변경에 의해 영향을 받는 관계를 의미한다고도 할 수 있다.
  
* 의존하는 대상을 구하는 방법
  * 가장 쉬운 방법은 의존 대상 객체를 직접 생성하는 것이다.
  * 이렇게 하면 클래스의 객체를 생성할 때 의존하는 클래스의 객체도 함께 생성된다.
  * 다만 이렇게 하면 유지보수 관점에서 문제점을 유발할 수 있다.
  * 이를 해결하기 위해 DI 혹은 서비스 로케이터를 통해 의존 객체를 구하는 방법이 있다.
  * 스프링에서는 DI를 이용해 의존 객체를 구한다.
  
## DI를 통한 의존 처리

* DI는 의존하는 객체를 직접 생성하는 대신 의존 객체를 전달받는 방식을 사용한다.
  * 생성자를 통해 의존 객체를 전달받음으로써 의존 객체를 주입받는다.

> MemberRegisterService 클래스에 DI 방식을 적용한 경우
```
public MemberRegisterService(MemberDao memberDao) {
    this.memberDao = memberDao;
}

// 객체 생성시 다음과 같이 MemberDao 객체를 전달해야 한다.
MemberDao dao = new MemberDao();
MemberRegisterService svc = new MemberRegisterService(dao);
```

## DI와 의존 객체 변경의 유연함

* 의존 객체를 직접 생성하는 경우
  * MemberRegisterService와 ChangePasswordService가 MemberDao 의존 객체를 직접 생성한다고 가정한다.
  * 만약 회원 데이터의 빠른 조회를 위해 MemberDao를 상속한 CachedMemberDao 클래스를 새로 만들었다고 할 때 이를 사용하려면 MemberRegisterService와 ChangePasswordService 클래스의 코드에서 MemberDao 클래스의 생성자가 아닌 CachedMemberDao 클래스의 생성자를 호출하도록 변경해야 한다.
  
* 위와 동일한 상황에서 DI를 사용하면 수정할 코드가 줄어든다.
  * 생성자를 통해 의존 객체를 주입받도록 구현한다면, MemberDao 대신 CachedMemberDao를 사용하도록 수정할 때 MemberDao 객체를 생성하는 코드만 변경하면 된다. 따라서 변경할 코드가 한 곳에 집중된다.