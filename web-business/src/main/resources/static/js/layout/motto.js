async function loadMotto() {
    await callApi("/v1/api/motto", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        if (!data) return;
        const motto = document.querySelector(".ticker ul");
        if (motto) {
            motto.innerHTML = `
                <li><a href="#">${data.data.title}</a></li>
            `;
            $.simpleTicker($("#breakingNewsTicker"), {
                speed: 1250,
                delay: 3500,
                easing: 'swing',
                effectType: 'roll'
            });
        }
    });
}

document.addEventListener("DOMContentLoaded", loadMotto);
