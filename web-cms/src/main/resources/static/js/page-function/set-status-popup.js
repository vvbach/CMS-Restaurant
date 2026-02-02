function setStatus(d, permissionType){
    let permission = ""
    permission += permissionType + "_"
    // xóa hết dữ liệu
    document.getElementById('detail-list-status').innerHTML = '';

    if (d?.status === 'Draft') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="PendingApproval">Chờ phê duyệt</button></li>`) ;
        if (!rolesPermissions.hasPermission(permission + "DRAFT")) {
            document.getElementById('btnChange').remove();
            document.getElementById('btnSaveChange').remove();
        }
    }
    else if (d?.status === 'Rejected') {
        showDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        updateReason( d?.reasonRejection,'#detail-reason-reject')
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="PendingApproval">Chờ phê duyệt</button></li>`) ;
        if (!rolesPermissions.hasPermission(permission + "REJECT")) {
            document.getElementById('btnChange').remove();
            document.getElementById('btnSaveChange').remove();
        }
    }
    else if (d?.status === 'PendingApproval') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Rejected"><b>Từ chối</b></button></li>
                            <li><button class="dropdown-item" data-action="Approved">Duyệt</button></li>`) ;
        if (!rolesPermissions.hasPermission(permission + "PENDING_APPROVE")) {
            document.getElementById('btnChange').remove();
            document.getElementById('btnSaveChange').remove();
        }
    }
    else if (d?.status === 'Approved') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Rejected"><b>Từ chối</b></button></li>
                            <li><button class="dropdown-item" data-action="Publish">Xuất bản</button></li>`) ;
        if (!rolesPermissions.hasPermission(permission + "APPROVE")) {
            document.getElementById('btnChange').remove();
            document.getElementById('btnSaveChange').remove();
        }
    }
    else if (d?.status === 'Publish') {
        hideDiv('detail-reason-reject');
        hideDiv('detail-reason-unpublish');
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Unpublish">Huỷ xuất bản</button></li>`) ;
        if (!rolesPermissions.hasPermission(permission + "PUBLISH")) {
            document.getElementById('btnChange').remove();
            document.getElementById('btnSaveChange').remove();
        }
    }
    else if (d?.status === 'Unpublish') {
        hideDiv('detail-reason-reject');
        showDiv('detail-reason-unpublish');
        updateReason( d?.reasonUnpublish,'#detail-reason-unpublish')
        document.getElementById('detail-list-status').insertAdjacentHTML('beforeend',
            `<li><button class="dropdown-item" data-action="Publish">Xuất bản</button></li>
                            <li><button class="dropdown-item" data-action="Draft">Nháp</button></li>`) ;
        if (!rolesPermissions.hasPermission(permission + "UNPUBLISH")) {
            document.getElementById('btnChange').remove();
            document.getElementById('btnSaveChange').remove();
        }
    }

    if (d?.isDelete === 'YES') {
        showDiv('detail-reason-delete')
        updateReason( d?.reasonDelete,'#detail-reason-delete')
    } else {
        hideDiv('detail-reason-delete')
    }

    document.getElementById('fldTrangThai').value = mapStatus(d?.status);
    document.getElementById('headerState').textContent = mapStatus(d?.status);
    document.getElementById('detail-txt-status').textContent = mapStatus(d?.status);
    document.getElementById('detail-txt-delete').textContent = mapIsDelete(d?.isDelete);
    document.getElementById('created-user').value = d?.createdByName;
    document.getElementById('created-at').value =viDateTime(d?.createdAt);
    document.getElementById('updated-user').value = d?.updatedByName
    document.getElementById('updated-at').value = viDateTime(d?.updatedAt);
    document.getElementById('id-row-edit').value = d?.id;
}

function updateReason(newReason, divId) {
    if (newReason) {
        const textarea = document.querySelector(divId +' textarea');
        textarea.value = newReason;
    }
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