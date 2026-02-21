async function viewDetail(id) {
    showDetailPanel()
    let url = "/v1/api/food/" + id
    await callApi(url, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
    }, (resp) => {
        const d = resp?.data ?? resp;

        document.getElementById('detail-id').value = d?.id ?? '';
        document.getElementById('detail-image').src = d?.imageUrl ?? '';
        document.getElementById('detail-name').value = d?.name ?? '';
        document.getElementById('detail-description').value = d?.description ?? '';
        document.getElementById('detail-price').value = d?.price ?? '';
        document.getElementById('detail-discount').value = d?.discount ?? '';
        document.getElementById('detail-stock-quantity').value = d?.stockQuantity ?? '';
        document.getElementById('detail-discount-price').value = d?.discountPrice ?? '';

        document.getElementById('detail-status').value = mapStatus(d?.status);
        document.getElementById('detail-is-delete').value = mapIsDelete(d?.isDelete);
        document.getElementById('inputAuthor').value = d?.createdByName ?? d?.createdBy ?? '-';
        document.getElementById('inputCreateAt').value = formatDate(d?.createdAt);
        document.getElementById('inputUpdater').value = d?.updatedByName ?? d?.updatedBy ?? '-';
        document.getElementById('inputUpdateAt').value = formatDate(d?.updatedAt);
        setProgress(MAP_STATUS_STEP[d?.status] ?? 0);
        if (d?.status === 'DRAFT') {
            document.getElementById('btn-update-detail').classList.remove('d-none')
        } else {
            document.getElementById('btn-update-detail').classList.add('d-none')
        }

    });
    this.disabled = false;
}