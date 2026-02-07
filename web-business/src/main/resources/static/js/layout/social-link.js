async function loadSocialLinks() {
    await callApi("/v1/api/social-link", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        const selectors = [".top-social-info", ".footer-social-info"];
        selectors.forEach(sel => {
            const container = document.querySelector(sel);
            if (!container) return;

            container.innerHTML = ""; // clear old content

            if (Array.isArray(data.data)) {
                data.data.forEach(item => {
                    const a = document.createElement("a");
                    a.href = item.url;
                    a.target = "_blank"; // open in new tab

                    const i = document.createElement("img");
                    i.src = `${item.iconUrl}`;
                    i.width = 20;
                    i.height = 20;

                    a.appendChild(i);
                    container.appendChild(a);
                });
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", loadSocialLinks);
