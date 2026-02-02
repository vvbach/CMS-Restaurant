function isValidUrl(string) {
    try {
        new URL(string);
        return true;
    } catch (_) {
        return false;
    }
}

function showSuccess(message) {
    alert(message);
}

function showError(message) {
    console.error(message);
    alert(message);
}

