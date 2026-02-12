let maxPage = 10;
let currentPage = 1;
const pagination = document.getElementById('pagination');
let objFilter = {};

// SỬ dụng cho chức năng tìm kiếm
async function handleFilter(e) {
    objFilter["isDelete"] = document.getElementById('f-is-delete').value || null;
    objFilter["description"] = document.getElementById('f-description').value || null;
    objFilter["formDate"] = document.getElementById('f-from-date').value || null;
    objFilter["toDate"] = document.getElementById('f-to-date').value || null;
    objFilter["createdByName"] = document.getElementById('f-create_by').value || null;
    objFilter["status"] = document.getElementById('f-s-status').value || null;
    let pageSize = document.getElementById('f-page-size').value || 10;
    let url = "/v1/api/admin-unit/filter?pageSize=" + pageSize + "&page=" + currentPage;
    await callApi(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(objFilter)
    }, (data) => {
        const tbody = document.getElementById('myTableBody');
        tbody.innerHTML = data.data.data.map((item, index) =>
            `
                <tr onclick="viewDetail('${item.id}')">
                    <th scope="row">${index + 1}</th>
                    <td><div class="text-truncate" style="min-width: 100px; max-width: 200px;" data-bs-toggle="tooltip">${item.name}</div></td>
                    <td>${item.isDelete === 'NO' ? 'Active' : 'Deleted'}</td>
                    <td>${mapStatus(item.status)}</td>
                    <td>${item.createdByName}</td>
                    <td>${formatDate(item.createdAt)}</td>
                </tr>
            `).join('');

        // Tính số page tối đa
        maxPage = Math.ceil(data.data.total / (document.getElementById('f-page-size').value || 10));
        if (currentPage === 1) {
            createListPage()
        }

        // hiển thị số records
        document.getElementById('count-badge').textContent = data.data.data.length + '/' + data.data.total;

    });
    this.disabled = false;
}

// Khi ấn button tìm kiếm
document.getElementById('btnFilter').addEventListener('click',(e) => {
    e.preventDefault();
    this.disabled = true;// Vì sao: tránh form submit mặc định
    currentPage = 1;
    handleFilter();
    this.disabled = false
});
