# Divary Spring Boot 프로젝트

다이빙 서포트 앱 Divary의 Spring Boot 3.5.3 기반 백엔드 API 서버입니다.

## 프로젝트 정보

- **프로젝트명**: Divary Spring Boot
- **Spring Boot 버전**: 3.5.3
- **Java 버전**: 21
- **빌드 도구**: Gradle
- **데이터베이스**: H2 (개발용 인메모리)

## 프로젝트 구조

아래는 기본 구조이고 필요한 도메인을 추가하면 됩니다.

```
divary-spring/
├── build.gradle                    # Gradle 빌드 설정
├── gradle/
│   └── wrapper/                    # Gradle Wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── divary/
│   │   │           ├── DivaryApplication.java          # 메인 애플리케이션 클래스
│   │   │           ├── common/                         # 공통 모듈
│   │   │           │   ├── entity/                     # 공통 엔티티
│   │   │           │   │   └── BaseEntity.java         # 기본 엔티티 (ID, 생성일, 수정일)
│   │   │           │   └── response/                   # 공통 응답 모듈
│   │   │           │       └── ApiResponse.java        # 표준 API 응답 클래스
│   │   │           ├── domain/                         # 도메인 모듈 (추후 도메인마다 controller, dto, service를 가지게 됨)
│   │   │           │   └── system/                     # 시스템 도메인
│   │   │           │       └── controller/             # 시스템 컨트롤러
│   │   │           │           └── SystemController.java
│   │   │           └── global/                         # 전역 설정 모듈
│   │   │               ├── config/                     # 전역 설정
│   │   │               │   └── SwaggerConfig.java      # Swagger API 문서 설정
│   │   │               └── exception/                  # 전역 예외 처리
│   │   │                   ├── BusinessException.java  # 비즈니스 예외 클래스
│   │   │                   ├── ErrorCode.java          # 에러 코드 enum
│   │   │                   └── GlobalExceptionHandler.java # 전역 예외 핸들러
│   │   └── resources/
│   │       ├── application.yml                         # 애플리케이션 설정 (추후 환경에 따라 구분)
```

## 주요 의존성

### Spring Boot Starters

- `spring-boot-starter-data-jpa`: JPA 데이터 액세스
- `spring-boot-starter-validation`: 입력값 검증
- `spring-boot-starter-web`: 웹 애플리케이션
- `spring-boot-starter-thymeleaf`: 템플릿 엔진
- `spring-boot-starter-devtools`: 개발 도구

### API 문서화

- `springdoc-openapi-starter-webmvc-ui:2.8.6`: Swagger UI

### 데이터베이스 (추후 설정된 DB에 따라 추가)

- `com.h2database:h2`: H2 인메모리 데이터베이스

## 패키지 구조 설명

### common 패키지 (여러 곳에서 재사용되는 모듈들)

공통으로 사용되는 모듈들을 포함합니다.

- **entity**: 모든 엔티티가 상속받는 기본 클래스
- **response**: 표준화된 API 응답 형식

### domain 패키지

비즈니스 도메인별로 구분된 모듈들을 포함합니다.

- **system**: 시스템 관련 기능 (헬스체크, 에러 테스트 등)

### global 패키지 (애플리케이션 전체에 영향을 주는 모듈들)

전역적으로 적용되는 설정과 예외 처리를 포함

- **config**: 애플리케이션 전역 설정
- **exception**: 전역 예외 처리 및 에러 코드 정의

## API 문서화 (Swagger)

### Swagger 설정

`SwaggerConfig.java`에서 OpenAPI 3.0 스펙을 기반으로 API 문서를 설정합니다.

```java
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Divary API")
                        .description("다이빙 서포트 앱 Divary의 REST API 문서")
                        .version("v1.0.0"));
    }
}
```

### Swagger 어노테이션 사용법

#### 에러 응답 정의

시스템 컨트롤러에 예제 있습니다. 한 번 살펴봐주세요.

```java
@ApiErrorResponses({
    ErrorCode.INTERNAL_SERVER_ERROR,
    ErrorCode.DATABASE_ERROR
})
```

### Swagger UI 접근

- URL: `http://localhost:8080/swagger-ui/index.html`
- API 문서 JSON: `http://localhost:8080/v3/api-docs`

## 표준 API 응답 형식

모든 API는 `ApiResponse<T>` 형식으로 응답합니다.

```json
{
  "timestamp": "2025-06-30T01:43:07.956473",
  "status": 200,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    // 실제 데이터 👍
  }
}
```

## 에러 처리

### 에러 처리 플로우

```
[컨트롤러 에러 발생]
       ↓
[GlobalExceptionHandler가 자동 캐치]
       ↓
[ErrorCode로 표준화된 에러 정보 추출]
       ↓
[ApiResponse로 일관된 응답 형식 생성]
       ↓
[클라이언트에게 JSON 응답 전달]
```

### 에러 코드 체계

추후 각 도메인에 맞게 추가하시면 됩니다.

- `COMMON_XXX`: 공통 에러
- `VALIDATION_XXX`: 검증 에러
- `BUSINESS_XXX`: 비즈니스 로직 에러
- `DATABASE_XXX`: 데이터베이스 에러

### 전역 예외 처리

`GlobalExceptionHandler`에서 모든 예외를 표준 형식으로 처리합니다.

## 데이터베이스 설정

### H2 콘솔 접근

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:divary`
- Username: `sa`
- Password: (비어있음)

## 개발 가이드라인

### 컨트롤러 작성

1. `@RestController` 어노테이션 사용
2. `@Tag` 어노테이션으로 API 그룹 정의
3. `@Operation` 어노테이션으로 API 설명 추가
4. `ApiResponse<T>` 형식으로 응답

### 예외 처리

1. 비즈니스 로직 예외는 `BusinessException` 사용
2. `ErrorCode` enum에 새로운 에러 코드 추가
3. `GlobalExceptionHandler`에서 처리되지 않는 예외 추가

### 엔티티 작성

1. `BaseEntity`를 상속받아 기본 필드 활용
2. JPA 어노테이션 사용
3. Lombok 어노테이션으로 보일러플레이트 코드 제거

## 환경 설정

### 개발 환경

- Java 21
- Spring Boot 3.5.3
- H2 인메모리 데이터베이스
- Swagger UI 활성화

### 프로덕션 환경

- MySQL 또는 PostgreSQL 데이터베이스가 될 것 같..죠?
- 개발을 마친 후 Swagger UI 비활성화
- 개발을 마친 후 로깅 레벨 조정

## 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.
