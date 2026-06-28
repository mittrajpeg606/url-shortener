const API_BASE_URL = "http://localhost:8181/api/v1/urls";

const shortenButton = document.getElementById("shortenBtn");
const copyButton = document.getElementById("copyBtn");
const copyShortCodeButton = document.getElementById("copyShortCodeBtn");
const analyticsButton = document.getElementById("analyticsBtn");
const openUrlButton = document.getElementById("openUrlBtn");

const loading = document.getElementById("loading");
const resultCard = document.getElementById("resultCard");
const errorCard = document.getElementById("errorCard");

const originalUrlInput = document.getElementById("originalUrl");
const shortUrlInput = document.getElementById("shortUrl");
const shortCodeInput = document.getElementById("shortCode");

document.addEventListener("DOMContentLoaded", () => {
  originalUrlInput.focus();

  animateCards();

  addRippleEffect();
});

originalUrlInput.addEventListener("keypress", (e) => {
  if (e.key === "Enter") {
    shortenUrl();
  }
});

shortenButton.addEventListener("click", shortenUrl);

copyButton.addEventListener("click", copyUrl);

copyShortCodeButton.addEventListener("click", copyShortCode);

analyticsButton.addEventListener("click", openAnalytics);

if (openUrlButton) {
  openUrlButton.addEventListener("click", () => {
    if (shortUrlInput.value) {
      window.open(shortUrlInput.value, "_blank");
    }
  });
}

async function shortenUrl() {
  const originalUrl = originalUrlInput.value.trim();

  const generatorType = document.getElementById("generatorType").value;

  hideMessages();

  if (originalUrl === "") {
    showError("Please enter a URL.");

    originalUrlInput.focus();

    return;
  }

  shortenButton.disabled = true;

  shortenButton.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Generating...`;

  loading.classList.remove("d-none");

  try {
    const response = await axios.post(
      API_BASE_URL + "/shorten",

      {
        originalUrl,

        generatorType,
      },
    );

    shortCodeInput.value = response.data.shortCode;

    shortUrlInput.value = response.data.shortUrl;

    resultCard.classList.remove("d-none");

    resultCard.style.animation = "fadeUp .45s ease";

    resultCard.scrollIntoView({
      behavior: "smooth",
    });
  } catch (error) {
    if (error.response) {
      if (typeof error.response.data === "string") {
        showError(error.response.data);
      } else if (error.response.data.message) {
        showError(error.response.data.message);
      } else {
        showError("Something went wrong.");
      }
    } else {
      showError("Unable to connect to backend.");
    }
  } finally {
    shortenButton.disabled = false;

    shortenButton.innerHTML = `<i class="bi bi-scissors"></i> Shorten URL`;

    loading.classList.add("d-none");
  }
}
function copyUrl() {
  if (!shortUrlInput.value) {
    showError("Generate a URL first.");

    return;
  }

  navigator.clipboard.writeText(shortUrlInput.value);

  animateButton(copyButton, "Copied!");
}

function copyShortCode() {
  if (!shortCodeInput.value) {
    showError("Generate a URL first.");

    return;
  }

  navigator.clipboard.writeText(shortCodeInput.value);

  animateButton(copyShortCodeButton, "Copied!");
}

function animateButton(button, text) {
  const originalText = button.innerHTML;

  button.innerHTML = `<i class="bi bi-check-circle-fill"></i> ${text}`;

  button.classList.add("btn-success");

  button.classList.remove("btn-primary");
  button.classList.remove("btn-dark");

  setTimeout(() => {
    button.innerHTML = originalText;

    if (button.id === "copyShortCodeBtn") {
      button.classList.remove("btn-success");
      button.classList.add("btn-primary");
    }

    if (button.id === "copyBtn") {
      button.classList.remove("btn-success");
      button.classList.add("btn-success");
    }
  }, 1800);
}

function openAnalytics() {
  if (!shortCodeInput.value) {
    showError("Generate a Short URL first.");

    return;
  }

  window.location.href =
    "analytics.html?code=" + encodeURIComponent(shortCodeInput.value);
}

function hideMessages() {
  errorCard.classList.add("d-none");

  resultCard.classList.add("d-none");
}

function showError(message) {
  errorCard.innerHTML = `<i class="bi bi-exclamation-triangle-fill"></i> ${message}`;

  errorCard.classList.remove("d-none");

  errorCard.style.animation = "fadeUp .35s ease";

  errorCard.scrollIntoView({
    behavior: "smooth",
  });
}

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
