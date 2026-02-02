/**
 * Roles & Permissions loader + utilities (global version, 30 min cache)
 *
 * - Depends on: callApi(), showErrorDialog() (optional)
 * - Uses localStorage for roles/permissions cache
 * - Broadcasts changes across tabs
 */

(() => {
    const ROLES_KEY = 'app:roleCodes'; // changed key name
    const PERMS_KEY = 'app:permissionCodes'; // changed key name
    const PERMS_META_KEY = 'app:permission-meta';
    const CACHE_TTL_MS = 30 * 60 * 1000; // 30 minutes

    let permissionSet = new Set();
    let roleSet = new Set();

    /* ------------------- Cross-tab sync ------------------- */
    const bcSupported = typeof BroadcastChannel !== 'undefined';
    const bc = bcSupported ? new BroadcastChannel('app-auth') : null;

    if (bc) {
        bc.onmessage = (ev) => {
            if (!ev?.data) return;
            if (ev.data.type === 'permissions:update') {
                permissionSet = new Set(ev.data.codes || []); // changed ids → codes
            } else if (ev.data.type === 'roles:update') {
                roleSet = new Set(ev.data.codes || []); // changed ids → codes
            } else if (ev.data.type === 'auth:cleared') {
                permissionSet = new Set();
                roleSet = new Set();
            }
        };
    } else {
        // fallback using storage events
        window.addEventListener('storage', (ev) => {
            if (ev.key === PERMS_KEY && ev.newValue)
                try {
                    permissionSet = new Set(JSON.parse(ev.newValue));
                } catch {
                }
            if (ev.key === ROLES_KEY && ev.newValue)
                try {
                    roleSet = new Set(JSON.parse(ev.newValue));
                } catch {
                }
            if (ev.key === 'app:auth:cleared') {
                permissionSet = new Set();
                roleSet = new Set();
            }
        });
    }

    /* ------------------- Persistence helpers ------------------- */
    function savePermissionCodes(codesArray) { // renamed
        try {
            localStorage.setItem(PERMS_KEY, JSON.stringify(codesArray));
            localStorage.setItem(PERMS_META_KEY, JSON.stringify({ts: Date.now(), ttlMs: CACHE_TTL_MS}));

            permissionSet = new Set(codesArray);
            if (bc) bc.postMessage({type: 'permissions:update', codes: codesArray}); // changed ids → codes
            else localStorage.setItem('app:permissions:updatedAt', Date.now().toString());
        } catch (e) {
            console.warn('Could not persist permissionCodes', e);
        }
    }

    function loadPermissionCodes() { // renamed
        try {
            const raw = localStorage.getItem(PERMS_KEY);
            if (!raw) return new Set();

            const metaRaw = localStorage.getItem(PERMS_META_KEY);
            if (metaRaw) {
                try {
                    const {ts, ttlMs} = JSON.parse(metaRaw);
                    if (ts && ttlMs && (Date.now() - ts > ttlMs)) {
                        localStorage.removeItem(PERMS_KEY);
                        localStorage.removeItem(PERMS_META_KEY);
                        return new Set();
                    }
                } catch {
                }
            }

            const arr = JSON.parse(raw);
            return new Set(Array.isArray(arr) ? arr : []);
        } catch {
            return new Set();
        }
    }

    function saveRoleCodes(codesArray) { // renamed
        try {
            localStorage.setItem(ROLES_KEY, JSON.stringify(codesArray));
            roleSet = new Set(codesArray);
            if (bc) bc.postMessage({type: 'roles:update', codes: codesArray}); // changed ids → codes
            else localStorage.setItem('app:roles:updatedAt', Date.now().toString());
        } catch (e) {
            console.warn('Could not persist roleCodes', e);
        }
    }

    function loadRoleCodes() { // renamed
        try {
            const raw = localStorage.getItem(ROLES_KEY);
            if (!raw) return new Set();
            const arr = JSON.parse(raw);
            return new Set(Array.isArray(arr) ? arr : []);
        } catch {
            return new Set();
        }
    }

    /* ------------------- Core utilities ------------------- */
    function hasPermission(code) {
        return permissionSet.has(String(code));
    }

    function hasAnyPermission(codes) {
        for (const code of codes) if (permissionSet.has(String(code))) return true;
        return false;
    }

    function hasRole(code) {
        return roleSet.has(String(code));
    }

    function clearAuthCache() {
        try {
            localStorage.removeItem(PERMS_KEY);
            localStorage.removeItem(PERMS_META_KEY);
            localStorage.removeItem(ROLES_KEY);
        } catch {
        }
        permissionSet = new Set();
        roleSet = new Set();
        if (bc) bc.postMessage({type: 'auth:cleared'});
        else localStorage.setItem('app:auth:cleared', Date.now().toString());
    }

    permissionSet = loadPermissionCodes();
    roleSet = loadRoleCodes();

    /* ------------------- Profile loader ------------------- */
    async function loadProfile({onError = null, onProfileLoaded = null} = {}) {
        const reportError = (err) => {
            if (typeof onError === 'function') return onError(err);
            if (typeof showErrorDialog === 'function')
                showErrorDialog(typeof err === 'string' ? err : err?.message || 'Error loading profile');
            else console.warn('loadProfile error:', err);
        };

        try {
            await callApi('/v1/api/users/current', {
                method: 'GET',
                headers: {'Content-Type': 'application/json'}
            }, async (data) => {
                const user = data?.data;
                if (!user) return;

                // update UI
                document.querySelectorAll('.header-fullName')
                    .forEach(el => el.innerText = user.fullName || '');
                if (user.avatar)
                    document.getElementById('header-avatar').src = user.avatar;

                if (typeof onProfileLoaded === 'function') {
                    try {
                        onProfileLoaded(user);
                    } catch (e) {
                        console.warn('onProfileLoaded failed', e);
                    }
                }

                const roles = Array.isArray(user.roles) ? user.roles : [];
                const roleCodes = roles.map(r => r?.code).filter(Boolean); // changed id → code
                saveRoleCodes(roleCodes);

                if (roles.length === 0) {
                    savePermissionCodes([]);
                    return;
                }

                const permissionCodeSet = new Set(); // renamed
                roles.forEach(role => {
                    (role.permissions || []).forEach(p => {
                        permissionCodeSet.add(String(p.code)); // changed id → code
                    });
                });
                savePermissionCodes(Array.from(permissionCodeSet));
            });
        } catch (err) {
            reportError(err);
            throw err;
        }
    }

    window.rolesPermissions = {
        hasPermission,
        hasAnyPermission,
        hasRole,
        clearAuthCache,
        loadProfile,
        getRoles: () => Array.from(roleSet),
        getPermissions: () => Array.from(permissionSet),
    };
})();


