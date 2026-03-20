async function loadAboutPage() {
    await callApi("/v1/api/about-page", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        if (!data) return;

        const breadcumbText = document.querySelector(".breadcumb-area .breadcumb-text h2");
        if (breadcumbText) {
            breadcumbText.innerHTML = data.data.title;
        }

        const aboutContainer = document.querySelector(".about-area .container");
        if (aboutContainer) {
            aboutContainer.innerHTML = data.data.text;
        }

        const banner = document.querySelector("#contact-area-banner");
        if (banner && data.data.imageUrl) {
            banner.src = data.data.imageUrl;
            banner.style.display = "block";
            banner.style.margin = "0 auto";
        }
    });
}

document.addEventListener("DOMContentLoaded", loadAboutPage);
