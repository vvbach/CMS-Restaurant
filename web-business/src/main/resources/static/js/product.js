let currentPage = 1;
let maxPage = 1;
let objFilter = {};

async function handleFilter() {
    // collect filter values (use jQuery for niceSelect compatibility)
    objFilter["categoryName"] = $('#categoryFilter').val() || null;
    objFilter["minPrice"] = document.getElementById("minPrice").value || null;
    objFilter["maxPrice"] = document.getElementById("maxPrice").value || null;
    objFilter["searchQuery"] = document.getElementById("searchBox").value || null;

    console.log("Applying filters:", objFilter);

    let pageSize = 12; // number of products per page
    let url = `/v1/api/food/filter?pageSize=${pageSize}&page=${currentPage}`;

    await callApi(
        url,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(objFilter)
        },
        (data) => {
            const grid = document.getElementById("foodGrid");
            grid.innerHTML = "";

            if (!data || !data.data || data.data.data.length === 0) {
                grid.innerHTML = `<div class="col-12 text-center"><p>No foods found.</p></div>`;
                return;
            }

            // Render products
            data.data.data.forEach(food => {
                const html = `
                    <div class="col-12 col-sm-6 col-lg-4 mb-4" onclick="goToDetail('${food.id}')" style="cursor:pointer">
                        <div class="card h-100 shadow-sm">
                            <img src="${food.imageUrl}" class="card-img-top" alt="${food.name}">
                            <div class="card-body d-flex flex-column">
                                <h5 class="card-title">${food.name}</h5>
                                <p class="card-text">${food.description}</p>
                                <div class="mt-auto">
                                    ${
                                        food.discount !== 0.0
                                            ? `<h4 class="text-primary">
                                                                        ${(food.price - (food.price * food.discount / 100)).toFixed(2)} ₫
                                                                   </h4>
                                                                   <p><del class="text-muted">${food.price.toFixed(2)} ₫</del></p>`
                                            : `<h4 class="text-primary">${food.price.toFixed(2)} ₫</h4>`
                                    }
                                </div>
                            </div>
                        </div>
                    </div>
                `;
                grid.insertAdjacentHTML("beforeend", html);
            });

            // update pagination
            maxPage = Math.ceil(data.data.total / pageSize);
            createPagination();
        }
    );
}

// Build pagination UI
function createPagination() {
    const pagination = document.getElementById("pagination");
    if (!pagination) return;

    pagination.innerHTML = `
        <li class="page-item ${currentPage === 1 ? "disabled" : ""}">
            <a class="page-link" href="#" onclick="goToPage(${currentPage - 1})">Prev</a>
        </li>
    `;

    for (let i = 1; i <= maxPage; i++) {
        pagination.innerHTML += `
            <li class="page-item ${i === currentPage ? "active" : ""}">
                <a class="page-link" href="#" onclick="goToPage(${i})">${i}</a>
            </li>
        `;
    }

    pagination.innerHTML += `
        <li class="page-item ${currentPage === maxPage ? "disabled" : ""}">
            <a class="page-link" href="#" onclick="goToPage(${currentPage + 1})">Next</a>
        </li>
    `;
}

function goToPage(page) {
    if (page < 1 || page > maxPage) return;
    currentPage = page;
    handleFilter();
}

function goToDetail(id) {
    window.location.href = `/product/detail/${id}`;
}

function loadCategories() {
    return new Promise((resolve, reject) => {
        callApi("/v1/api/food-category", { method: "GET" }, async (data) => {
            try {
                const select = document.getElementById("categoryFilter");
                if (!select) return resolve();

                // clear existing (keep first option)
                select.innerHTML = `<option value="">All danh mục</option>`;

                if (!data || !data.data || data.data.length === 0) {
                    // still reinit niceSelect so UI is consistent
                    $('#categoryFilter').niceSelect('destroy');
                    $('#categoryFilter').niceSelect();
                    return resolve();
                }

                data.data.forEach(cat => {
                    const opt = document.createElement("option");
                    opt.value = cat.name;
                    opt.textContent = cat.name;
                    select.appendChild(opt);
                });

                // destroy and re-init niceSelect to sync values
                $('#categoryFilter').niceSelect('destroy');
                $('#categoryFilter').niceSelect();

                resolve();
            } catch (err) {
                reject(err);
            }
        });
    });
}


document.addEventListener("DOMContentLoaded", async () => {
    await loadCategories();

    if (window.location.href.includes("category")) {
        const params = new URL(window.location.href).searchParams;
        const uuid = params.get("category");
        const res = await callApi(`/v1/api/food-category/${uuid}`);
        const categoryName = res?.data?.name;
        if (categoryName) {
            document.getElementById("categoryFilter").value = categoryName;
        }
    }

    // Filter button
    document.getElementById("filterBtn").addEventListener("click", async (e) => {
        e.preventDefault();
        currentPage = 1; // reset to first page on filter
        await handleFilter();
    });

    // Initial load
    await handleFilter();
});
