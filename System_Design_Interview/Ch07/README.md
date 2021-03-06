# 분산 시스템을 위한 유일 ID 생성기 설계

* 단일 서버에서는 관계형 데이터베이스에서 auto_increment 속성이 설정된 기본 키를 쓰면 된다.
  * 하지만 분산 환경에서는 이러한 방법이 통하지 않는다.

## 개략적 설계안 제시 및 동의 구하기

### 다중 마스터 복제(multi-master replication)

* 데이터베이스의 auto_increment 기능을 활용하나, ID 값을 구할 때 1씩 증가시키지 않고 데이터베이스 서버 수만큼 증가시킨다.
* 데이터베이스 수를 늘리면서 초당 생산 가능 ID 수도 늘릴 수 있어 규모 확장성 문제를 어느 정도 해결할 수 있다.
* 단점
  * 여러 데이터 센터에 걸쳐 규모를 늘리기 어렵다.
  * ID의 유일성은 보장되지만 그 값이 시간 흐름에 맞춰 커지지 않는다.
  * 서버를 추가하거나 삭제할 때 정상적으로 동작하지 않을 수 있다.

### UUID

* UUID : 컴퓨터 시스템에 저장되는 정보를 유일하게 식별하기 위한 128비트 수
* 충돌 가능성이 지극히 낮다.
* 각 웹 서버는 별도의 ID 생성기를 통해 독립적으로 ID를 생성한다.
* 장점
  * 단순하고 동기화 이슈도 없다.
  * 규모 확장이 쉽다.
* 단점
  * ID가 128비트로 길다.
  * ID를 시간순으로 정렬할 수 없다.
  * ID에 숫자가 아닌 값이 포함될 수 있다.

### 티켓 서버(ticket server)

* auto_increment 기능을 갖춘 데이터베이스 서버, 즉 티켓 서버를 중앙 집중형으로 하나만 사용한다.
* 장점
  * 유일성이 보장되는 숫자로만 구성된 ID를 쉽게 만들 수 있다.
  * 구현하기 쉽고, 중소 규모 애플리케이션에 적합하다.
* 단점
  * 티켓 서버가 SPOF가 되기에 장애가 발생하면 해당 서버를 이용하는 모든 시스템이 영향을 받는다.

### 트위터 스노플레이크 접근법

* 생성해야 하는 ID의 구조를 여러 절(section)으로 분할한다.
* ID의 구조
  * 사인(sign) 비트 : 1비트를 할당하고, 음수와 양수를 구별할 때 사용한다.
  * 타임스탬프(timestamp) : 41비트를 할당하고, 기원 시각(epoch) 이후로 몇 밀리초가 경과했는지를 나타낸다.
  * 데이터센터 ID : 5비트를 할당하므로, 32개의 데이터센터까지 지원 가능하다.
  * 서버 ID : 5비트를 할당하므로, 32개의 서버까지 사용 가능하다.
  * 일련번호 : 12비트를 할당하고, 각 서버에서 ID를 생성할 때마다 1만큼 증가시킨다. 1밀리초가 경과할 때마다 0으로 초기화한다.

## 상세 설계

* 트위터 스노플레이크 접근법을 사용해 보다 상세한 설계를 진행한다.
* 데이터센터 ID와 서버 ID는 시스템 시작 시 결정되고 운영 중에는 바뀌지 않는다.
* 타임스탬프와 일련번호는 ID 생성기가 돌고 있는 중에 만들어지는 값이다.

### 타임스탬프

* 타임스탬프는 시간의 흐름에 따라 점점 큰 값을 갖게 되므로 시간 순으로 정렬 가능하다.
* 타임스탬프의 값을 UTC 시각으로, UTC 시각을 타임스탬프로 바꿀 수 있다.
* 41비트로 표현할 수 있는 타임스탬프의 최댓값은 대략 69년이다.
* 69년이 지나면 기원 시각을 바꾸거나 ID 체계를 다른 것으로 이전하여야 한다.

### 일련번호

* 12비트로 4096개의 값을 가질 수 있다.
* 어떤 서버가 같은 밀리초 동안 하나 이상의 ID를 만들어 낸 경우에만 0보다 큰 값을 가진다.

## 마무리

* 추가 논의 사항
  * 시계 동기화(clock synchronization)
    * ID 생성 서버들이 전부 같은 시계를 사용하지 않을 수도 있다.
    * 하나의 서버가 여러 코어에서 실행될 경우, 여러 서버가 물리적으로 독립된 여러 장비에서 실행되는 경우
    * 보통 NTP(Network Time Protocol)을 통해 이 문제를 해결한다.
  * 각 절의 길이 최적화
    * 동시성(concurrency)이 낮고 수명이 긴 애플리케이션이면 일련변호 절의 길이를 줄이고 타임스탬프 절의 길이를 늘린다.
  * 고가용성(high availability) : ID 생성기는 필수 불가결(mission critical) 컴포넌트이므로 아주 높은 가용성을 제공해야 한다.
