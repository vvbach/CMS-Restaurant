async function loadMegaMenu() {
    try {
        // 1. Fetch categories
        const categoriesRes = await callApi("/v1/api/category-page", {
            method: "GET",
            headers: {"Content-Type": "application/json"}
        });

        if (!categoriesRes || !categoriesRes.data) return;

        const data = categoriesRes?.data || [];

        const categoryBlocks = data.map(el => {
            return `
                <ul class="single-mega cn-col-4">
                    <li class="title">${el.name}</li>
                    <li><a href="/category-page/${el.id}">Giới thiệu</a></li>
                    <li><a href="/product?category=${el.categoryId}">Sản phẩm</a></li>
                </ul>
            `
        })

        await callApi("/v1/api/featured-category", {
            method: 'GET',
            headers: {"Content-Type": "application/json"},
        }, (res) => {
            if (!res) return;

            const data = res.data

            const imageLinks = data.map(el => `
                <a href="/product?category=${el.categoryId}"><img src="${el.imageUrl}" alt="${el.name}"></a>
            `).join('')

            categoryBlocks.push(`
            <div class="single-mega cn-col-4">
                <div class="receipe-slider owl-carousel">
                    ${imageLinks}
                </div>
            </div>
            `);
        });

        // 5. Render into menu
        const megaMenu = document.getElementById("dynamicMegaMenu");
        megaMenu.innerHTML = categoryBlocks.join("");

    } catch (err) {
        console.error("Failed to load mega menu:", err);
    }
}

document.addEventListener("DOMContentLoaded", loadMegaMenu);