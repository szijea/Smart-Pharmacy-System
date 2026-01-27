(function() {
    const API_BASE = '/employees'; // apiCall adds /api prefix
    let allStaff = [];

    // DOM Elements
    const tableBody = document.getElementById('staff-table');
    const modal = document.getElementById('staff-modal');
    const modalBackdrop = document.getElementById('staff-modal-backdrop');
    const form = document.getElementById('staff-form');
    const modalTitle = document.getElementById('modal-title');
    const searchInput = document.getElementById('staff-search');
    const shopSelect = document.getElementById('shop-select');
    const filterRole = document.getElementById('filter-role');
    const filterStatus = document.getElementById('filter-status');

    // API Functions
    async function fetchStaff() {
        try {
            return await apiCall(API_BASE);
        } catch (error) {
            console.error('Error fetching staff:', error);
            return [];
        }
    }

    async function createStaff(data) {
        return await apiCall(API_BASE, {
            method: 'POST',
            body: data
        });
    }

    async function updateStaff(id, data) {
        return await apiCall(`${API_BASE}/${id}`, {
            method: 'PUT',
            body: data
        });
    }

    async function deleteStaffApi(id) {
        return await apiCall(`${API_BASE}/${id}`, {
            method: 'DELETE'
        });
    }

    // Render Functions
    function renderTable(list) {
        tableBody.innerHTML = '';
        if (list.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" class="px-6 py-4 text-center text-gray-500">暂无数据</td></tr>';
            return;
        }

        list.forEach(staff => {
            const tr = document.createElement('tr');
            tr.className = 'hover:bg-gray-50';

            const roleMap = { 1: '管理员', 2: '店长', 3: '收��员', 4: '药剂师' };
            const roleName = roleMap[staff.roleId] || '未知职位';
            const statusBadge = staff.status === 1
                ? '<span class="badge temperature-normal">在岗</span>'
                : '<span class="badge temperature-high">离职/禁用</span>';
            const dateStr = staff.createTime ? new Date(staff.createTime).toLocaleDateString() : '-';

            tr.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${staff.employeeId}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${staff.username}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${staff.name}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${roleName}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${staff.phone || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${dateStr}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">${statusBadge}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button class="text-primary hover:text-blue-900 mr-3 edit-btn" data-id="${staff.employeeId}">编辑</button>
                    <button class="text-red-600 hover:text-red-900 delete-btn" data-id="${staff.employeeId}">删除</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });

        document.getElementById('staff-summary').textContent = `显示 ${list.length} 条数据`;
        updateKPIs(list);
    }

    function updateKPIs(list) {
        const total = list.length;
        const active = list.filter(s => s.status === 1).length;
        const leave = total - active;
        // 简单模拟本月新增：假设 createTime 在本月
        const now = new Date();
        const newMonth = list.filter(s => {
            if (!s.createTime) return false;
            const d = new Date(s.createTime);
            return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
        }).length;

        setText('kpi-staff-total', total);
        setText('kpi-staff-active', active);
        setText('kpi-staff-new', newMonth);
        setText('kpi-staff-leave', leave);
    }

    function setText(id, val) {
        const el = document.getElementById(id);
        if (el) el.textContent = val;
    }

    // Modal Functions
    function openModal(mode, staff = null) {
        modal.style.display = 'flex';
        modalBackdrop.style.display = 'block';

        const idInput = document.getElementById('staff-id');
        const usernameInput = document.getElementById('staff-username');
        const passwordInput = document.getElementById('staff-password');
        const nameInput = document.getElementById('staff-name');
        const roleInput = document.getElementById('staff-role');
        const phoneInput = document.getElementById('staff-phone');
        const statusInput = document.getElementById('staff-status');
        const passwordHint = document.getElementById('password-hint');

        if (mode === 'edit' && staff) {
            modalTitle.textContent = '编辑员工';
            idInput.value = staff.employeeId;
            usernameInput.value = staff.username;
            usernameInput.disabled = true;
            passwordInput.value = '';
            passwordInput.required = false;
            passwordHint.textContent = '(留空不修改)';
            nameInput.value = staff.name;
            roleInput.value = staff.roleId;
            phoneInput.value = staff.phone || '';
            statusInput.value = staff.status;
        } else {
            modalTitle.textContent = '新增员工';
            form.reset();
            idInput.value = '';
            usernameInput.disabled = false;
            passwordInput.required = true;
            passwordHint.textContent = '(新增必填)';
            statusInput.value = 1;
        }
    }

    function closeModal() {
        modal.style.display = 'none';
        modalBackdrop.style.display = 'none';
    }

    // Event Listeners
    function initEvents() {
        // Load data
        loadStaff();

        // Refresh button
        const refreshBtn = document.getElementById('refresh-staff');
        if(refreshBtn) refreshBtn.addEventListener('click', loadStaff);

        // Add button
        const addBtn = document.getElementById('add-staff');
        if(addBtn) addBtn.addEventListener('click', () => openModal('add'));

        // Close modal
        const closeBtn = document.getElementById('close-staff-modal');
        if(closeBtn) closeBtn.addEventListener('click', closeModal);

        const cancelBtn = document.getElementById('cancel-staff');
        if(cancelBtn) cancelBtn.addEventListener('click', closeModal);

        if(modalBackdrop) modalBackdrop.addEventListener('click', closeModal);

        // Form submit
        if(form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                const id = document.getElementById('staff-id').value;
                const formData = {
                    username: document.getElementById('staff-username').value,
                    name: document.getElementById('staff-name').value,
                    roleId: parseInt(document.getElementById('staff-role').value),
                    phone: document.getElementById('staff-phone').value,
                    status: parseInt(document.getElementById('staff-status').value)
                };

                const password = document.getElementById('staff-password').value;
                if (password) {
                    formData.password = password;
                } else if (!id) {
                    alert('新增员工必须填写密码');
                    return;
                }

                const btn = document.getElementById('save-staff');
                const originalText = btn.textContent;
                btn.textContent = '保存中...';
                btn.disabled = true;

                try {
                    if (id) {
                        // Edit
                        if (!formData.password) {
                            const oldStaff = allStaff.find(s => s.employeeId == id);
                            if (oldStaff) formData.password = oldStaff.password;
                        }
                        await updateStaff(id, formData);
                    } else {
                        // Add
                        await createStaff(formData);
                    }
                    closeModal();
                    loadStaff();
                } catch (err) {
                    alert(err.message);
                } finally {
                    btn.textContent = originalText;
                    btn.disabled = false;
                }
            });
        }

        // Table actions (Edit/Delete)
        if(tableBody) {
            tableBody.addEventListener('click', async (e) => {
                if (e.target.classList.contains('edit-btn')) {
                    const id = e.target.dataset.id;
                    const staff = allStaff.find(s => s.employeeId == id);
                    if (staff) openModal('edit', staff);
                } else if (e.target.classList.contains('delete-btn')) {
                    const id = e.target.dataset.id;
                    if (confirm('确定要删除该员工吗？此操作不可恢复。')) {
                        try {
                            await deleteStaffApi(id);
                            loadStaff();
                        } catch (err) {
                            alert(err.message);
                        }
                    }
                }
            });
        }

        // Search & Filter
        if(searchInput) searchInput.addEventListener('input', filterStaff);
        if(shopSelect) shopSelect.addEventListener('change', filterStaff);
        if(filterRole) filterRole.addEventListener('change', filterStaff);
        if(filterStatus) filterStatus.addEventListener('change', filterStaff);
    }

    async function loadStaff() {
        const btn = document.getElementById('refresh-staff');
        let icon;
        if(btn) {
            icon = btn.querySelector('i');
            if(icon) icon.classList.add('fa-spin');
        }

        allStaff = await fetchStaff();
        filterStaff();

        if(icon) icon.classList.remove('fa-spin');
    }

    function filterStaff() {
        const keyword = searchInput ? searchInput.value.toLowerCase() : '';
        const role = filterRole ? filterRole.value : '';
        const status = filterStatus ? filterStatus.value : '';

        const filtered = allStaff.filter(s => {
            const matchKeyword = !keyword || (
                (s.name && s.name.toLowerCase().includes(keyword)) ||
                (s.username && s.username.toLowerCase().includes(keyword)) ||
                (s.phone && s.phone.includes(keyword))
            );
            const matchRole = role === '' || s.roleId == role;
            const matchStatus = status === '' || s.status == status;
            return matchKeyword && matchRole && matchStatus;
        });
        renderTable(filtered);
    }

    // Initialize
    document.addEventListener('DOMContentLoaded', initEvents);

})();
