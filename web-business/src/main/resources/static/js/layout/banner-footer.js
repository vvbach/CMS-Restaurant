async function loadBannerFooter() {
    await callApi("/v1/api/banner-footer", {
        method: 'GET',
        headers: {"Content-Type": "application/json"},
    }, (res) => {
        if (!res) return;
        const container = document.querySelector('.promoted-foods-container');
        container.innerHTML = res.data.map(food => `
                <div class="single-insta-feeds">
                    <img src="${food.imageUrl}" alt="" style="height: 186px">
                    <a href="/product/detail/${food.foodId}">
                        <div class="insta-icon">
                            <i class="fa fa-instagram" aria-hidden="true"></i>
                        </div>
                    </a>
                </div>
            `).join('');
    });
}

document.addEventListener("DOMContentLoaded", loadBannerFooter);
