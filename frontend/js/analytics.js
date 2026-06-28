const API_BASE_URL = "http://localhost:8181/api/v1/urls";

const fetchButton = document.getElementById("fetchAnalyticsBtn");
const openShortUrlBtn = document.getElementById("openShortUrlBtn");

const loading = document.getElementById("loading");
const analyticsCard = document.getElementById("analyticsCard");
const errorCard = document.getElementById("errorCard");

const shortCodeInput = document.getElementById("shortCode");

document.addEventListener("DOMContentLoaded", () => {
  shortCodeInput.focus();

  animateCards();

  addRippleEffect();
});

shortCodeInput.addEventListener("keypress", (e) => {
  if (e.key === "Enter") {
    fetchAnalytics();
  }
});

fetchButton.addEventListener("click", fetchAnalytics);

openShortUrlBtn.addEventListener("click", () => {
  const shortUrl = document.getElementById("shortUrl").href;

  if (shortUrl) {
    window.open(shortUrl, "_blank");
  }
});

async function fetchAnalytics() {
  const shortCode = shortCodeInput.value.trim();

  analyticsCard.classList.add("d-none");

  errorCard.classList.add("d-none");

  openShortUrlBtn.classList.add("d-none");

  if (shortCode === "") {
    showError("Please enter a Short Code.");

    shortCodeInput.focus();

    return;
  }

  fetchButton.disabled = true;

  fetchButton.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Fetching...`;

  loading.classList.remove("d-none");

  try {
    const response = await axios.get(API_BASE_URL + "/analytics/" + shortCode);

    const data = response.data;

    document.getElementById("originalUrl").innerText = data.originalUrl;

    document.getElementById("clickCount").innerText = data.clickCount;

    document.getElementById("createdAt").innerText = formatDate(data.createdAt);

    document.getElementById("lastAccessedAt").innerText = data.lastAccessedAt
      ? formatDate(data.lastAccessedAt)
      : "Never Accessed";

    const shortUrl = document.getElementById("shortUrl");

    shortUrl.href = data.shortUrl;

    shortUrl.innerText = data.shortUrl;

    analyticsCard.classList.remove("d-none");

    analyticsCard.style.animation = "fadeUp .45s ease";

    openShortUrlBtn.classList.remove("d-none");

    analyticsCard.scrollIntoView({
      behavior: "smooth",
    });
  } catch (error) {
    if (error.response) {
      if (typeof error.response.data === "string") {
        showError(error.response.data);
      } else if (error.response.data.message) {
        showError(error.response.data.message);
      } else {
        showError("Unable to fetch analytics.");
      }
    } else {
      showError("Unable to connect to backend.");
    }
  } finally {
    fetchButton.disabled = false;

    fetchButton.innerHTML = `<i class="bi bi-search"></i> Fetch Analytics`;

    loading.classList.add("d-none");
  }
}
function showError(message) {
  errorCard.innerHTML = `<i class="bi bi-exclamation-triangle-fill"></i> ${message}`;

  errorCard.classList.remove("d-none");

  errorCard.style.animation = "fadeUp .35s ease";

  errorCard.scrollIntoView({
    behavior: "smooth",
  });
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleString();
}

window.onload = () => {
  const params = new URLSearchParams(window.location.search);

  const code = params.get("code");

  if (code) {
    shortCodeInput.value = code;

    fetchAnalytics();
  }
};

function animateCards() {
  const cards = document.querySelectorAll(".card");

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

    card.style.transform = "translateY(25px)";

    card.style.transition = ".6s ease";

    observer.observe(card);
  });
}

function copyShortUrl() {
  const shortUrl = document.getElementById("shortUrl").innerText;

  if (!shortUrl) {
    showError("Fetch analytics first.");

    return;
  }

  navigator.clipboard.writeText(shortUrl);

  animateButton(copyShortUrlBtn, "Copied!");
}

function animateButton(button, text) {
  const original = button.innerHTML;

  button.innerHTML = `<i class="bi bi-check-circle-fill"></i> ${text}`;

  button.classList.remove("btn-primary");

  button.classList.add("btn-success");

  setTimeout(() => {
    button.innerHTML = original;

    button.classList.remove("btn-success");

    button.classList.add("btn-primary");
  }, 1800);
}

function addRippleEffect() {
  document.querySelectorAll(".btn").forEach((button) => {
    button.addEventListener("click", function (e) {
      const ripple = document.createElement("span");

      ripple.classList.add("ripple");

      this.appendChild(ripple);

      const rect = this.getBoundingClientRect();

      ripple.style.left = `${e.clientX - rect.left}px`;

      ripple.style.top = `${e.clientY - rect.top}px`;

      setTimeout(() => {
        ripple.remove();
      }, 600);
    });
  });
}

const copyShortUrlBtn = document.getElementById("copyShortUrlBtn");

copyShortUrlBtn.addEventListener("click", copyShortUrl);

document.getElementById("shortUrl").addEventListener("click", function () {
  this.classList.add("text-success");

  setTimeout(() => {
    this.classList.remove("text-success");
  }, 600);
});

document.querySelectorAll(".info-box").forEach((box) => {
  box.addEventListener("mouseenter", () => {
    box.style.transform = "translateY(-2px)";
  });

  box.addEventListener("mouseleave", () => {
    box.style.transform = "translateY(0px)";
  });
});

window.addEventListener("pageshow", () => {
  fetchButton.disabled = false;

  fetchButton.innerHTML = `<i class="bi bi-search"></i> Fetch Analytics`;
});
