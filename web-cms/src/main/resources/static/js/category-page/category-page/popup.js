<!-- mở popup để update dữ liệu update-->
document.getElementById('openModalBtn').addEventListener('click', async function () {
    if (!document.getElementById('detail-id').value) return;

    if (this.disabled) return;   // prevent double-click
    this.disabled = true;

    let myModal = new bootstrap.Modal(document.getElementById('popup-update'));
    let url = "/v1/api/category-page/" + document.getElementById('detail-id').value;

    try {
        await callApi(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, async (resp) => {
            const d = resp?.data ?? resp;

            setStatus(d, "UI");
            setProgress(MAP_STATUS_STEP[d?.status] ?? 0);

            document.getElementById("editCategoryBtn").disabled = d?.status !== 'DRAFT'

            if (d.categoryId) {
                const category = await callApi("/v1/api/category/" + d.categoryId, {method: 'GET'})
                document.querySelector('#edit-categoryName').value = category?.data.name ?? ""
            }

            document.querySelector('#edit-categoryId').value = d?.categoryId ?? "";
            document.querySelector('#edit-description').value = d?.description ?? "";

            if (d?.status === 'DRAFT') {
                document.getElementById('btn-update-detail').classList.remove('d-none')
            } else {
                document.getElementById('btn-update-detail').classList.add('d-none')
            }
        });

        myModal.show();
    } catch (err) {
        console.error("Error loading detail:", err);
    } finally {
        this.disabled = false;  // always re-enable, even if error
    }
});


document.getElementById('openModalNewBtn').addEventListener('click', async function () {
    if (this.disabled) return;   // already running
    this.disabled = true;
    var myModal = new bootstrap.Modal(document.getElementById('popup-new'));
    this.disabled = false;
    myModal.show();
});