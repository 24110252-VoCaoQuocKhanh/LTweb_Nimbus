const TOKEN_KEY = "jwt_token";

function message(text, kind = "error") {
    const element = document.getElementById("message");
    if (!element) return;
    element.textContent = text;
    element.className = `message visible ${kind}`;
}

async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set("Content-Type", "application/json");
    const token = localStorage.getItem(TOKEN_KEY);
    const isProtectedApi = path.startsWith("/users");
    if (token && isProtectedApi) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(path, {...options, headers});
    const body = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
        const validation = body?.validationErrors
            ? Object.values(body.validationErrors).join(". ")
            : "";
        throw new Error(validation || body?.message || `HTTP ${response.status}`);
    }
    return body;
}

function initLogin() {
    document.getElementById("loginForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            const result = await api("/auth/login", {
                method: "POST",
                body: JSON.stringify({
                    email: document.getElementById("email").value,
                    password: document.getElementById("password").value
                })
            });
            localStorage.setItem(TOKEN_KEY, result.token);
            window.location.href = "/user/profile";
        } catch (error) {
            message(error.message);
        }
    });
}

function initRegister() {
    document.getElementById("registerForm").addEventListener("submit", async (event) => {
        event.preventDefault();
        try {
            await api("/auth/signup", {
                method: "POST",
                body: JSON.stringify({
                    fullName: document.getElementById("fullName").value,
                    email: document.getElementById("email").value,
                    password: document.getElementById("password").value
                })
            });
            message("Đăng ký thành công. Đang chuyển đến trang đăng nhập...", "success");
            setTimeout(() => window.location.href = "/login", 800);
        } catch (error) {
            message(error.message);
        }
    });
}

function renderProfile(user) {
    document.getElementById("profile").innerHTML = `
        <dl>
            <dt>Họ tên</dt><dd>${escapeHtml(user.fullName)}</dd>
            <dt>Email</dt><dd>${escapeHtml(user.email)}</dd>
            <dt>Vai trò</dt><dd><span class="badge">${escapeHtml(user.role)}</span></dd>
            <dt>Ngày tạo</dt><dd>${formatDate(user.createdAt)}</dd>
        </dl>`;
}

function renderUsers(users) {
    const rows = users.map(user => `
        <tr>
            <td>${user.id}</td>
            <td>${escapeHtml(user.fullName)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td><span class="badge">${escapeHtml(user.role)}</span></td>
        </tr>`).join("");
    document.getElementById("users").innerHTML = `
        <table>
            <thead><tr><th>ID</th><th>Họ tên</th><th>Email</th><th>Vai trò</th></tr></thead>
            <tbody>${rows}</tbody>
        </table>`;
}

async function loadProfile() {
    try {
        const [currentUser, users] = await Promise.all([api("/users/me"), api("/users")]);
        renderProfile(currentUser);
        renderUsers(users);
    } catch (error) {
        localStorage.removeItem(TOKEN_KEY);
        message(error.message);
        setTimeout(() => window.location.href = "/login", 900);
    }
}

function initProfile() {
    if (!localStorage.getItem(TOKEN_KEY)) {
        window.location.href = "/login";
        return;
    }
    document.getElementById("logoutButton").addEventListener("click", () => {
        localStorage.removeItem(TOKEN_KEY);
        window.location.href = "/login";
    });
    document.getElementById("reloadUsers").addEventListener("click", async () => {
        try {
            renderUsers(await api("/users"));
            message("Đã cập nhật danh sách.", "success");
        } catch (error) {
            message(error.message);
        }
    });
    loadProfile();
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>'"]/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
    })[character]);
}

function formatDate(value) {
    return value ? new Intl.DateTimeFormat("vi-VN", {dateStyle: "medium", timeStyle: "short"})
        .format(new Date(value)) : "-";
}

const page = document.body.dataset.page;
if (page === "login") initLogin();
if (page === "register") initRegister();
if (page === "profile") initProfile();
