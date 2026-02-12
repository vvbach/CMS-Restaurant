let maxPage = 10;
let currentPage = 1;
const pagination = document.getElementById('pagination');
let objFilter = {};

// SỬ dụng cho chức năng tìm kiếm
async function handleFilter(e) {

    let pageSize = document.getElementById('f-page-size').value || 10;
    let url = "/v1/api/image-web/filter?pageSize=" + pageSize +"&page=" +currentPage;
    await callApi(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(objFilter)
    }, (data) => {
        const tbody = document.getElementById('gallery');
        tbody.innerHTML = data.data.data.map((item, index) => `
            <div class="col-6 col-sm-4 col-md-3 col-lg-2">
                <a href="#"
                   class="img-link text-decoration-none"
                   data-index="0"
                   data-full="${item.pathImage}"
                   data-title="Alpine Lake">
                    <div class="ratio ratio-1x1">
                        <img
                                src="${item.pathImage}"
                                class="gallery-img rounded object-fit-cover shadow-sm"
                                alt="Alpine lake with mountains"
                                loading="lazy" width="600" height="600">
                    </div>
                    <figcaption class="figure-caption text-center mt-1">${item.description}</figcaption>
                </a>
            </div>
        `).join('');

        // Tính số page tối đa
        maxPage = Math.ceil(data.data.total / (document.getElementById('f-page-size').value || 10));
        if (currentPage === 1) {
            createListPage();
        }

        // hiển thị số records
        document.getElementById('count-badge').textContent = data.data.data.length + '/' + data.data.total;

    });
    this.disabled = false;
}
