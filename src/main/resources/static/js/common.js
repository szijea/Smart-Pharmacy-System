// common.js - 统一版
(function(){
if (typeof window !== 'undefined' && window.__COMMON_JS_LOADED__) {
    console.warn('[common.js] 已加载，跳过重复初始化');
    return;
}
if (typeof window !== 'undefined') { window.__COMMON_JS_LOADED__ = true; }

// 统一 API 基础地址：优先使用部署时注入的 window.API_BASE，其次 location.origin + '/api'
const BASE_URL = (function(){
  if (typeof window !== 'undefined') {
    if (window.API_BASE) return window.API_BASE.replace(/\/$/, '');
    if (window.location && window.location.origin) return window.location.origin + '/api';
  }
  return '/api'; // 最后回退为相对路径，避免硬编码端口
})();

// 通用API调用函数
async function apiCall(endpoint, options = {}) {
    try {
        const url = `${BASE_URL}${endpoint}`;
        const tenant = (typeof localStorage !== 'undefined') ? localStorage.getItem('selectedTenant') : null;
        if(!options.headers) options.headers = {};
        const defaultTenant = 'wx';
        if(tenant){
            options.headers['X-Shop-Id'] = tenant;
        } else {
            console.warn('[apiCall] 未找到 selectedTenant, 使用默认租户', defaultTenant, '，请求URL:', url);
            options.headers['X-Shop-Id'] = defaultTenant;
        }
        const method = (options.method || (options.body ? 'POST' : 'GET')).toUpperCase();
        // 自动序列化 body
        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData) && !(options.body instanceof Blob)) {
            options.body = JSON.stringify(options.body);
        }
        if (options.body) { console.log(`[apiCall] 准备发送 ${method} ${url} Body长度=${options.body.length}`); }

        // Ensure default headers
        const defaultHeaders = {
            'Content-Type': 'application/json; charset=UTF-8',
            'Accept': 'application/json'
        };

        let headers = {
            ...defaultHeaders,
            ...options.headers
        };

        // Construct fetch options, explicitly processing headers
        const fetchOptions = {
            ...options,
            headers: headers
        };

        const response = await fetch(url, fetchOptions);
        // 415 兜底重试：增加 charset
        if(response.status === 415) {
            console.warn('[apiCall] 收到 415，尝试使用 charset 重试:', url);
            headers['Content-Type'] = 'application/json; charset=UTF-8';
            const retry = await fetch(url, { ...options, headers: headers });
            if(!retry.ok){
                let detail='';
                try{ detail = await retry.text(); }catch(e){ }
                throw new Error(`HTTP 415 重试仍失败: ${detail}`);
            } else {
                const ct = retry.headers.get('content-type');
                return ct && ct.includes('application/json') ? await retry.json() : await retry.text();
            }
        }
        if (!response.ok) {
            let errorDetail = '';
            try { const errorResponse = await response.json(); errorDetail = errorResponse.message || JSON.stringify(errorResponse); console.error('错误响应详情:', errorResponse); } catch (e) { errorDetail = await response.text(); console.error('错误响应文本:', errorDetail); }
            throw new Error(`HTTP error! status: ${response.status}, 详情: ${errorDetail}`);
        }
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) { const result = await response.json(); console.log('API 调用成功 (JSON):', result); return result; }
        const text = await response.text(); console.warn('API 返回非 JSON 响应，返回文本:', text); return text;
    } catch (error) { console.error('API 调用异常:', error); throw error; }
}

// 药品相关API
const medicineAPI = {
    getAll: () => apiCall('/medicines'),
    getById: (id) => apiCall(`/medicines/${id}`),
    search: (keyword, category = '', page = 1, size = 100) => {
        let url = `/medicines/search?page=${page}&size=${size}`;
        if (keyword) { url += `&keyword=${encodeURIComponent(keyword)}`; }
        if (category) { url += `&category=${encodeURIComponent(category)}`; }
        return apiCall(url);
    },
    searchWithStock: function(keyword, category = '', page = 1, size = 100) {
        let url = `/medicines/search-with-stock?page=${page}&size=${size}`;
        if (keyword) { url += `&keyword=${encodeURIComponent(keyword)}`; }
        if (category) { url += `&category=${encodeURIComponent(category)}`; }
        console.log('调用 searchWithStock, URL:', url);
        return apiCall(url);
    },
    create: (medicine) => apiCall('/medicines', { method: 'POST', body: medicine }),
    // 专用更新：直接使用 fetch，内部处理 415 并返回最终结果，避免先抛错后再回退造成控制台报错
    update: async (id, medicine) => {
        const tenant = (typeof localStorage !== 'undefined') ? localStorage.getItem('selectedTenant') : null;
        const url = `${BASE_URL}/medicines/${id}`;
        const headers = { 'Content-Type':'application/json', 'Accept':'application/json' };
        if(tenant) headers['X-Shop-Id'] = tenant;
        const body = JSON.stringify(medicine);
        let resp = await fetch(url, { method:'PUT', headers, body });
        if (resp.status === 415) {
            console.warn('[medicineAPI.update] 收到 415，尝试 charset 重试');
            const headers2 = { ...headers, 'Content-Type':'application/json; charset=UTF-8' };
            resp = await fetch(url, { method:'PUT', headers: headers2, body });
        }
        const ct = resp.headers.get('content-type');
        const data = ct && ct.includes('application/json') ? await resp.json() : await resp.text();
        if (!resp.ok) {
            throw new Error(`HTTP ${resp.status} ${typeof data==='string'?data:JSON.stringify(data)}`);
        }
        return data;
    },
    delete: (id) => apiCall(`/medicines/${id}`, { method: 'DELETE' })
};

// 订单相关API统一自动序列化
const orderAPI = {
    create: (orderData) => apiCall('/orders', { method: 'POST', body: orderData }),
    getOrders: (filters = {}, page = 1, size = 10) => { let url = `/orders?page=${page}&size=${size}`; if (filters.date) url += `&date=${encodeURIComponent(filters.date)}`; if (filters.status) url += `&status=${encodeURIComponent(filters.status)}`; if (filters.paymentType) url += `&paymentType=${encodeURIComponent(filters.paymentType)}`; if (filters.member) url += `&member=${encodeURIComponent(filters.member)}`; return apiCall(url); },
    getOrderDetail: (orderId) => apiCall(`/orders/${orderId}`),
    refund: (orderId) => apiCall(`/orders/${orderId}/refund`, { method: 'POST' }),
    exportOrders: (filters = {}) => { let url = '/orders/export'; const params = new URLSearchParams(); if (filters.date) params.append('date', filters.date); if (filters.status) params.append('status', filters.status); if (filters.paymentType) params.append('paymentType', filters.paymentType); if (filters.member) params.append('member', filters.member); const qs = params.toString(); if(qs) url += `?${qs}`; return apiCall(url); }
};

// 分类相关API
const categoryAPI = {
    getAll: () => apiCall('/categories'),
    getById: (id) => apiCall(`/categories/${id}`)
};

// 会员相关API统一
const memberAPI = {
    getAll: () => apiCall('/members'), getById: (id) => apiCall(`/members/${id}`), search: (k) => apiCall(`/members/search?keyword=${encodeURIComponent(k)}`),
    create: (member) => apiCall('/members', { method:'POST', body: member }), update: (id, member) => apiCall(`/members/${id}`, { method:'PUT', body: member }), delete: (id) => apiCall(`/members/${id}`, { method:'DELETE' }), quickSearch: (k) => apiCall(`/members/quick-search?keyword=${encodeURIComponent(k)}`)
};

// 入库相关API（需要定义这些API）
const stockInAPI = {
    create: (stockInData) => apiCall('/stock-in/orders', {
        method: 'POST',
        body: JSON.stringify(stockInData)
    }),
    getOrders: () => apiCall('/stock-in/orders'),
    getOrderDetail: (orderId) => apiCall(`/stock-in/orders/${orderId}`)
};

// 供应商相关API
const supplierAPI = { getAll: () => apiCall('/suppliers'), getById: (id) => apiCall(`/suppliers/${id}`), create: (supplier) => apiCall('/suppliers',{method:'POST', body: supplier}), update: (id, supplier) => apiCall(`/suppliers/${id}`,{method:'PUT', body: supplier}) };

// 批次药品相关API（需要定义这些API）
const batchMedicineAPI = {
    // 可以根据需要添加方法
};

// 新增：inventoryAPI 用于库存页面
const inventoryAPI = {
    getAll: () => apiCall('/inventory'),
    getLowStock: () => apiCall('/inventory/low-stock'),
    getExpiringSoon: () => apiCall('/inventory/expiring-soon'),
    getDetail: (id) => apiCall(`/inventory/${id}`),
    getByMedicine: (medicineId) => apiCall(`/inventory/by-medicine/${medicineId}`),
    searchByBatch: async (batchKeyword) => {
        try {
            // 如果后端有接口可直接调用: /inventory/search?batch=xxx
            const direct = await apiCall(`/inventory/search?batch=${encodeURIComponent(batchKeyword)}`);
            if (direct && direct.code === 200) return direct;
        } catch(e){ console.warn('[inventoryAPI.searchByBatch] 后端搜索接口不可用, 使用本地过滤', e.message); }
        // 回退: 全量拉取后端数据再过滤
        const all = await apiCall('/inventory');
        const filtered = (all.data||[]).filter(item => (item.batchNo||'').includes(batchKeyword));
        return { code:200, message:'local-filter', data: filtered };
    }
};

// 新增：员工相关API
const employeesAPI = { getAll: () => apiCall('/employees'), getById: (id) => apiCall(`/employees/${id}`), create: (e) => apiCall('/employees',{method:'POST', body: e}), update: (id,e) => apiCall(`/employees/${id}`,{method:'PUT', body: e}), delete: (id) => apiCall(`/employees/${id}`,{method:'DELETE'}), getByRole: (roleId) => apiCall(`/employees/role/${roleId}`), toggleStatus: async (id,newStatus) => { const emp=await employeesAPI.getById(id); if(!emp||!emp.employeeId) throw new Error('员工不存在'); emp.status=newStatus?1:0; return employeesAPI.update(id,emp); } };

// 新增：系统设置 API
const settingAPI = {
    get: () => apiCall('/settings'),
    update: (data) => apiCall('/settings',{method:'POST',body:JSON.stringify(data)})
};

// 工具函数
const utils = {
    formatPrice: (price) => {
        if (!price && price !== 0) return '¥0.00';
        return '¥' + parseFloat(price).toFixed(2);
    },
    formatDate: (date) => {
        if (!date) return '';
        return new Date(date).toLocaleDateString('zh-CN');
    },
    formatDateTime: (date) => {
        if (!date) return '';
        return new Date(date).toLocaleString('zh-CN');
    }
};

// 显示消息提示
function showMessage(message, type = 'success') {
    const messageDiv = document.createElement('div');
    messageDiv.setAttribute('role','alert');
    messageDiv.setAttribute('aria-live','polite');
    messageDiv.className = `fixed top-4 right-4 px-4 py-2 rounded-lg shadow-lg z-50 ${
        type === 'success' ? 'bg-green-500 text-white' : (type==='error' ? 'bg-red-500 text-white' : 'bg-blue-500 text-white')
    }`;
    messageDiv.innerHTML = `
        <i class="fa ${type === 'success' ? 'fa-check-circle' : (type==='error'?'fa-exclamation-triangle':'fa-info-circle')} mr-2"></i>
        ${message}
    `;
    document.body.appendChild(messageDiv);
    if(typeof window.announce === 'function'){ window.announce(message); }
    setTimeout(() => { messageDiv.remove(); }, 3000);
}

// 确保全局导出（合并已存在的 window.api，避免覆盖）
(function() {
    // 如果已有 window.api，合并而不是覆盖
    window.api = window.api || {};

    // 把本模块的命名空间合并到 window.api
    Object.assign(window.api, {
        BASE_URL,
        apiCall,
        medicineAPI,
        categoryAPI,
        memberAPI,
        stockInAPI,
        supplierAPI,
        orderAPI,
        utils,
        batchMedicineAPI,
        showMessage,
        inventoryAPI,
        employeesAPI,
        settingAPI
    });

    // 兼容旧脚本直接调用 apiCall/BASE_URL
    window.apiCall = window.apiCall || apiCall;
    window.BASE_URL = window.BASE_URL || BASE_URL;

    console.log('=== common.js 加载完成 ===');
    console.log('API 对象已挂载到 window.api');
    try {
        console.log('medicineAPI 方法列表:', Object.keys(window.api.medicineAPI));
    } catch (e) {
        console.warn('medicineAPI 不存在或不可枚举');
    }
})();

// 新增：全局仪表盘刷新函数（若不存在）
if (typeof window.refreshDashboardWidgets !== 'function') {
  window.refreshDashboardWidgets = async function(){
    try {
            const base = window.api?.BASE_URL || '/api';
      const [alertsResp, hotResp] = await Promise.all([
        fetch(base + '/dashboard/stock-alerts').then(r=>r.json()).catch(()=>null),
        fetch(base + '/dashboard/hot-products').then(r=>r.json()).catch(()=>null)
      ]);
      if (typeof updateStockAlerts === 'function' && alertsResp && (alertsResp.data||alertsResp.alerts)) {
        updateStockAlerts(alertsResp.data||alertsResp.alerts);
      }
      if (typeof updateHotProducts === 'function' && hotResp && hotResp.data) {
        updateHotProducts(Array.isArray(hotResp.data)?hotResp.data:hotResp.data.hotProducts||[]);
      }
      console.log('Dashboard widgets refreshed');
    } catch(e){ console.warn('refreshDashboardWidgets failed', e); }
  }
}

})();
