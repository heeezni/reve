// Banner Slider Functionality
class BannerSlider {
  constructor() {
    this.currentSlide = 0;
    this.slides = document.querySelectorAll('.banner-slide');
    this.dots = document.querySelectorAll('.dot');
    this.totalSlides = this.slides.length;
    this.autoPlayInterval = null;

    this.init();
  }

  init() {
    this.bindEvents();
    this.startAutoPlay();
  }

  bindEvents() {
    // Dot click events
    this.dots.forEach((dot, index) => {
      dot.addEventListener('click', () => {
        this.goToSlide(index);
      });
    });

    // Banner arrow events
    const leftArrow = document.querySelector('.banner-arrow.left');
    const rightArrow = document.querySelector('.banner-arrow.right');
    if (leftArrow) {
      leftArrow.addEventListener('click', () => {
        this.prevSlide();
        this.restartAutoPlay();
      });
    }
    if (rightArrow) {
      rightArrow.addEventListener('click', () => {
        this.nextSlide();
        this.restartAutoPlay();
      });
    }

    // Pause autoplay on hover
    const banner = document.querySelector('.main-banner');
    if (banner) {
      banner.addEventListener('mouseenter', () => {
        this.stopAutoPlay();
      });

      banner.addEventListener('mouseleave', () => {
        this.startAutoPlay();
      });
    }
  }

  goToSlide(index) {
    // Remove active class from current slide and dot
    this.slides[this.currentSlide].classList.remove('active');
    this.dots[this.currentSlide].classList.remove('active');
    this.dots[this.currentSlide].classList.remove('bg-white');
    this.dots[this.currentSlide].classList.add('bg-white/50');

    // Update current slide
    this.currentSlide = index;

    // Add active class to new slide and dot
    this.slides[this.currentSlide].classList.add('active');
    this.dots[this.currentSlide].classList.add('active');
    this.dots[this.currentSlide].classList.remove('bg-white/50');
    this.dots[this.currentSlide].classList.add('bg-white');
  }

  nextSlide() {
    const nextIndex = (this.currentSlide + 1) % this.totalSlides;
    this.goToSlide(nextIndex);
  }

  prevSlide() {
    const prevIndex =
      (this.currentSlide - 1 + this.totalSlides) % this.totalSlides;
    this.goToSlide(prevIndex);
  }

  startAutoPlay() {
    this.autoPlayInterval = setInterval(() => {
      this.nextSlide();
    }, 5000); // Change slide every 5 seconds
  }

  stopAutoPlay() {
    if (this.autoPlayInterval) {
      clearInterval(this.autoPlayInterval);
      this.autoPlayInterval = null;
    }
  }

  restartAutoPlay() {
    this.stopAutoPlay();
    this.startAutoPlay();
  }
}

// Modal Functionality
class Modal {
  constructor() {
    this.modal = document.getElementById('loginModal');
    this.loginBtn = document.querySelector('.login-btn');
    this.closeBtn = document.querySelector('.close');

    this.init();
  }

  init() {
    this.bindEvents();
  }

  bindEvents() {
    // Open modal
    if (this.loginBtn) {
      this.loginBtn.addEventListener('click', (e) => {
        e.preventDefault();
        this.openModal();
      });
    }

    // Close modal
    if (this.closeBtn) {
      this.closeBtn.addEventListener('click', () => {
        this.closeModal();
      });
    }

    // Close modal when clicking outside
    if (this.modal) {
      window.addEventListener('click', (e) => {
        if (e.target === this.modal) {
          this.closeModal();
        }
      });
    }

    // Close modal with Escape key
    document.addEventListener('keydown', (e) => {
      if (
        e.key === 'Escape' &&
        this.modal &&
        this.modal.style.display === 'block'
      ) {
        this.closeModal();
      }
    });
  }

  openModal() {
    if (this.modal) {
      this.modal.style.display = 'block';
      document.body.style.overflow = 'hidden'; // Prevent background scrolling
    }
  }

  closeModal() {
    if (this.modal) {
      this.modal.style.display = 'none';
      document.body.style.overflow = 'auto'; // Restore scrolling
    }
  }
}

// Form Validation
class FormValidation {
  constructor() {
    this.init();
  }
  init() {
    const loginForm = document.querySelector('.form-login form');

    if (loginForm) {
      loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const isValid = this.validateForm(loginForm);
        if (isValid) {
          loginForm.submit();
        }
      });
    }
  }

  validateForm(form) {
    const inputs = form.querySelectorAll('input[required]');
    let isValid = true;

    inputs.forEach((input) => {
      if (!input.value.trim()) {
        this.showError(input, '이 필드는 필수입니다.');
        isValid = false;
      } else {
        this.removeError(input);
      }
    });

    return isValid;
  }

  showError(input, message) {
    this.removeError(input);

    const errorDiv = document.createElement('div');
    errorDiv.className = 'text-red-500 text-xs mt-1';
    errorDiv.textContent = message;

    input.parentNode.appendChild(errorDiv);
    input.classList.add('border-red-500');
    input.classList.remove('border-gray-300');
  }

  removeError(input) {
    const existingError = input.parentNode.querySelector('.text-red-500');
    if (existingError) {
      existingError.remove();
    }
    input.classList.remove('border-red-500');
    input.classList.add('border-gray-300');
  }
}

// Initialize all functionality when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new BannerSlider();
  new Modal();
  new FormValidation();

  // Add loading animation
  window.addEventListener('load', () => {
    document.body.classList.add('loaded');
  });
});

// Utility function for smooth animations with Tailwind
function animateOnScroll() {
  const elements = document.querySelectorAll('.category-item, .review-item');

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.remove('opacity-0', 'translate-y-8');
          entry.target.classList.add('opacity-100', 'translate-y-0');
        }
      });
    },
    {
      threshold: 0.1,
    }
  );

  elements.forEach((element) => {
    element.classList.add(
      'opacity-0',
      'translate-y-8',
      'transition-all',
      'duration-600',
      'ease-out'
    );
    observer.observe(element);
  });
}

// Initialize scroll animations
document.addEventListener('DOMContentLoaded', animateOnScroll);
