# 🍃 Divary-Spring 🍃

## 👶🏼 Spring Members 👶🏼
<img width="160px" src="https://avatars.githubusercontent.com/u/106726806?v=4"/> | <img width="160px" src="https://avatars.githubusercontent.com/u/105594739?v=4"/> | <img width="160px" src="https://avatars.githubusercontent.com/u/105594739?v=4"/> | <img width="160px" src="https://avatars.githubusercontent.com/u/105594739?v=4"/> | 
|:-----:|:-----:|:-----:|:-----:|
|[송재곤 (아진)](https://github.com/worhs02)|[강영민 (바게트빵)](https://github.com/Baguette-bbang)|[박채연 (므느)](https://github.com/qkrcodus)|[정세린 (후디)](https://github.com/sereene)|
|팀장 👑|팀원 👨🏻‍💻|팀원 👨🏻‍💻|팀원 👨🏻‍💻|
</div>
<br/>


## 🛠️ Development Environment 🛠️
![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=flat-square&logo=Spring&logoColor=white)
![IntelliJJ](https://img.shields.io/badge/Intellij%20Idea-000?logo=intellij-idea&style=flat-square)

## 🥞 Stacks 🥞
| Name          | Description   |
| ------------  | ------------- |
| <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=flat-square&logo=Spring&logoColor=white"> | Java 기반의 웹 애플리케이션을 빠르게 개발할 수 있는 프레임워크|
| <img src="https://img.shields.io/badge/-Git-F05032?style=flat&logo=git&logoColor=white"> | 분산 버전 관리 시스템으로, 코드 히스토리 관리와 협업을 효율적으로 지원|
| <img src="https://img.shields.io/badge/-Notion-000000?style=flat&logo=notion&logoColor=white"> | 작업 관리 및 문서화를 위한 통합 협업 도구|
| <img src="https://img.shields.io/badge/-Discord-5865F2?style=flat&logo=discord&logoColor=white"> | 팀 커뮤니케이션 및 실시간 협업을 위한 음성 및 텍스트 기반 플랫폼|

## 📚 Libraries 📚
| Name         | Version  |  Description        |
| ------------ |  :-----: |  ------------ |
| Lombok | `0.0.0` | 반복되는 Java 코드를 줄이기 위한 어노테이션 기반 코드 생성 도구 |
| JPA| `0.0.0` | 데이터베이스 연동을 위한 ORM 라이브러리 |
| Spring Security|  `0.0.0`  | 인증 및 권한 처리를 위한 보안 프레임워크|


## 💻 Convention 💻

## 🌲 Branch Convention 🌲
1. **기본 브랜치 설정**
    - main : 배포 가능한 안정적인 코드가 유지되는 브랜치
    - develop: 기본 브랜치로, 기능을 개발하는 브랜치
2. **작업 순서**
    
    1. 작업할 이슈 작성
    
    예) `#111 사용자 로그인 기능 구현`
    
    2. 작업 브랜치 생성
        - 기능 개발: `feat/#[이슈번호]-title`
            - ex) feat/#111-login
        - 버그 수정: `fix/#[이슈번호]-title`
            - ex) fix/#111-login
        - 리팩토링: `refac/#[이슈번호]-title`
            - ex) refac/#111-login
    3. **생성한 브랜치에서 작업 수행** 
    4. **원격 저장소에 작업 브랜치 푸시** 
    5. **Pull Request 생성**
    - `develop` 브랜치 대상으로 Pull Request 생성
    - 리뷰어의 리뷰를 받은 후 PR을 승인 받고 `develop` 브랜치에 병합 후 브랜치 삭제
---
## 🧑‍💻 Code Convention 🧑‍💻

[Based](https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html)

**네이밍 규칙**

- **변수/상수**: 카멜케이스 (예: `userName`)
- **클래스/구조체**: 파스칼케이스 (예: `UserProfile`)
- **함수/메서드**: 동사로 시작하며 카멜케이스 (예: `fetchData()`)

 **코드 스타일**

- **명시적 타입 선언**: 가능하면 타입 명시 (예: `var name : String = “name”`)
- **옵셔널 처리**: `guard`나 `if let`을 사용하여 안전하게 언래핑
- **함수 파라미터**: 간결하고 직관적인 이름 사용
---
## 💬 Issue Convention 💬
1. **Feature**: 기능 추가 시 작성
    - **Issue**: ✅ Feature
    - **내용**: 작업하고자 하는 기능을 입력
    - **TODO**:
        - [ ]  구현 내용 입력
    - **ETC**: 논의가 필요한 사항이나 참고 내용 작성
2. **Fix/Bug**: 오류/버그 발생 시 작성
    - **Issue**: 🐞 Fix / Bug
    - **내용**: 발생한 문제 설명
    - **원인 파악**
    - **해결 방안**
    - **결과 확인**
    - **ETC**: 논의할 사항 작성
3. **Refactor**: 리팩토링 작업 시 작성
    - **Issue**: ♻️ Refactor
    - **내용**: 리팩토링이 필요한 부분 작성
    - **Before**: 변경 전 상태 및 이유 설명
    - **After**: 변경 후 예상되는 구조 설명
    - **TODO**:
        - [ ]  변경 내용 입력
    - **ETC**: 논의할 사항 작성
4. **Document**: 문서 작업시 작성
    - **Issue**: 📋 Document
    - **내용**: 작성/변경된 문서
    - **TODO**:
        - [ ]  변경 내용 입력
    - **ETC**: 논의할 사항 작성
---
## 🫷 PR Convention 🫸
```markdown
**🔗 관련 이슈**

연관된 이슈 번호를 적어주세요. (예: #123)

---

**📌 PR 요약**

PR에 대한 간략한 설명을 작성해주세요.

(예: 해당 변경 사항의 목적이나 주요 내용)

---

**📑 작업 내용**

작업의 세부 내용을 작성해주세요.

1. 작업 내용 1
2. 작업 내용 2
3. 작업 내용 3

---

**스크린샷 (선택)**

---

**💡 추가 참고 사항**

PR에 대해 추가적으로 논의하거나 참고해야 할 내용을 작성해주세요. (예: 변경사항이 코드베이스에 미치는 영향, 테스트 방법 등)
```
---
## 🙏 Commit Convention 🙏

- `feat` : 새로운 기능이 추가되는 경우
- `fix` : bug가 수정되는 경우
- `docs` :  문서에 변경 사항이 있는 경우
- `style` : 코드 스타일 변경하는 경우 (공백 제거 등)
- `refac` : 코드 리팩토링하는 경우 (기능 변경 없이 구조 개선)
- `design` : UI 디자인을 변경하는 경우

```spring boot
// Format
[Type]/#[이슈번호]: [Description]

// Example
feat/#1: 로그인 기능 구현
fix/#32: 로그인 api 오류 수정
```
---
## 📁 Foldering Convention 📁
```markdown
📦Divary
┣ 📂domain                
┃ ┗ 📂controller           # API 요청을 받아 처리하는 계층
┃ ┗ 📂service              # 비즈니스 로직 처리
┃ ┗ 📂repository           # 데이터베이스 연동
┃ ┗ 📂exception            # 예외 처리 
┃ ┗ 📂dto                  # 데이터 전송 객체
┣ 📂global                 # 공통 인프라 관리
┃ ┗ 📂auth                 # 인증/인가 관련 처리
┃ ┗ 📂commonm              # 공통 유틸, 예외 처리, 응답 포맷 등
┃ ┗ 📂config               # Spring 설정 파일
┗ 📂resources
  ┗ 📂properties           # application.properties, YAML 등 환경 설정
```
