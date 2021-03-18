## SNU 전기정보공학부 행정실 안내로봇의 챗봇 앱 개발 (Chatbot app for TEMI the robot)

### [전체적인 프로젝트 구조]

![image](https://user-images.githubusercontent.com/31657141/110227291-190c4f80-7f3a-11eb-9705-507aef327fff.png)


1. 사용자는 앱 UI를 처음으로 접하고 말할 기회가 주어지면 질문을 한다. 테미는 이를 음성인식한다.

2. 테미의 내장된 STT (Speech to Text) 기능을 통해 위의 질문을 텍스트(string) 형태로 변환한다.

3. Google cloud project private 키를 사용하여 행정실 안내를 하는 dialogflow agent 챗봇에 접속하고, 텍스트를 request로 보낸다.

4. 위의 request에 대해서 올바른 response를 가져온다. 이 Response에는 대화 intent, context, 그리고 실제 대답 텍스트가 포함된다.

5. Response 받기가 완료되면 callback 함수가 실행되며 여기서 필요한 response 정보를 정리 및 처리한다.

6. 안드로이드 TTS (Text to Speech)로 사용자에게 최종 대답을 하고,  필요한 경우 테미가 이동하도록 테미 SDK를 통해 지시한다.

<br>

Front end - UI, 사용자 인식, 테미의 이동 및 대답, 음성인식.

Back end - 질문과 대답의 정보 전달, Dialogflow agent, Web hook.

* * *
### [안내로봇 사용 방법]


- 사용자가 테미 앞에 서면 테미는 먼저 말을 건다.

- 이에 말로 대답을 하거나, 제공하는 버튼을 활용하여 행정실에 방문한 용건을 말한다.

제공하는 버튼:

> Page1 button

![image](https://user-images.githubusercontent.com/31657141/110230649-cccf0880-7f55-11eb-95ac-10b19f5ed1f4.png)


> Page2 button

![image](https://user-images.githubusercontent.com/31657141/110230651-d0628f80-7f55-11eb-86b3-a0eaf9090afd.png)



- 대화가 끝나거나 멈추면, 대화시작 버튼을 누르거나 "헤이 테미"라고 말을 걸어 대화를 이어한다.

- 대화나 음성을 종료하고 싶으면, 대화중지 버튼을 누른다.

대화시작 및 대화중지 버튼:

![image](https://user-images.githubusercontent.com/31657141/110230791-d4db7800-7f56-11eb-88c4-333d61b5476b.png)

- 사용자와의 상호작용이 끝나고 5초간 변화가 없는 경우 테미는 행정실 입구로 자동 복귀한다.

- 테미가 이동 중에 멈추게 하면, 원하는 대화를 이어할 수 있다. 이때도 대화가 끝나면 자동 복귀 시퀀스가 실행된다.

* * *
### [안내로봇 관리 방법]


- 테미의 전원 버튼은 화면 뒤 목 부분에 위치한다.

- 테미에 설치된 앱(Dialogflow agent 연동)은 테미가 Wifi에 연결되어 있어야만 사용 가능하다.

<br>

- 관리자를 위해 아래와 같이 앱 상단에 출근 및 퇴근 버튼을 배치한다.

![image](https://user-images.githubusercontent.com/31657141/110227400-8c629100-7f3b-11eb-9b4a-d189d23f2dbf.png)

<br>

- 테미의 출근 (대기) 장소는 행정실 입구 근처이다.

- 테미의 퇴근 장소는 홈베이스가 있는 행정실 입구 근처이다.

<br>

- 테미는 주중에 자동으로 9시에 출근하고, 5시에 퇴근한다.

- 배터리가 15% 아래로 떨어지면 홈베이스로 퇴근하고, 완전히 충전된 후에 다시 행정실 입구로 출근한다.

* * *
### [안내로봇 총 기능 정리]

#### Dialogflow agent 상의 기능

- 업무에 따는 행정실 선생님 자리 안내

- 성함에 따른 행정실 선생님 자리 안내

- 행정실 선생님 부재 여부의 확인 (Web hook)

<br>

- 행정실, 학생센터, 전산실, 새장, 과방 등 건물 내 중요 위치 안내

- 화장실, 정수기 위치 안내

<br>

- 증명서 발급, 우편물 확인 안내

- 전기정보공학부 간략한 소개

<br>

- 오늘의 날씨 안내 (Web hook)

- 윗공대 학식 안내 (Web hook)

- 네이버 백과사전 연동 (Web hook)

<br>

- 자연스러운 일상 대화의 가능 (Small talk)

<br>

- 다국어 설정 (한국어, 영어 선택 가능)

언어 설정 버튼:

![image](https://user-images.githubusercontent.com/31657141/110230731-38b17100-7f56-11eb-86ee-1912a71cedc9.png)

영어 설정 시 버튼:

> Page1 button

![image](https://user-images.githubusercontent.com/31657141/110230750-54b51280-7f56-11eb-8dcc-2f21d3f87685.png)



> Page2 button

![image](https://user-images.githubusercontent.com/31657141/110230754-5aaaf380-7f56-11eb-90ba-ae77b8292261.png)


#### 이외의 추가 기능

- 전기정보공학부 홈페이지, 구글 날씨, 행정실 선생님 연락처 페이지 링크

<br>

- 사용자 체온 측정 및 마스크 착용 여부 감지 기능 (테미와 연동된 테블릿 pc에서 구현)








