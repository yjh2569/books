# 채팅 시스템 설계

## 문제 이해 및 설계 범위 확정

* 채팅 앱은 여러 가지 케이스가 있기 때문에 원하는 앱이 무엇인지 정확하게 정해야 한다.
  * 1:1 채팅에 집중하는 앱 : 페이스북 메신저, 위챗, 왓츠앱
  * 그룹 채팅에 중점을 둔 업무용 앱 : 슬랙
  * 대규모 그룹 소통 및 응답 지연이 낮은 음성 채팅 앱 : 디스코드
* 요구 기능 예시
  * 응답 지연이 낮은 1:1 채팅 기능
  * 최대 100명까지 참여 가능한 그룹 채팅 기능
  * 사용자의 접속상태 표시 기능
  * 다양한 단말 지원 및 하나의 계정으로 여러 단말에 동시 접속 지원
  * 푸시 알림

## 개략적 설계안 제시 및 동의 구하기

* 클라이언트는 모바일 앱이나 웹 어플리케이션으로 클라이언트 간에 직접 통신은 없다.
* 대신 클라이언트는 채팅 서비스와 통신한다.
* 채팅 서비스가 제공하는 기능
  * 클라이언트들로부터 메시지 수신
  * 메시지 수신자 결정 및 전달
  * 수신자 미접속 상태인 경우 접속할 때까지 해당 메시지 보관
* 채팅 서비스 통신 프로토콜
  * HTTP 프로토콜을 이용해 보내는 경우
    * 클라이언트는 채팅 서비스에 HTTP 프로토콜로 연결한 다음 메시지를 보내 수신자에게 해당 메시지를 전달하라고 알린다.
    * 채팅 서비스 접속 시 클라이언트와 서버 사이의 연결을 끊지 않고 유지하기 위해 keep-alive 헤더를 사용하면 효율적이다.
      * keep-alive 헤더 : 송신자가 연결에 대한 타임아웃과 요청 최대 개수를 어떻게 정했는지를 알려준다.
      * TCP 접속 과정 내 핸드셰이크 횟수도 줄일 수 있다.
    * HTTP는 클라이언트가 연결을 만드는 프로토콜이고, 서버에서 클라이언트로 임의 시점에 메시지를 보내는 데는 쉽게 쓰일 수 없다.

### 폴링(polling)

* 클라이언트가 주기적으로 서버에게 새 메시지가 있는지 물어보는 방법
* 답해줄 메시지가 없는 경우 서버 자원이 불필요하게 낭비된다.

### 롱 폴링(long polling)

* 클라이언트는 새 메시지가 반환되거나 타임아웃될 때까지 연결을 유지한다.
* 새 메시지를 받으면 연결 종료 후 서버에 새로운 요청을 보내 모든 절차를 다시 시작한다.
* 송신 클라이언트와 수신 클라이언트가 다른 채팅 서버에 접속하면 문제가 발생한다.
* 서버 입장에서는 클라이언트가 연결을 해제했는지 알 방법이 없다.
* 메시지를 많이 받지 않는 클라이언트도 결국 타임아웃이 일어날 때마다 주기적으로 서버에 다시 접속해 비효율적이다.

### 웹 소켓(WebSocket)

* 서버가 클라이언트에게 비동기 메시지를 보낼 때 가장 많이 사용하는 기술
* 웹소켓 연결은 클라이언트가 시작한다.
* 한 번 맺어진 연결은 항구적이고 양방향이다.
* 처음에는 HTTP 연결을 하나 특정 핸드셰이크 절차를 거쳐 웹소켓 연결로 업그레이드된다.
* 웹소켓 연결에서는 서버가 클라이언트에게 비동기적으로 메시지를 전달할 수 있다.
* 방화벽이 있는 환경에서도 잘 동작한다. HTTP 또는 HTTPS 프로토콜이 사용하는 기본 포트번호를 그대로 쓰기 때문이다.
* 웹소켓을 사용하면 메시지 송수신 시 동일한 프로토콜을 사용할 수 있어 설계뿐 아니라 구현도 단순하고 직관적이다.
* 다만, 웹소켓 연결은 항구적으로 유지해야 하므로 서버 측에서 연결 관리를 효율적으로 해야 한다.

### 개략적 설계안

* 채팅 서비스를 제외한 다른 부분에서는 기능들을 HTTP상에서 구현해도 된다.
* 무상태 서비스
  * 많은 웹사이트와 앱이 보편적으로 제공하는 기능
  * 로드밸런서 뒤에 위치해 요청들이 그 경로에 맞는 서비스로 정확하게 전달된다.
  * 서비스들 중 상당수가 시장에 완제품으로 나와 있어 직접 구현하지 않아도 쉽게 사서 쓸 수 있다.
  * 서비스 탐색(service discovery) 서비스 : 클라이언트가 접속할 채팅 서버의 DNS 호스트 명을 클라이언트에게 알려준다.
* 상태 유지 서비스
  * 채팅 서비스가 유일한 상태 유지 서비스
  * 클라이언트는 서버가 살아 있는 한 다른 서버로 연결을 변경하지 않는다.
  * 서비스 탐색 서비스가 채팅 서비스와 협력해 특정 서버에 부하가 몰리는 것을 막는다.
* 제3자 서비스 연동
  * 푸시 알림
  * 새 메시지를 받았다면 앱이 실행 중이지 않더라도 알림을 받을 수 있어야 한다.
* 규모 확장성
  * 서버 한 대로 모든 기능을 구현하면 대규모 트래픽 서비스를 지원할 수 없고, SPOF 문제가 발생하므로 적합하지 않다.
* 개략적 설계안
  * 채팅 서버 : 클라이언트 간의 메시지 중계 역할
  * 접속상태 서버 : 사용자의 접속 여부 관리
  * API 서버 : 로그인, 회원가입, 프로파일 변경 등 채팅을 제외한 나머지 전부를 처리
  * 알림 서버 : 푸시 알림 전송
  * 키-값 저장소 : 채팅 이력 보관. 시스템 접속 사용자는 이전 채팅 이력을 전부 보게 될 것이다.
* 저장소
  * 사용자 프로파일, 설정, 친구 목록같은 일반적인 데이터는 관계형 데이터베이스에 보관한다.
  * 채팅 이력은 키-값 저장소에 저장한다.
    * 수평적 규모 확장이 쉽다.
    * 데이터 접근 지연시간이 낮다.
    * 관계형 데이터베이스는 데이터 가운데 롱 테일에 해당하는 부분을 잘 처리하지 못한다.
      * 롱 테일 : 주요 데이터가 아닌 퍼져 있는 비주요 데이터
    * 이미 많은 안정적인 채팅 시스템이 키-값 저장소를 채택하고 있다.

### 데이터 모델

* 1:1 채팅을 위한 메시지 테이블
  * 기본 키인 message_id로 메시지 순서를 정한다.
  * created_at을 사용해서 메시지 순서를 정하면 동시에 만들어진 메시지를 정렬할 수 없다.
* 그룹 채팅을 위한 메시지 테이블
  * (channel_id, message_id)의 복합 키를 기본 키로 사용한다.
  * channel은 채팅 그룹과 같은 뜻이다.
  * channel_id는 파티션 키로도 사용한다. 그룹 채팅에 적용될 모든 질의는 특정 채널을 대상으로 하기 때문이다.
* 메시지 ID
  * 메시지들의 순서도 표현해야 하므로 다음과 같은 속성을 만족해야 한다.
    * 고유값이어야 한다.
    * 정렬 가능하며 시간 순서와 일치해야 한다.
  * RDBMS라면 auto_increment가 가능하지만 NoSQL에서는 해당 기능을 제공하지 않는다.
  * 스노플레이크 같은 전역적 64bit 순서 번호(sequence number) 생성기를 이용한다.
  * 지역적 순서 번호 생성기(local sequence number generator)를 이용한다.
    * ID 유일성은 같은 그룹 내에서만 보증된다.

## 상세 설계

### 서비스 탐색

* 서비스 탐색 기능은 클라이언트에게 가장 적합한 채팅 서버를 추천한다.
* 추천 기준은 클라이언트의 위치, 서버의 용량 등이 있다.
* 서비스 탐색 기능을 구현하는데 쓰이는 오픈 소스 솔루션으로 아파치 주키퍼(Apache Zookeeper)가 있다.
* 사용 가능한 모든 채팅 서버를 여기 등록해 두고, 클라이언트가 접속을 시도하면 기준에 따라 최적의 채팅 서버를 골라주면 된다.
* 동작 방식
  * 사용자 A가 시스템에 로그인 시도
  * 로드밸런서가 로그인 요청을 API 서버들 가운데 하나로 전송
  * API 서버가 사용자 인증 완료 후 서비스 탐색 기능이 동작해 해당 사용자를 서비스할 최적의 채팅 서버를 선택해 사용자 A에게 반환
  * 사용자 A는 그 채팅 서버와 웹 소켓 연결

### 메시지 흐름

* 1:1 채팅 메시지 처리 흐름
  * 사용자 A가 채팅 서버 1로 메시지 전송
  * 채팅 서버 1은 ID 생성기를 사용해 해당 메시지의 ID 결정
  * 채팅 서버 1은 해당 메시지를 메시지 동기화 큐로 전송
  * 메시지를 키-값 저장소에 저장
  * 사용자 B가 접속 중인 경우 메시지를 사용자 B가 접속 중인 채팅 서버(채팅 서버 2)로 전송
  * 그렇지 않으면 푸시 알림 메시지를 푸시 알림 서버로 전송
  * 채팅 서버 2는 메시지를 사용자 B에게 전송
* 여러 단말 사이의 메시지 동기화
  * 각 단말은 cur_max_message_id라는 변수를 유지하는데, 해당 단말에서 관측된 가장 최신 메시지의 ID이다.
  * 새 메시지 간주 조건
    * 수신자 ID가 현재 로그인한 사용자 ID와 같다.
    * 키-값 저장소에 보관된 메시지로서, 그 ID가 cur_max_message_id보다 크다.
* 소규모 그룹 채팅에서의 메시지 흐름
  * 한 사용자가 메시지를 전송하면 나머지 사용자의 메시지 동기화 큐에 해당 메시지가 복사된다.
  * 새로운 메시지가 왔는지 확인하려면 큐만 보면 되기에 메시지 동기화 플로가 단순하다.
  * 그룹이 크지 않으면 메시지를 수산자별로 복사해야 큐에 넣는 작업의 비용이 문제가 되지 않는다.
  * 수신자 관점에서는 여러 사용자로부터 오는 메시지를 수신할 수 있어야 한다.

### 젭속상태 표시

* 접속상태 서버는 클라이언트와 웹소켓으로 통신하는 실시간 서비스의 일부다.
* 사용자 로그인
  * 접속상태 서버는 로그인한 사용자의 상태와 last_active_at 타임스탬프 값을 키-값 저장소에 저장한다.
  * 이 절차가 끝나면 해당 사용자는 접속 중인 것으로 표시된다.
* 로그아웃
  * 키-값 저장소에 보관된 사용자 상태가 online에서 offline으로 바뀐다.
  * 이 절차가 끝나면 해당 사용자는 미접속 상태로 표시된다.
* 접속 장애
  * 인터넷 연결이 끊어지면 클라이언트와 서버 사이에 맺어진 웹소켓 같은 지속성 연결도 끊어진다.
  * 이러한 경우 사용자를 오프라인 상태로 표시하고 연결이 복구되면 온라인 상태로 변경하면 되지만, 짧은 네트워크 장애의 경우 오버헤드를 발생시킨다.
  * 따라서 박동(heartbeat) 검사를 통해 해당 문제를 해결한다.
    * 온라인 상태의 클라이언트가 주기적으로 박동 이벤트를 접속상태 서버로 보낸다.
    * 마지막 이벤트를 받은지 x초 이내에 또 다른 박동 이벤트 메시지를 받으면 해당 사용자의 접속 상태를 온라인으로 유지하고, 그렇지 않으면 오프라인 상태로 바꾼다.
* 상태 정보의 전송
  * 상태정보 서버는 발행-구독 모델(publish-subscribe model)을 사용한다.
  * 각각의 친구관계마다 채널을 하나씩 두고 한 사용자의 접속상태가 바뀌면 그 사실을 친구관계 채널에 전달한다.
  * 클라이언트와 서버 사이의 통신에는 실시간 웹소켓을 사용한다.
  * 그룹 크기가 작은 경우에는 괜찮지만 큰 경우에는 비용이나 시간이 많이 들게 된다.
  * 이러한 성능 문제는 사용자가 그룹 채팅에 입장하는 순간에만 상태 정보를 읽어가게 하거나, 친구 리스트에 있는 사용자의 접속상태를 갱신하고 싶으면 수동으로 하도록 유도하면 개선할 수 있다.

## 마무리

* 추가 논의 내용
  * 미디어 지원 : 압축 방식, 클라우드 저장소, 섬네일 생성 등
  * 종단 간 암호화 : 메시지는 발신인과 수신인 이외에 아무도 볼 수 없게 한다.
  * 캐시 : 이미 읽은 메시지를 클라이언트에 캐시해 서버와 주고받는 데이터 양 감소
  * 로딩 속도 개선 : 데이터, 채널 등을 지역적으로 분산하는 네트워크 구축
  * 오류 처리
    * 채팅 서버 오류 : 채팅 서버에 장애 발생 시 서비스 탐색 기능이 동작해 클라이언트에게 새로운 서버 배정
    * 메시지 재전송 : 재시도나 큐를 이용해 메시지의 안정적 전송 보장
