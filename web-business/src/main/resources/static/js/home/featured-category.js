async function loadFeaturedCategory() {
    await callApi("/v1/api/featured-category", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        if (!data) return;
        const topCategoryArea = document.querySelector(".top-category-area .container .row");

        if (!topCategoryArea) return;

        topCategoryArea.innerHTML = data.data.map(category => `
            <div class="col-12 col-lg-6">
                    <div class="single-top-catagory">
                        <img src="${category.imageUrl}" alt="${category.name}">
                        <!-- Content -->
                        <div class="top-cta-content">
                            <h3>${category.name}</h3>
                            <h6>${category.description}</h6>
                            <a href="/product?category=${category.categoryId}" class="btn delicious-btn">Xem các món ăn</a>
                        </div>
                    </div>
                </div>
        `).join('');
    });
}

document.addEventListener("DOMContentLoaded", loadFeaturedCategory);
