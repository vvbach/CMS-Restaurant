async function loadAboutCategory() {
    await callApi(`/v1/api/about-category/${location.pathname.split("/")[2]}`, {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (res) => {
        if (!res) return;

        const data = res?.data;

        document.getElementById("about-category-title").innerText = data.title;
        document.getElementById("about-category-subtitle").innerText = data.subtitle;
        document.getElementById("about-category-description").innerText = data.description;
    });
}

document.addEventListener("DOMContentLoaded", loadAboutCategory);
