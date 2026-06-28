// ================================
// Landing Page Animations
// ================================

document.addEventListener("DOMContentLoaded", () => {
  animateCards();

  animateTechBadges();

  smoothScroll();
});

// =================================
// Feature Card Animation
// =================================

function animateCards() {
  const cards = document.querySelectorAll(".feature-card");

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.style.opacity = "1";

          entry.target.style.transform = "translateY(0px)";
        }
      });
    },
    {
      threshold: 0.15,
    },
  );

  cards.forEach((card) => {
    card.style.opacity = "0";

    card.style.transform = "translateY(40px)";

    card.style.transition = ".6s ease";

    observer.observe(card);
  });
}

// =================================
// Tech Badge Animation
// =================================

function animateTechBadges() {
  const badges = document.querySelectorAll(".tech");

  badges.forEach((badge, index) => {
    badge.style.opacity = "0";

    badge.style.transform = "scale(.8)";

    badge.style.transition = ".35s";

    setTimeout(() => {
      badge.style.opacity = "1";

      badge.style.transform = "scale(1)";
    }, index * 120);
  });
}

// =================================
// Smooth Anchor Scroll
// =================================

function smoothScroll() {
  document.querySelectorAll("a[href^='#']").forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      e.preventDefault();

      const target = document.querySelector(this.getAttribute("href"));

      if (target) {
        target.scrollIntoView({
          behavior: "smooth",
        });
      }
    });
  });
}

// =================================
// Launch Button Ripple Effect
// =================================

document.querySelectorAll(".btn").forEach((button) => {
  button.addEventListener("click", function (e) {
    const ripple = document.createElement("span");

    ripple.classList.add("ripple");

    this.appendChild(ripple);

    const x = e.clientX - e.target.offsetLeft;

    const y = e.clientY - e.target.offsetTop;

    ripple.style.left = `${x}px`;

    ripple.style.top = `${y}px`;

    setTimeout(() => {
      ripple.remove();
    }, 600);
  });
});
