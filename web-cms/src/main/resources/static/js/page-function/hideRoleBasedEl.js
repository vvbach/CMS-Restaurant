function hideAddBtn(permissionType) {
    if (!rolesPermissions.hasPermission(permissionType + "_ADD")) {
        const el = document.getElementById("openModalNewBtn");
        if (el) el.remove();
    }
}

function hideUpdateBtn(permissionType) {
    if (!rolesPermissions.hasPermission(permissionType + "_UPDATE")){
        const el = document.getElementById("btn-update-detail");
        if (el) el.remove();
    }
}

function hideDeleteBtn(permissionType) {
    if (!rolesPermissions.hasPermission(permissionType + "_DELETE")){
        const el = document.getElementById("openModalDeleteBtn");
        if (el) el.remove();
    }
}