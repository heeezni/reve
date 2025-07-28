# 향수 쇼핑몰 클론 프로젝트

## 프로젝트 구조

```
src/                       # 소스 코드 및 리소스 파일
    ├───main/                  # 메인 애플리케이션 소스
    │   ├───java/              # Java 소스 코드
    │   │   └───com/
    │   │       └───example/
    │   │           └───reve/
    │   │               ├───ReveApplication.java # Spring Boot 애플리케이션 메인 클래스
    │   │               └───controller/          # 웹 요청을 처리하는 컨트롤러 패키지
    │   │                   ├───InfoController.java # 정보 페이지 (소개, 약관, 정책) 관련 컨트롤러
    │   │                   ├───admin/             # 관리자 기능 관련 컨트롤러
    │   │                   │   ├───AdminBoardController.java # 관리자 게시판 (공지, Q&A) 관련 컨트롤러
    │   │                   │   ├───AdminController.java # 관리자 대시보드, 상품, 주문, 회원 관리 컨트롤러
    │   │                   │   └───AdminCouponController.java # 관리자 쿠폰 관리 관련 컨트롤러
    │   │                   └───user/              # 사용자 기능 관련 컨트롤러
    │   │                       ├───BoardController.java # 사용자 게시판 (공지, Q&A) 관련 컨트롤러
    │   │                       ├───CartController.java # 사용자 장바구니 관련 컨트롤러
    │   │                       ├───MemberController.java # 사용자 회원 (로그인, 회원가입) 관련 컨트롤러
    │   │                       ├───MypageController.java # 사용자 마이페이지 관련 컨트롤러
    │   │                       ├───OrderController.java # 사용자 주문 (결제, 확인) 관련 컨트롤러
    │   │                       └───ShopController.java # 사용자 상품 (목록, 상세) 관련 컨트롤러
    │   └───resources/           # 애플리케이션 리소스 파일
    │       ├───application.properties # Spring Boot 설정 파일 (포트, DB 연결 등)
    │       ├───static/          # 정적 리소스 (CSS, JS, 이미지 등)
    │       │   ├───image/       # 이미지 파일
    │       │   │   └───.gitkeep # 빈 디렉토리 유지를 위한 더미 파일
    │       │   └───js/          # JavaScript 파일
    │       │       └───.gitkeep # 빈 디렉토리 유지를 위한 더미 파일
    │       └───templates/       # Thymeleaf 등 템플릿 엔진이 사용하는 HTML 파일
    │           ├───index.html             # 메인 홈 페이지 (URL: /)
    │           ├───admin/                 # 관리자 페이지
    │           │   ├───dashboard.html     # 관리자 대시보드 (URL: /admin/dashboard)
    │           │   ├───board/             # 관리자 게시판 관리
    │           │   │   ├───notice/        # 관리자 공지사항 관리
    │           │   │   │   ├───form.html  # 공지사항 작성/수정 폼 (URL: /admin/board/notice/form)
    │           │   │   │   └───list.html  # 공지사항 목록 (URL: /admin/board/notice/list)
    │           │   │   └───qna/           # 관리자 Q&A 관리
    │           │   │       ├───list.html  # Q&A 목록 (URL: /admin/board/qna/list)
    │           │   │       └───reply.html # Q&A 답변 작성 (URL: /admin/board/qna/reply)
    │           │   ├───coupon/            # 관리자 쿠폰 관리
    │           │   │   ├───add.html       # 쿠폰 등록 폼 (URL: /admin/coupon/add)
    │           │   │   ├───edit.html      # 쿠폰 수정 폼 (URL: /admin/coupon/edit)
    │           │   │   └───list.html      # 쿠폰 목록 (URL: /admin/coupon/list)
    │           │   ├───fragment/          # 관리자 페이지 공통 UI 조각
    │           │   │   ├───footer.html    # 푸터 (재사용)
    │           │   │   ├───header.html    # 헤더 (재사용)
    │           │   │   └───sidebar.html   # 사이드바 (재사용)
    │           │   ├───member/            # 관리자 회원 관리
    │           │   │   └───list.html      # 회원 목록 (URL: /admin/member/list)
    │           │   ├───order/             # 관리자 주문 관리
    │           │   │   └───list.html      # 주문 목록 (URL: /admin/order/list)
    │           │   └───product/           # 관리자 상품 관리
    │           │       ├───add.html       # 상품 등록 폼 (URL: /admin/product/add)
    │           │       ├───edit.html      # 상품 수정 폼 (URL: /admin/product/edit)
    │           │       └───list.html      # 상품 목록 (URL: /admin/product/list)
    │           ├───common/                # 공통 페이지
    │           │   └───error.html         # 에러 페이지 (직접 URL 매핑 없음, 에러 발생 시 사용)
    │           ├───info/                  # 정보성 페이지
    │           │   ├───about.html         # 회사/서비스 소개 (URL: /info/about)
    │           │   ├───policy.html        # 개인정보처리방침 (URL: /info/policy)
    │           │   └───terms.html         # 이용약관 (URL: /info/terms)
    │           ├───member/                # 회원 관련 페이지
    │           │   ├───login.html         # 로그인 페이지 (URL: /member/login)
    │           │   └───signup.html        # 회원가입 페이지 (URL: /member/signup)
    │           └───user/                  # 사용자 페이지
    │               ├───board/             # 사용자 게시판
    │               │   ├───notice/        # 사용자 공지사항
    │               │   │   ├───detail.html # 공지사항 상세 (URL: /board/notice/detail)
    │               │   │   └───list.html  # 공지사항 목록 (URL: /board/notice/list)
    │               │   └───qna/           # 사용자 Q&A
    │               │       ├───detail.html # Q&A 상세 (URL: /board/qna/detail)
    │               │       ├───form.html  # Q&A 작성 폼 (URL: /board/qna/form)
    │               │       └───list.html  # Q&A 목록 (URL: /board/qna/list)
    │               ├───cart/              # 사용자 장바구니
    │               │   └───cart.html      # 장바구니 페이지 (URL: /cart)
    │               ├───fragment/          # 사용자 페이지 공통 UI 조각
    │               │   ├───footer.html    # 푸터 (재사용)
    │               │   ├───header.html    # 헤더 (재사용)
    │               │   └───sidebar.html   # 사이드바 (재사용)
    │               ├───mypage/            # 사용자 마이페이지
    │               │   ├───account.html   # 계정 정보 (URL: /shop/mypage/account)
    │               │   ├───index.html     # 마이페이지 대시보드 (URL: /shop/mypage)
    │               │   ├───order.html     # 주문 내역 (URL: /shop/mypage/order)
    │               │   └───wishlist.html  # 위시리스트 (URL: /shop/mypage/wishlist)
    │               ├───order/             # 사용자 주문 관련
    │               │   ├───checkout.html  # 결제 페이지 (URL: /shop/order/checkout)
    │               │   └───confirmation.html # 결제 완료 페이지 (URL: /shop/order/confirmation)
    │               └───shop/              # 사용자 쇼핑
    │                   ├───detail.html    # 상품 상세 (URL: /shop/detail)
    │                   └───list.html      # 상품 목록 (URL: /shop/list)
    └───test/                  # 테스트 코드
        └───java/              # Java 테스트 소스 코드
            └───com/
                └───example/
                    └───reve/
                        └───ReveApplicationTests.java # Spring Boot 테스트 클래스
```
