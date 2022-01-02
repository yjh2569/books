# 데이터 구조와 처리

## 기본 데이터 타입

* 기본 데이터 타입은 크기(비트 수)와 해석(부호, 문자, 포인터, 불리언 등)의 두 가지 측면이 존재한다.
* 포인터 : 컴퓨터 아키텍처에 따라 결정되는 크기와 부호가 없는 정수로 메모리 주소로 해석된다.
* 일부 언어는 잘못된 포인터 사용으로 인한 오류를 막기 위해 참조라는 더 추상적인 개념을 구현하기도 한다.

## 비트맵

* 비트의 배열
* 기본 연산으로 비트 설정(set), 비트 지우기(clear), 비트가 0인지 검사하기, 비트가 1인지 검사하기가 있다.
* 마스크 : 비트맵의 특정 부분을 들여다볼 수 있는 비트 패턴
* 어떤 자원이 사용 가능하거나 사용 중인지 여부를 나타낼 때 유용하게 사용할 수 있다.

## 문자열

* 문자열 연산 시 가장 큰 문제가 되는 것은 문자열의 길이다. 문자열은 보통 가변 문자열로 길이가 자주 변하기 때문이다.
* 이를 해결하기 위해서 문자열 데이터 안에 문자열 길이를 포함하거나, 문자열 끝에 문자열 터미네이터 NUL(0)을 넣는다.

## 복합 데이터 타입

* 구조체(struct) : 여러 데이터 타입을 한꺼번에 저장할 수 있는 데이터 구조
* 멤버(member) : 구조체에 저장된 각 데이터
* 메모리 정렬을 지키기 위해 구조체 중간에 패딩(padding)을 추가하기도 한다. 메모리 정렬을 지킨다는 것은 구조체 내의 어떤 데이터가 두 개의 메모리 공간에 걸치는 경우가 없어야 한다는 것이다.
* 공용체(union) : 같은 메모리 공간이나 내용을 여러 가지 관점으로 바라보는 데이터 구조
> 색을 표현할 때 8비트값을 4개 사용했었는데, 이를 공용체로 표현할 때 32비트의 color로 표현하기도 하고 components.red, components.green, components.blue, components.alpha로 나누어 표현하기도 한다.

## 동적 메모리 할당

* 프로그램 데이터 공간 중 힙 영역은 MMU가 있는 시스템에서 런타임 라이브러리가 프로그램에게 필요한 메모리 용량을 판단해 요청한다.
* 브레이크(break) : 프로그램이 사용할 수 있는 메모리의 끝
* 연결 리스트 노드 등 동적인 대상에 사용할 메모리를 힙에서 얻는다.
* C에서는 malloc과 free 함수를 통해 힙을 관리한다.

## 가비지 컬렉션

* 자바나 자바스크립트는 malloc이나 free를 하지 않고 가비지 컬렉션을 구현함으로써 동적 메모리 할당을 지원한다.
* 자바와 같은 언어는 포인터 대신 참조를 사용하는데, 참조는 포인터를 추상화해서 기능은 거의 비슷하나 실제 메모리 주소를 노출하지는 않는다.
* 가비지 컬렉션을 사용하는 언어는 데이터 요소를 만들 때 이 요소가 사용할 메모리도 할당하는 new 연산자를 제공하지만, 데이터 요소를 삭제하는 경우에 대응하는 연산자는 없다. 대신, 언어의 런타임 환경이 변수 사용을 추적해 더 이상 사용하지 않는 메모리를 자동으로 해재한다.
* 프로그래머가 가비지 컬렉션을 제어할 수 없고, 불필요한 참조가 남는 경우 프로그램이 메모리를 더 많이 사용하는 등 몇몇 문제점이 존재한다.

## 대용량 저장장치

* 디스크의 기본 단위는 블록이고, 연속적인 블록을 클러스터라고 부른다.
* 디스크에서는 파일 이름을 통해 파일의 데이터가 저장된 디스크 블록을 찾는다.
* 유닉스에서는 블록 중 일부를 아이노드(inode)로 따로 지정해 파일에 대한 여러 가지 정보와 파일의 데이터가 들어 있는 블록에 대한 인덱스를 저장한다.
* 아이노드에는 직접 블록, 간접 블록, 2중 간접 블록, 3중 간접 블록을 사용해 디스크 내 많은 파일들을 관리한다.
* 아이노드에는 디렉터리를 파일 유형 중 하나로 추가해 계층적 파일 시스템을 구현한다. 디렉터리는 파일 이름과 파일 데이터를 가리키는 아이노드를 연결해준다.
* 여러 아이노드가 같은 블록을 참조할 수 있다. 각 참조를 링크라고 부른다. 이로 인해 같은 파일이 여러 디렉터리에 나타날 수 있다.
* 디렉터리에 대해서도 링크를 할 수 있는데, 이를 심볼릭 링크라고 한다.
* 가용 공간을 추적하기 위해 각 블록을 1비트로 표현하는 비트맵을 사용한다.
* 파일 시스템 그래프와 가용 공간을 나타내는 비트맵 사이에 동기화가 깨질 수 있다. 이를 해결하기 위해 저널링 파일 시스템이 만들어졌다.

## 데이터베이스

* 데이터베이스 : 정해진 방식으로 조직화된 데이터 모음
* 데이터베이스 관리 시스템(DBMS) : 데이터베이스에 정보를 저장하고 읽어올 수 있게 하는 프로그램
* B 트리라는 데이터 구조를 활용해 데이터를 효율적으로 저장한다.

## 데이터 이동

* 데이터 복사는 여러 경우에 대해 이루어지는 주요 연산 중 하나로, 이를 효율적으로 하는 게 중요하다.
* 단순히 루프를 통해 각 바이트를 변화시키는 경우 메모리를 변화시키는 시간보다 인덱스를 유지하고 길이를 갱신하기 위한 시간이 더 걸린다. 따라서 루프 언롤릴 기법을 사용해 좀더 효율적인 메모리 변화를 시도한다.
* 루프 언롤링 : 루프를 통해 구현한 작업에 대해 일부 루프를 펼쳐서 작성하는 것
> 2개의 바이트에 대해 루프 언롤링을 한 뒤 길이를 갱신하는 작업을 한 번으로 줄임으로써 전체적인 길이 갱신 시간을 줄일 수 있다.
* 루프 언롤링을 활용한 예시로 더프의 장치가 있다. 더프의 장치는 루프를 여덟 번 펼치고 남은 바이트 수에 따라 적당한 위치부터 펼친 루프의 단계를 시작한다.
* 효율성을 높이기 위해 64비트 기계라면 한꺼번에 8바이트를 바꾸는 방법도 있다.

## 벡터를 사용한 I/O

* 데이터를 복사해서 전달하는 것이 아닌 크기와 데이터에 대한 포인터로 이뤄진 벡터를 운영체제에 넘겨 데이터를 쓰거나 읽는다.
* 수집 : 벡터를 활용해 여러 위치에서 데이터를 모아서 쓰는 행위
* 분산 : 벡터를 활용해 여러 위치로 데이터를 분산시켜 읽는 행위

## 정렬

* 정렬 대상이 포인터 크기보다 크다면 데이터를 직접 정렬하는 대신 데이터를 가리키는 포인터를 재배열하는 방식으로 정렬한다.
* 정렬을 할 때 데이터를 비교하는 방법과 관련된 함수가 필요한 경우가 있다. 특히 여러 데이터 타입을 가지는 노드들을 정렬해야 하는 경우에는 어떤 데이터를 기준으로 정렬해야 할지를 결정해야 한다.
* 문자열의 경우 앞에서부터 한 문자씩 차례로 비교하는 방식을 사용한다. 다만 이러한 방식은 아스키 코드에서는 잘 작동하지만 다른 로케일(지역)을 지원해야 하면 문제가 발생할 수도 있다.

## 해시

* 검색에 사용할 키를 해시 함수에 적용한 결과에 따라 데이터를 메모리에 저장한다.
* 해시 테이블 : 해시 함수의 결과를 배열 인덱스로 활용하는 배열. 배열의 각 원소를 버킷이라 한다.
* 충돌 : 해시 함수에 의해 한 버킷에 두 개 이상의 데이터가 대응되는 경우
* 단일 연결 리스트를 활용한 해시 체인을 사용해 이런 문제를 해결할 수 있다. 삽입 속도를 빠르게 하기 위해 체인의 맨 앞에 원소를 추가하거나, 검색 시간을 빠르게 하기 위해 체인에 넣을 때 삽입 정렬로 원소를 넣는다.

## 효율성과 성능

* 근래에 들어서 전자 장치의 값이 줄어들어 성능과 효율성이 서로 분리됐다. 즉, 덜 효율적인 알고리즘을 돌려도 더 많은 프로세스를 사용해 더 나은 성능을 얻는 경우가 많아졌다.
* 데이터베이스 샤딩(sharding)(수평 파티셔닝(horizontal partitioning)) : 데이터베이스를 각각 다른 기계에서 실행되는 여러 샤드로 나누는 방식
  * 인터페이스를 통해 요청이 들어온 데이터베이스 연산을 모든 샤드에 전달한다.
  * 컨트롤러가 결과를 하나로 모은다.
  * 작업을 여러 작업자로 나눠 수행할 수 있어 성능이 향상된다.
  * 샤딩의 변종으로 맵리듀스가 있다. 맵리듀스는 컨트롤러가 중간 결과를 모으는 방법을 코드로 직접 작성할 수 있게 해준다.