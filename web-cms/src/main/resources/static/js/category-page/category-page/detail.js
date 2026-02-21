async function viewDetail(id) {
    let url = "/v1/api/category-page/" + id
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, async (resp) => {
        const d = resp?.data ?? resp;

        const openModalDeleteBtn = document.getElementById('openModalDeleteBtn')
        if (d?.status === 'DRAFT') {
            document.getElementById('btn-update-detail').classList.remove('d-none')
            openModalDeleteBtn.classList.remove('d-none')
        } else {
            document.getElementById('btn-update-detail').classList.add('d-none')
            openModalDeleteBtn.classList.add('d-none')
        }

        document.getElementById('detail-id').value = d?.id ?? '';
        document.getElementById('detail-description').value = d?.description ?? '';
        document.getElementById('detail-status').value = mapStatus(d?.status);
        document.getElementById('detail-is-delete').value = mapIsDelete(d?.isDelete);
        document.getElementById('inputAuthor').value = d?.createdByName ?? d?.createdBy ?? '-';
        document.getElementById('inputCreateAt').value = formatDate(d?.createdAt);
        document.getElementById('inputUpdater').value = d?.updatedByName ?? d?.updatedBy ?? '-';
        document.getElementById('inputUpdateAt').value = formatDate(d?.updatedAt);
        setProgress(MAP_STATUS_STEP[d?.status] ?? 0);

        if (d.categoryId) {
            await callApi("/v1/api/category/" + d.categoryId, {
                method: 'GET'
            }, (res) => {
                const categoryData = res?.data ?? res;
                document.getElementById('detail-category').value = categoryData.name;
            });
        } else {
            document.getElementById('detail-category').value = "-"
        }

        if (d?.status === 'DRAFT') {
            document.getElementById('btn-update-detail').classList.remove('d-none')
        } else {
            document.getElementById('btn-update-detail').classList.add('d-none')
        }

    });
    showDetailPanel()
    this.disabled = false;
}