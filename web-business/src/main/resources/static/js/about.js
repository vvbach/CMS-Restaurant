async function loadAboutPage() {
    await callApi("/v1/api/about-page", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        if (!data) return;

        // Title
        const breadcumbText = document.querySelector(".breadcumb-area .breadcumb-text");
        if (breadcumbText) {
            breadcumbText.innerHTML = data.data.title;
        }

        // Text
        const aboutContainer = document.querySelector(".about-area .container");
        if (aboutContainer) {
            aboutContainer.innerHTML = data.data.text;
        }

        // Background image
        const banner = document.querySelector("#contact-area-banner");
        if (banner && data.data.imageUrl) {
            banner.src = data.data.imageUrl;
        }
    });
}

document.addEventListener("DOMContentLoaded", loadAboutPage);
