document.getElementById('openModalHistoryBtn').addEventListener('click', async function () {
    if (!document.getElementById('detail-id').value) return;

    this.disabled = true;

    try {
        let myModal = new bootstrap.Modal(document.getElementById('history-modal'));
        let id = document.getElementById('detail-id').value;

        // call about history endpoint (was admin-unit)
        await callApi(`/v1/api/about/${id}/history`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        }, (resp) => {

            const d = resp?.data ?? resp;
            const container = document.getElementById('historyTableBody');

            if (!d || d.length === 0) {
                container.innerHTML = `
                    <div class="text-center text-muted py-4">
                        No history found
                    </div>
                `;
                return;
            }

            d.sort((a, b) => new Date(b.eventDate) - new Date(a.eventDate));

            container.innerHTML = `
                <div class="timeline">
                    ${d.map(item => `
                        <div class="timeline-item border-start border-3 ps-3 mb-4">

                            <div class="d-flex align-items-center gap-3 mb-2">

                                ${item.imageUrl ? `
                                    <img src="${item.imageUrl}"
                                         alt="about image"
                                         class="rounded border"
                                         style="width: 40px; height: 40px; object-fit: cover;">
                                ` : `
                                    <div class="bg-light border rounded d-flex align-items-center justify-content-center"
                                         style="width: 40px; height: 40px;">
                                        <i class="bi bi-image text-muted"></i>
                                    </div>
                                `}
                                
                                <div>
                                    <div class="fw-semibold">${item.title ?? '-'}</div>
                                    <div class="small text-muted">
                                        ${item.text ?? '-'}
                                    </div>
                                </div>
                                
                                <div>
                                    <div class="small text-muted">
                                        ${formatDate(item.eventDate)}
                                    </div>
                                </div>

                                <span class="badge ${getStatusColor(item.status)} ms-auto">
                                    ${mapStatus(item.status)}
                                </span>

                            </div>

                            ${item.rejectionReason ? `
                                <div class="text-danger small mt-1">
                                    <strong>Rejection:</strong> ${item.rejectionReason}
                                </div>
                            ` : ''}

                            ${item.unpublishReason ? `
                                <div class="text-warning small mt-1">
                                    <strong>Unpublish:</strong> ${item.unpublishReason}
                                </div>
                            ` : ''}

                            ${item.deletionReason ? `
                                <div class="text-danger small mt-1">
                                    <strong>Deletion:</strong> ${item.deletionReason}
                                </div>
                            ` : ''}

                        </div>
                    `).join('')}
                </div>
            `;
        });

        myModal.show();
    } finally {
        this.disabled = false;
    }
});
