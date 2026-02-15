function updateItem(config) {
    return async function(e) {
        e.preventDefault();
        this.disabled = true;

        // Sync all TinyMCE editors back into their <textarea>
        tinymce.triggerSave();

        // Build payload dynamically
        const payload = {};
        for (const field of config.fields) {
            const element = document.getElementById(field.elementId);
            if (element) {
                if (field.type === 'image') {
                    // Handle image URL extraction
                    payload[field.key] = decodeURIComponent(new URL(element.src).pathname.split('/tts/')[1]);
                } else if (field.type === 'multi-select') {
                    // Handle multiple select values
                    payload[field.key] = Array.from(element.selectedOptions).map(opt => opt.value);
                } else if (field.type === 'tinymce') {
                    // Handle TinyMCE editor
                    payload[field.key] = tinymce.get(field.elementId).getContent();
                } else {
                    // Handle regular input values
                    payload[field.key] = element.value;
                }
            }
        }

        const jsonPayload = JSON.stringify(payload);

        try {
            const id = document.getElementById(config.idElementId || 'id-row-edit').value;
            const url = config.apiEndpoint + id;

            await callApi(url, {
                method: config.method || 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: jsonPayload
            }, (data) => {
                showSuccessDialog(data.message);
                handleFilter();
            });
        } catch (err) {
            console.error(err);
        } finally {
            closeAllModals();
            this.disabled = false;
        }
    };
}
