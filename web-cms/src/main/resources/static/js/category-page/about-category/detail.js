async function viewDetail(id) {
    viewUIDetail()
    let url = "/v1/api/about-category/" + id
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, async (resp) => {
        const d = resp?.data ?? resp;

        document.getElementById('detail-id').value = d?.id ?? '';
        document.getElementById('detail-description').value = d?.description ?? '';
        document.getElementById('detail-title').value = d?.title ?? '';
        document.getElementById('detail-subtitle').value = d?.subtitle ?? '';
        document.getElementById('detail-status').value = mapStatus(d?.status);
        document.getElementById('detail-is-delete').value = mapIsDelete(d?.isDelete);
        document.getElementById('inputAuthor').value = d?.createdByName ?? d?.createdBy ?? '-';
        document.getElementById('inputCreateAt').value = viDateTime(d?.createdAt);
        document.getElementById('inputUpdater').value = d?.updatedByName ?? d?.updatedBy ?? '-';
        document.getElementById('inputUpdateAt').value = viDateTime(d?.updatedAt);

        const openModalDeleteBtn = document.getElementById('openModalDeleteBtn')
        if (d?.status === 'Draft') {
            document.getElementById('btn-update-detail').classList.remove('d-none')
            openModalDeleteBtn.classList.remove('d-none')
        } else {
            document.getElementById('btn-update-detail').classList.add('d-none')
            openModalDeleteBtn.classList.add('d-none')
        }

    });
    this.disabled = false;
}