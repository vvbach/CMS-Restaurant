function setStatus(d, permissionType){
    let permission = ""
    permission += permissionType + "_"
    document.getElementById('detail-list-status').innerHTML = '';

    if (d?.status === 'DRAFT') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="PendingApproval">Pending Approval</button></li>`) ;
        if (!rolesPermissions.hasPermission("UI_DRAFT")) {
            removeButtons()
        }
    }
    else if (d?.status === 'REJECTED') {
        showDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        updateReason( d?.rejectionReason,'#detail-reason-reject')
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="PendingApproval">Pending Approval</button></li>`) ;
        if (!rolesPermissions.hasPermission("UI_REJECT")) {
            removeButtons()
        }
    }
    else if (d?.status === 'PENDING_APPROVAL') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Reject"><b>Reject</b></button></li>
                  <li><button class="dropdown-item" data-action="Approve">Approve</button></li>`) ;
        if (!rolesPermissions.hasPermission("UI_PENDING_APPROVE")) {
            removeButtons()
        }
    }
    else if (d?.status === 'APPROVED') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Reject"><b>Reject</b></button></li>
                  <li><button class="dropdown-item" data-action="Publish">Publish</button></li>`) ;
        if (!rolesPermissions.hasPermission("UI_APPROVE")) {
            removeButtons()
        }
    }
    else if (d?.status === 'PUBLISHED') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Unpublish">Unpublish</button></li>`) ;
        if (!rolesPermissions.hasPermission("UI_PUBLISH")) {
            removeButtons()
        }
    }
    else if (d?.status === 'UNPUBLISHED') {
        hideDiv('detail-reason-reject');
        showDiv('detail-reason-unpublish');
        updateReason( d?.unpublishReason,'#detail-reason-unpublish')
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Publish">Publish</button></li>
                  <li><button class="dropdown-item" data-action="Draft">Draft</button></li>`) ;
        if (!rolesPermissions.hasPermission("UI_UNPUBLISH")) {
            removeButtons()
        }
    }

    if (d?.isDelete === 'YES') {
        showDiv('detail-reason-delete')
        updateReason( d?.deletionReason,'#detail-reason-delete')
    } else {
        hideDiv('detail-reason-delete')
    }

    document.getElementById('statusField').value = mapStatus(d?.status);
    document.getElementById('headerState').textContent = mapStatus(d?.status);
    document.getElementById('detail-txt-status').textContent = mapStatus(d?.status);
    document.getElementById('detail-txt-delete').textContent = mapIsDelete(d?.isDelete);
    document.getElementById('created-user').value = d?.createdByName;
    document.getElementById('created-at').value =formatDate(d?.createdAt);
    document.getElementById('updated-user').value = d?.updatedByName
    document.getElementById('updated-at').value = formatDate(d?.updatedAt);
    document.getElementById('id-row-edit').value = d?.id;
}

function updateReason(newReason, divId) {
    if (newReason) {
        const textarea = document.querySelector(divId +' textarea');
        textarea.value = newReason;
    }
}

function removeButtons(){
    const btnChange = document.getElementById('btnChange')
    if (btnChange) btnChange.remove();
    const btnSaveChange = document.getElementById('btnSaveChange')
    if (btnSaveChange) btnSaveChange.remove();
}

document.getElementById("openModalDeleteBtn").addEventListener("click", async function (e) {
    e.preventDefault(); // Ngăn reload trang
    this.disabled = true;
    let id = document.getElementById('detail-id').value || null;
    if (id !== null) {
        let myModal = new bootstrap.Modal(document.getElementById('confirmDeleteModal'));
        document.getElementById('id-confirm-delete').value = id;
        myModal.show();
    }

    this.disabled = false

});