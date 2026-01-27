// api.js
class PharmacyAPI {
    constructor() {
        // 支持通过 window.API_BASE 或全局常量覆盖，默认改为使用相对路径 /api，避免硬编码端口
        this.baseURL = (window.API_BASE || window.__API_BASE__ || '/api').replace(/\/$/, '');
        this.tenantHeaderKey = 'X-Shop-Id';
    }

    getTenant() {
        return localStorage.getItem('selectedTenant') || localStorage.getItem('tenant') || 'wx';
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const tenant = this.getTenant();
        const config = {
            method: options.method || 'GET',
            headers: {
                'Content-Type': 'application/json',
                [this.tenantHeaderKey]: tenant,
                ...options.headers,
            },
            ...options,
        };
        if (config.body && typeof config.body === 'object') {
            config.body = JSON.stringify(config.body);
        }
        try {
            const response = await fetch(url, config);
            if (!response.ok) {
                let detail = '';
                try { detail = await response.text(); } catch(_){}
                throw new Error(`HTTP ${response.status} ${response.statusText} -> ${detail.slice(0,200)}`);
            }
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                return await response.json();
            }
            return await response.text();
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    // 设置API
    settingAPI = {
        get: () => this.request('/settings'),
        update: (data) => this.request('/settings', { method: 'POST', body: data }),
    };

    // 用户API
    userAPI = {
        changePassword: (oldPassword, newPassword) => this.request('/user/change-password', {
            method: 'POST',
            body: { oldPassword, newPassword, confirmPassword: newPassword },
        }),
    };

    // 会员API
    memberAPI = {
        list: (page=1,size=20) => this.request(`/members?page=${page}&size=${size}`),
        stats: () => this.request('/members/stats'),
        search: (keyword='', page=1, size=20) => this.request(`/members/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`),
        create: (data) => this.request('/members', { method: 'POST', body: data }),
        update: (id,data) => this.request(`/members/${encodeURIComponent(id)}`, { method:'PUT', body:data }),
        delete: (id) => this.request(`/members/${encodeURIComponent(id)}`, { method:'DELETE' }),
        debugAll: () => this.request('/members/debug-all')
    };

    // 药品API
    medicineAPI = {
        getAll: (page = 0, size = 100) => this.request(`/medicines?page=${page}&size=${size}`),
        searchWithStock: (keyword = '', category = '', page = 1, size = 100) => this.request(`/medicines/search-with-stock?keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(category)}&page=${page}&size=${size}`),
        getById: (id) => this.request(`/medicines/${encodeURIComponent(id)}`),
        update: (id, data) => this.request(`/medicines/${encodeURIComponent(id)}`, { method: 'PUT', body: data }),
        delete: (id) => this.request(`/medicines/${encodeURIComponent(id)}`, { method: 'DELETE' }),
        create: (data) => this.request('/medicines', { method: 'POST', body: data })
    };

    // 库存API
    inventoryAPI = {
        getAll: () => this.request('/inventory'),
        getDetail: (id) => this.request(`/inventory/${encodeURIComponent(id)}`),
        getByMedicine: (medicineId) => this.request(`/inventory/by-medicine/${encodeURIComponent(medicineId)}`),
        searchByBatch: (batchNo) => this.request(`/inventory/search-batch?batchNo=${encodeURIComponent(batchNo)}`)
    };

    // 供应商API
    supplierAPI = {
        list: () => this.request('/suppliers'),
        createDefaultIfMissing: async () => {
            try { const res = await this.request('/suppliers'); if(Array.isArray(res) && res.length>0) return res; } catch(e){ /* ignore */ }
            // 如果后端存在自动创建逻辑，则这里仅提示
            return [];
        }
    };

    // 订单API
    orderAPI = {
        ping: () => this.request('/orders/ping'),
        create: (data) => this.request('/orders', { method: 'POST', body: data }),
        list: (page=1,size=20) => this.request(`/orders?page=${page}&size=${size}`),
        search: (params={}) => {
            const q = new URLSearchParams(params).toString();
            return this.request(`/orders/search?${q}`);
        },
        getOrderDetail: (orderId) => this.request(`/orders/${encodeURIComponent(orderId)}`),
        refund: (orderId, reason='') => this.request(`/orders/${encodeURIComponent(orderId)}/refund`, { method:'POST', body:{ reason } })
    };

    // 消息提示
    showMessage(message, type = 'success') {
        const messageEl = document.createElement('div');
        messageEl.className = `fixed top-4 right-4 px-6 py-3 rounded-lg shadow-lg z-50 transform transition-all duration-300 ${
            type === 'success' ? 'bg-green-500 text-white' :
            type === 'error' ? 'bg-red-500 text-white' :
            type === 'warning' ? 'bg-yellow-500 text-white' : 'bg-blue-500 text-white'
        }`;
        messageEl.textContent = message;
        document.body.appendChild(messageEl);
        setTimeout(() => {
            messageEl.style.opacity = '0';
            messageEl.style.transform = 'translateX(100%)';
            setTimeout(() => { document.body.contains(messageEl) && document.body.removeChild(messageEl); }, 300);
        }, 3000);
    }
}

// 创建全局API实例（若已存在不覆盖，方便调试重载）
if(!window.api){ window.api = new PharmacyAPI(); } else { window.api.baseURL = (window.API_BASE || window.api.baseURL); }
