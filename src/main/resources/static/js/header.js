// Header Scroll Effect
class HeaderScroll {
  constructor() {
    this.header = document.getElementById('header');
    this.bannerHeight = window.innerHeight; // 배너 높이 (화면 높이)

    this.init();
  }

  init() {
    if (this.header) {
      window.addEventListener('scroll', () => {
        this.handleScroll();
      });
    }
  }

  handleScroll() {
    const scrollTop = document.documentElement.scrollTop;

    if (scrollTop > this.bannerHeight) {
      // 배너를 지나면 - 흰색 배경
      this.header.classList.add('bg-white/95', 'backdrop-blur-md', 'shadow-sm');
      this.header.classList.remove('bg-transparent');
    } else {
      // 배너 영역에서는 - 투명 배경 유지
      this.header.classList.remove(
        'bg-white/95',
        'backdrop-blur-md',
        'shadow-sm'
      );
      this.header.classList.add('bg-transparent');
    }

    // 헤더 숨김/표시 기능 제거 - 항상 보이도록
    this.header.style.transform = 'translateY(0)';
  }
}

// Smooth Scrolling for Navigation Links
class SmoothScroll {
  constructor() {
    this.init();
  }

  init() {
    const navLinks = document.querySelectorAll('nav a[href^="#"]');

    navLinks.forEach((link) => {
      link.addEventListener('click', (e) => {
        e.preventDefault();

        const targetId = link.getAttribute('href');
        const targetElement = document.querySelector(targetId);

        if (targetElement) {
          const headerHeight = document.querySelector('header').offsetHeight;
          const targetPosition = targetElement.offsetTop - headerHeight;

          window.scrollTo({
            top: targetPosition,
            behavior: 'smooth',
          });
        }
      });
    });
  }
}

// Mobile Menu Toggle
class MobileMenu {
  constructor() {
    this.init();
  }

  init() {
    // Add mobile menu button if not exists
    const header = document.querySelector('header .flex');
    if (header && !document.querySelector('.mobile-menu-btn')) {
      const mobileBtn = document.createElement('button');
      mobileBtn.className = 'mobile-menu-btn md:hidden p-2';
      mobileBtn.innerHTML =
        '<svg class="w-6 h-6" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path></svg>';

      header.insertBefore(mobileBtn, header.firstChild);

      // Add mobile menu
      const mobileMenu = document.createElement('div');
      mobileMenu.className =
        'mobile-menu fixed inset-0 bg-white z-40 transform translate-x-full transition-transform duration-300 md:hidden';
      mobileMenu.innerHTML = `
                <div class="flex justify-between items-center p-4 border-b">
                    <span class="text-xl font-bold">Menu</span>
                    <button class="mobile-close p-2">
                        <svg class="w-6 h-6" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                        </svg>
                    </button>
                </div>
                <nav class="p-4 space-y-4">
                    <a href="/info/about" class="block text-lg font-medium">ABOUT</a>
                    <a href="/shop/list" class="block text-lg font-medium">SHOP</a>
                    <a href="/board/notice/list" class="block text-lg font-medium">NOTICE</a>
                    <a href="/board/qna/list" class="block text-lg font-medium">Q&A</a>
                </nav>
            `;

      document.body.appendChild(mobileMenu);

      // Bind events
      mobileBtn.addEventListener('click', () => {
        mobileMenu.classList.remove('translate-x-full');
      });

      mobileMenu
        .querySelector('.mobile-close')
        .addEventListener('click', () => {
          mobileMenu.classList.add('translate-x-full');
        });
    }
  }
}

// Initialize header functionality when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new HeaderScroll();
  new SmoothScroll();
  new MobileMenu();
});
