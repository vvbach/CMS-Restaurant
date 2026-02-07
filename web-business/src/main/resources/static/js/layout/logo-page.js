async function loadLogo() {
    await callApi("/v1/api/logo-page", {
        method: 'GET',
        headers: { "Content-Type": "application/json" },
    }, (data) => {
        if (!data) return;

        // update header logo
        const brand = document.querySelector(".nav-brand");
        if (brand) {
            const img = brand.querySelector("img");
            if (img && data.data.url) {
                img.src = data.data.url;
                img.alt = data.data.name;
            }
        }

        // update footer logo
        const footerLogo = document.querySelector(".footer-logo a");
        if (footerLogo) {
            const img = footerLogo.querySelector("img");
            if (img && data.data.url) {
                img.src = data.data.url;
                img.alt = data.data.name;
            }
        }
    });
}

document.addEventListener("DOMContentLoaded", loadLogo);
