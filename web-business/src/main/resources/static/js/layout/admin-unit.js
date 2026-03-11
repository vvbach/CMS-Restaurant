async function loadAdminUnit() {
    await callApi("/v1/api/admin-unit", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        if (!data) return;
        const adminUnit = document.querySelector(".footer-admin-unit");
        if (adminUnit) {
            const img = adminUnit.querySelector("img");
            if (img && data.data.logoUrl) {
                img.src = data.data.logoUrl;
                img.alt = data.data.name || "Admin Unit";
                document.querySelector(".admin-unit-name").innerText = data.data.name || "";
            }
        }
    });
}

document.addEventListener("DOMContentLoaded", loadAdminUnit);
