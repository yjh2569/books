# 컴퓨터 내부의 언어 체계

## Boolean Algebra

* NOT : 논리적 반대
* AND : 두 개 이상의 모든 비트가 참인 경우에만 참을 반환하는 연산
* OR : 두 개 이상의 비트에 대해 한 비트라도 참이면 참을 반환하는 연산
* XOR : 배타적 OR. 첫 번째 비트와 두 번째 비트가 다른 경우에만 참을 반환하는 연산

## 정수를 비트로 표현하는 방법

* 가장 작은 유효 비트(least significant bit, LSB) : 2진수에서 가장 오른쪽의 비트
* 가장 큰 유효 비트(most significant bit, MSB) : 2진수에서 가장 왼쪽의 비트
* leading zero : 컴퓨터가 미리 정해진 수의 비트를 한 덩어리로 사용하도록 만들어졌기 때문에 2진수를 표현할 때 항상 일정한 개수의 비트를 사용해 값을 표현하는 경우가 종종 있다. 이때 실제 수를 2진수로 표현하는데 불필요하지만 해당 비트 수를 맞추기 위해 맨 앞에 오는 0들을 leading zero라고 한다. 
> 16비트 컴퓨터에서 3을 2진수로 표현하는 경우 '11'로도 충분하지만 16비트를 맞추기 위해 앞에 0을 붙여 '0000000000000011'로 표현한다.

### 음수 표현

* 부호와 크기 표현법 : 한 비트를 부호에 사용하고 나머지 비트를 수의 크기를 표현하는데 사용하는 방법. 0을 표현하는 방법이 두 가지라 비용이 낭비되고, XOR과 AND를 통한 덧셈 계산이 불가능해 사용하지 않는다.
* 1의 보수 표현법 : 음수를 표현할 때 양수의 모든 비트를 뒤집는 방법. 0을 표현하는 방법이 두 가지라 비용이 낭비되고, MSB 쪽에서 올림이 발생하는 경우를 해결해야 하기에 사용하지 않는다.
> 4비트 연산에서 +3과 -3은 각각 0011과 1100으로, +5과 -5는 각각 0101, 1010으로 표현한다.
  * 순환 올림(end-around carry) : 1의 보수 표현법에서 MSB 쪽에서 올림이 발생하는 경우 LSB로 올림을 전달하는 것
  > 0010(+2)와 1110(-1)을 더 하면 MSB에서 올림이 발생하는데, 이 경우 LSB로 올림을 전달해 계산한다. 그러면 0001(+1)로 정상적인 결과를 얻을 수 있다.
* 2의 보수 표현법 : 어떤 양수의 음수 표현을 얻고자 할 때 해당 양수에 어떤 수를 더해야 0이 나올지를 생각하는 방법. NOT 연산을 취한 뒤 1을 더하면 된다. MSB에서 올림이 발생하면 해당 값은 버린다. 0을 표현하는 방법이 하나뿐이고, XOR과 AND를 통한 덧셈 계산이 가능하기에 해당 표현법을 많이 사용한다.
> -2에 대한 표현은 0010(+2)의 비트를 뒤집고 1을 더하면 얻을 수 있다(1110).

## 실수를 표현하는 방법

* 고정소수점 표현법 : 2진 소수점의 위치를 임의로 정하고 소수점 앞의 비트는 정수를, 소수점 뒤의 비트는 소수를 표현하는 방법. 쓸모 있는 범위의 실수값을 표현하기 위해 필요한 비트 개수가 많아 잘 사용하지 않는다.
* 부동소수점 표현법 : 가수와 지수 부분으로 나눠 실수를 표현하는 방법

## 2진 코드화한 10진수

* 2진 코드화한 10진수(binary-coded decimal, BCD) : 4비트를 사용해 10진 숫자를 표현하는 방법. 낭비되는 비트가 많아 잘 사용하지 않으나 디스플레이나 가속도 센서 등이 사용하는 경우가 있다.
> 12를 BCD로 표현하면 0001 0010이다. 이는 십의 자리에 있는 1을 0001로, 일의 자리에 있는 2를 0010으로 표현한 것이다.

## 2진수를 다루는 쉬운 방법

* 8진 표현법 ; 2진수 비트들을 3개씩 그룹으로 묶는 방법
* 16진 표현법 : 2진수 비트들을 4개씩 그룹으로 묶는 방법. 컴퓨터 내부가 8비트의 배수를 사용해 만들기에 4개씩 그룹으로 묶어 사용하는 16진 표현법이 많이 사용된다. 10~15의 숫자는 a~f로 표현한다.
> 11010011111111000001은 4개의 비트씩 그룹으로 묶으면 1101/0011/1111/1100/0001이고, 이를 16진 표현법으로 나타내면 d3fc1이다.
* 프로그래밍 언어에서는 8진 숫자는 0으로 시작하는 숫자로, 10진 숫자는 1~9로, 16진 숫자는 0x로 시작하는 숫자로 구분한다.

## 비트 그룹의 이름

* 니블(nibble)-4비트, 바이트(byte)-8비트, 하프 워드(half-word)-16비트, 워드(word)-32비트, 더블 워드(double word)-64비트
* 워드(word) : 컴퓨터가 설계상 자연스럽게 사용할 수 있는, 즉 빠르게 처리할 수 있는 비트 묶음의 크기

## 텍스트 표현

* 정보 교환을 위한 미국 표준 코드(ASCII) : 키보드에 있는 모든 기호에 대해 7비트 수 값을 할당
* 유니코드(Unicode) : 영어 뿐만 아니라 다른 나라의 문자도 표현하기 위한 16비트의 코드 표준
* 인코딩(encoding) : 다른 비트 패턴을 표현하기 위해 사용하는 비트 패턴. 유니코드는 문자 코드에 따라 각기 다른 인코딩을 사용한다.
* 유니코드 변환 형식 8비트(UTF-8) : 유니코드를 위한 가변 길이 문자 인코딩 방식. 유니코드 문자를 8비트 덩어리의 시퀀스로 인코딩한다.

## 문자를 사용한 수 표현

* QP(Quoted-Printable) 인코딩 : 8비트 데이터를 7비트 데이터만 지원하는 통신 경로를 통해 송수신하기 위한 인코딩 방법. '=' 다음에 바이트의 각 니블을 표현하는 16진 숫자 2개를 추가해 8비트값을 표현한다. 1바이트를 표현하기 위해 3바이트를 사용하기에 비효율적이다.
* base64 인코딩 : 3바이트 데이터 24비트를 4개의 6비트 덩어리로 나누고, 각 덩어리의 6비트에 출력 간으한 문자를 할당해 표현하는 방법. 3바이트 조합을 4바이트 조합으로 변환할 수 있으나 원본 데이터 길이가 3바이트의 배수라는 보장이 없기에 패딩 문자(2바이트가 남으면 '=', 1바이트가 남으면 '=='를 붙임)를 도입해 이런 문제를 해결한다.
* URL 인코딩 : % 뒤에 어떤 문자의 16진 표현을 덧붙이는 방식으로 문자를 인코딩하는 방법
