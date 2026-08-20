# 💊 메디패스 (Medipass) — Backend

> **약 봉투 한 장이면, 해외 반입 준비가 끝납니다.**
>
> _"이 약, 그 나라에 가져가도 되나요?"_

해외여행에 약을 가져갈 때 **어떤 성분이 규제 대상인지**, **어떤 서류를 준비해야 하는지**를
정부 원천 데이터 기준으로 판정해 주는 여행자 의약품 반입 준비 서비스의 백엔드입니다.

약 봉투를 촬영하면 **OCR**로 약을 읽어 **식약처 의약품 정보**와 매칭하고,
여행 국가를 고르면 **성분별 신호등 판정**과 **준비 서류 체크리스트**를 만들어 줍니다.
서류는 **AWS S3**에 보관하고, 현지에서 문제가 생기면 **재외공관 연락처와 현지어 설명문**까지 제공합니다.

---

## 🔹 주요 기능

### 1. 🔐 인증 / 인가
- 카카오 OAuth2 소셜 로그인
- 로그인 성공 시 JWT 발급 → 이후 모든 요청은 `Authorization: Bearer <token>` 헤더 사용
- 내 계정 정보 조회

### 2. 📷 약 봉투 인식 & 복약카드
- 약 봉투 사진 업로드 → **CLOVA OCR**로 약품명·용법 추출
- 추출 결과를 **식약처 의약품 API**의 품목코드 기준으로 매칭·검증
- 인식이 애매하면 제품명으로 후보 목록 검색해 사용자가 직접 선택
- 확정된 약을 **복약카드**로 등록 (조제일자·처방일수·1회 복용량·복용 횟수)

### 3. ✈️ 여행 등록 & 반입 판정
- 여행(출발지·도착지·기간) 등록 및 가져갈 약 선택
- 성분별 **신호등 판정** — 반입 가능 / 준비 필요 / 반입 불가
- 수량 한도가 있는 성분은 **소지량을 계산해 한도와 비교**
- 판정 근거(원천 문서·기준일) 및 국가별 반입 규정 조회

### 4. ✅ 준비 서류 체크리스트
- 판정 결과에 따라 필요한 서류·행동 항목을 자동 생성
- 항목별 완료 체크, 정부 공식 신청 페이지 링크 연결
- 업로드가 필요한 항목은 PDF 첨부 (최대 10MB, PDF 시그니처까지 검증)

### 5. 🗂️ 서류함
- 업로드한 서류를 **AWS S3**에 저장 (DB에는 객체 키만 보관)
- 조회 시마다 **만료 시간이 있는 presigned URL**을 발급해 미리보기·다운로드 제공
- 약품별 서류 목록 조회, 서류 삭제

### 6. 🆘 해외 긴급 대응
- **외교부 재외공관 API** 연동 — 현지 대사관·영사관 연락처 조회
- 복용 중인 약을 **현지어 설명문**으로 변환 (OpenAI) — 현지 병원·약국·세관에서 제시

---

## 🌏 규제 데이터 파이프라인

이 서비스의 핵심은 **정부 원천 문서에서 뽑아낸 규제 데이터**입니다.
나라마다 문서 형식이 달라, 오프라인에서 **정규화된 TSV**로 추출해 두고
애플리케이션이 시작할 때 UPSERT 방식으로 적재합니다.

```
📄 정부 원천 문서(PDF)
      │  오프라인 추출 (pdfplumber)
      ▼
📋 정규화 TSV  (성분명 · 분류 · 금지 여부 · 수량 한도 · 기준일)
      │  앱 기동 시 자동 적재 (UPSERT — id 유지, 목록 이탈분은 비활성)
      ▼
🗄️ ingredient_regulation / requirement_template
      │
      ▼
⚖️ 판정 엔진  →  신호등 + 준비 서류 체크리스트
```

| 국가 | 원천 | 분류 체계 |
|---|---|---|
| 🇯🇵 일본 | 후생노동성 마약단속부(NCD) 규제약물 목록 | N(마약) · P(향정신성) · SRM(각성제 원료) |

새 국가 추가는 `CountryRegulationSource` · `CountryRequirementSource` 구현체 하나와
TSV 한 벌을 더하면 끝나도록 설계돼 있습니다.

---

## 🚀 기술 스택

- **Language / Runtime**: Java 21 (Temurin)

  <img src="https://img.shields.io/badge/Java%2021-007396?style=flat-square&logo=openjdk&logoColor=white" />

- **Framework**: Spring Boot 4.1.0, Spring Web MVC, Spring Data JPA, Bean Validation

  <img src="https://img.shields.io/badge/Spring%20Boot%204.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Web%20MVC-6DB33F?style=flat-square&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-59666C?style=flat-square&logo=hibernate&logoColor=white" />

- **Database**: MySQL (Hibernate / JPA)

  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white" />

- **Auth**: Kakao OAuth2 Login, Spring Security, JWT (jjwt 0.12.3)

  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white" />
  <img src="https://img.shields.io/badge/Kakao%20Login-FFCD00?style=flat-square&logo=kakao&logoColor=black" />
  <img src="https://img.shields.io/badge/JWT%20(jjwt%200.12.3)-000000?style=flat-square&logo=jsonwebtokens&logoColor=white" />

- **Storage**: AWS S3 (AWS SDK for Java v2, Presigned URL)

  <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=flat-square&logo=amazons3&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS%20SDK%20v2-FF9900?style=flat-square&logo=amazonaws&logoColor=white" />

- **External API**: 식약처 의약품정보, 외교부 재외공관, CLOVA OCR, OpenAI

  <img src="https://img.shields.io/badge/식약처%20API-0B4DA2?style=flat-square&logo=data.gov&logoColor=white" />
  <img src="https://img.shields.io/badge/외교부%20API-003478?style=flat-square&logo=data.gov&logoColor=white" />
  <img src="https://img.shields.io/badge/CLOVA%20OCR-03C75A?style=flat-square&logo=naver&logoColor=white" />
  <img src="https://img.shields.io/badge/OpenAI-412991?style=flat-square&logo=openai&logoColor=white" />

- **Docs**: springdoc-openapi 3.0.3 (Swagger UI)

  <img src="https://img.shields.io/badge/Swagger%20UI-85EA2D?style=flat-square&logo=swagger&logoColor=black" />

- **Build / Deploy**: Gradle (Wrapper), Docker, GitHub Actions

  <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white" />

- **Etc.**: Lombok, JPA Auditing

  <img src="https://img.shields.io/badge/Lombok-BC2323?style=flat-square&logo=lombok&logoColor=white" />
  <img src="https://img.shields.io/badge/JPA%20Auditing-0FAAFF?style=flat-square&logo=clockify&logoColor=white" />

---

## 🏗️ 아키텍처 개요

```
📱 Mobile / Web
    │  카카오 로그인 · JWT
    ▼
💊 Medipass Backend (Spring Boot)
    │
    ├─▶ 🗄️ MySQL           도메인 데이터 · 규제 마스터
    ├─▶ 🪣 AWS S3          서류 원본 (Presigned URL 발급)
    │
    ├─⇄ 📷 CLOVA OCR       약 봉투 인식
    ├─⇄ 🏥 식약처 API       의약품 품목 · 성분 조회
    ├─⇄ 🏛️ 외교부 API       재외공관 연락처
    └─⇄ 🤖 OpenAI          현지어 설명문 생성
```

- 외부 API는 모두 `global/*/client` 아래 전용 클라이언트로 감싸, 도메인 코드가 외부 스펙에 직접 묶이지 않게 했습니다.
- 규제 판정은 외부 호출 없이 **우리 DB만으로 완결**되어, 외부 API 장애와 무관하게 동작합니다.
- 서류는 DB에 URL을 저장하지 않고 **객체 키만 보관**한 뒤 조회 시마다 만료형 URL을 발급합니다.

---

## 📁 패키지 구조

```
src/main/java/com/medipass/server
├─ domain
│  ├─ user         # 사용자, 카카오 OAuth2 로그인
│  ├─ country      # 국가 마스터
│  ├─ medication   # 약 봉투 스캔 · 식약처 매칭 · 복약카드
│  ├─ regulation   # 규제 성분 · 서류 템플릿 · 판정 엔진
│  │  └─ source      # 국가별 규제/서류 Source
│  ├─ trip         # 여행 · 여행별 약 · 체크리스트
│  ├─ document     # 서류 업로드 · 서류함
│  ├─ emergency    # 재외공관 데이터
│  └─ sos          # 긴급 연락처 · 현지어 설명문
└─ global
   ├─ config       # Security, Web, CORS 설정
   ├─ jwt          # JWT 필터 · Provider · UserDetails
   ├─ oauth        # 카카오 OAuth2 핸들러
   ├─ s3           # S3 업로드 · Presigned URL
   ├─ ocr          # CLOVA OCR 클라이언트
   ├─ mfds         # 식약처 API 클라이언트
   ├─ mofa         # 외교부 API 클라이언트
   ├─ openai       # OpenAI 클라이언트 · 프롬프트
   ├─ response     # 공통 응답 (ApiResponse / Success / Error)
   ├─ exception    # 전역 예외 처리
   ├─ filter       # 요청 ID 필터
   ├─ entity       # BaseEntity (JPA Auditing)
   └─ init         # 초기 데이터 로더 (국가 · 규제 · 서류 템플릿)
```

---

## 🔌 주요 API

Swagger UI와 OAuth2 로그인 경로를 제외한 **모든 엔드포인트는 인증이 필요합니다.**

### 사용자
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/users/me` | 내 계정 정보 조회 |

### 의약품
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/medications/scans` | 약 봉투 이미지 인식 (OCR) |
| GET | `/api/medications/candidates` | 제품명으로 식약처 의약품 후보 검색 |
| POST | `/api/medications` | OCR 확인 결과로 의약품 등록 |
| GET | `/api/medications` | 복약카드 목록 조회 |
| GET | `/api/medications/cards` | 홈 화면 복약카드 조회 |
| GET | `/api/medications/{medicationId}/card` | 복약카드 단건 상세 조회 |
| GET | `/api/medications/{medicationId}/documents` | 약품별 서류 목록 조회 |

### 여행 · 체크리스트
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/trips/analyze` | 여행 정보 분석 |
| POST | `/api/trips` | 여행 등록 |
| GET | `/api/trips/checklog` | 여행 목록 (체크로그) |
| GET | `/api/trips/medications` | 여행에 담을 수 있는 약 목록 |
| GET | `/api/trips/{tripId}` | 여행 상세 조회 |
| PATCH | `/api/trips/{tripId}/title` | 여행 제목 수정 |
| DELETE | `/api/trips/{tripId}` | 여행 삭제 |
| GET | `/api/trips/{tripId}/medications/{tripMedicationId}/destination` | 도착 국가 반입 규정 조회 |
| GET | `/api/trips/{tripId}/medications/{tripMedicationId}/basis` | 판정 근거 조회 |
| GET | `/api/trips/{tripId}/medications/{tripMedicationId}/checklist` | 준비 체크리스트 조회 |
| PATCH | `/api/trips/{tripId}/medications/{tripMedicationId}/checklist/{checklistItemId}` | 체크리스트 완료 상태 변경 |
| POST | `/api/trips/{tripId}/medications/{tripMedicationId}/checklist/{checklistItemId}/document` | 체크리스트 서류 업로드 |

### 서류함
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/documents` | 서류함 메인 조회 |
| GET | `/api/documents/{documentId}` | 서류 보기 (미리보기 URL 발급) |
| POST | `/api/documents/{documentId}/download` | 서류 다운로드 URL 발급 |
| DELETE | `/api/documents/{documentId}` | 서류 삭제 |

### 규제 판정 · SOS
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/regulations/judge` | 성분 규제 판정 |
| POST | `/api/sos/contacts` | 현지 재외공관 연락처 조회 |
| POST | `/api/sos/scripts` | 현지어 설명문 생성 |

---

## 👥 팀 구성 (Backend)

<table>
  <tr>
    <td align="center" width="180">
      <a href="https://github.com/ThreeeJ">
        <img src="https://github.com/ThreeeJ.png" width="120" height="120" style="border-radius:50%" /><br/>
        <b>정종진</b>
      </a>
    </td>
    <td align="center" width="180">
      <a href="https://github.com/yj-044">
        <img src="https://github.com/yj-044.png" width="120" height="120" style="border-radius:50%" /><br/>
        <b>조윤지</b>
      </a>
    </td>
  </tr>
  <tr>
    <td align="center">Backend</td>
    <td align="center">Backend</td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/ThreeeJ">@ThreeeJ</a></td>
    <td align="center"><a href="https://github.com/yj-044">@yj-044</a></td>
  </tr>
</table>