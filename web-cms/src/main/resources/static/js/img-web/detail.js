async function viewDetail(id) {
    showDetailPanel()
    let url = "/v1/api/image-web/detail/" + id
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, (resp) => {
        const d = resp?.data ?? resp;
        const imgEl = document.getElementById('detail-image');
        imgEl.src = d?.pathImage;
        imgEl.alt = fileNameFromPath(d?.pathImage) || 'image';

        document.getElementById('detail-id').value = d?.id ?? '';
        document.getElementById('detail-desc').value = d?.description ?? '';
        document.getElementById('detail-status').value = mapStatus(d?.status);
        document.getElementById('detail-is-delete').value = mapIsDelete(d?.isDelete);

        document.getElementById('inputAuthor').value = d?.createdByName ?? d?.createdBy ?? '-';
        document.getElementById('inputCreateAt').value = formatDate(d?.createdAt);

        document.getElementById('inputUpdater').value = d?.updatedByName ?? d?.updatedBy ?? '-';
        document.getElementById('inputUpdateAt').value = formatDate(d?.updatedAt);

        if (d?.status === 'DRAFT') {
            document.getElementById('bt-update-detail').classList.remove('d-none')
        } else {
            document.getElementById('bt-update-detail').classList.add('d-none')
        }

    });
    this.disabled = false;
}