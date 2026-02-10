async function viewDetail(id) {
    showDetailPanel()
    let url = "/v1/api/featured-category/" + id
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, async (resp) => {
        const d = resp?.data ?? resp;

        document.getElementById('detail-id').value = d?.id ?? '';
        document.getElementById('detail-image').src = d?.imageUrl ?? '';
        document.getElementById('detail-description').value = d?.description ?? '';
        document.getElementById('detail-status').value = mapStatus(d?.status);
        document.getElementById('detail-is-delete').value = mapIsDelete(d?.isDelete);
        document.getElementById('inputAuthor').value = d?.createdByName ?? d?.createdBy ?? '-';
        document.getElementById('inputCreateAt').value = formatDate(d?.createdAt);
        document.getElementById('inputUpdater').value = d?.updatedByName ?? d?.updatedBy ?? '-';
        document.getElementById('inputUpdateAt').value = formatDate(d?.updatedAt);

        if (d.categoryId) {
            await callApi("/v1/api/category/" + d.categoryId, {
                method: 'GET',
                headers: {'Content-Type': 'application/json'}
            }, (res) => {
                const foodData = res?.data ?? res;
                document.getElementById('detail-category').value = foodData.name;
            })
        } else {
            document.getElementById('detail-category').value = '-';
        }

        if (d?.status === 'DRAFT') {
            document.getElementById('btn-update-detail').classList.remove('d-none')
        } else {
            document.getElementById('btn-update-detail').classList.add('d-none')
        }

    });
    this.disabled = false;
}