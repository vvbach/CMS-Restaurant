async function viewDetail(id) {
    showDetailPanel()
    let url = "/v1/api/category-best-food/" + id
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, async (resp) => {
        const d = resp?.data ?? resp;

        document.getElementById('detail-id').value = d?.id ?? '';
        document.getElementById('detail-description').value = d?.description ?? '';
        document.getElementById('detail-status').value = mapStatus(d?.status);
        document.getElementById('detail-is-delete').value = mapIsDelete(d?.isDelete);
        document.getElementById('inputAuthor').value = d?.createdByName ?? d?.createdBy ?? '-';
        document.getElementById('inputCreateAt').value = formatDate(d?.createdAt);
        document.getElementById('inputUpdater').value = d?.updatedByName ?? d?.updatedBy ?? '-';
        document.getElementById('inputUpdateAt').value = formatDate(d?.updatedAt);

        if (d.foodId) {
            await callApi("/v1/api/food/" + d?.foodId, {
                method: 'GET',
                headers: {'Content-Type': 'application/json'}
            }, (res) => {
                const foodData = res?.data ?? res;
                document.getElementById('detail-food').value = foodData.name;
                document.getElementById('detail-image').src = foodData?.imageUrl;
            })
        } else {
            document.getElementById('detail-food').value = '-';
        }

        const openModalDeleteBtn = document.getElementById('openModalDeleteBtn')
        if (d?.status === 'DRAFT') {
            document.getElementById('btn-update-detail').classList.remove('d-none')
            openModalDeleteBtn.classList.remove('d-none')
        } else {
            document.getElementById('btn-update-detail').classList.add('d-none')
            openModalDeleteBtn.classList.add('d-none')
        }

    });
    this.disabled = false;
}