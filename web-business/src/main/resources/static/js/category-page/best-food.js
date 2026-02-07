async function loadCategoryBestFood() {
    await callApi(`/v1/api/category-best-food/${location.pathname.split("/")[2]}`, {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (data) => {
        if (!data) return;
        const bestFoodRow = document.querySelector(".best-food-area .container .row:last-child");

        if (!bestFoodRow) return;

        bestFoodRow.innerHTML = data.data.map(food => `
            <div class="col-12 col-sm-6 col-lg-4">
                <div class="single-best-receipe-area mb-30">
                    <img src="${food.imageUrl}" alt="${food.foodName}">
                    <div class="receipe-content">
                        <a href="/product/detail/${food.foodId}">
                            <h5>${food.foodName}</h5>
                        </a>
                        <p>${food.description || ""}</p>
                    </div>
                </div>
            </div>
        `).join('');
    });
}

document.addEventListener("DOMContentLoaded", loadCategoryBestFood);
