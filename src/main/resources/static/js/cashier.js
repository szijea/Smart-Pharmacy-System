// cashier.js - 收银页面逻辑
// 依赖 common.js 中的 BASE_URL 以及 medicineAPI/memberAPI/orderAPI

(function(){
  const cart = []; // { medicineId, name, spec, price, quantity }
  let selectedMember = null;
  let hangOrders = []; // 简单前端缓存挂单
  let activeRewards = []; // 积分福利

  // Load hangOrders from localStorage
  try {
    const saved = localStorage.getItem('pharmacy_hang_orders');
    if(saved) hangOrders = JSON.parse(saved);
  } catch(e) { console.error('Failed to load hang orders', e); }

  // 加载可用的积分福利
  async function loadActiveRewards() {
    try {
      const res = await fetch('/api/point-rewards/active');
      if(res.ok) {
        activeRewards = await res.json();
      }
    } catch(e) { console.error('Failed to load rewards', e); }
  }
  loadActiveRewards();

  function checkPointRewards(member) {
    if(!member || !activeRewards.length) return;
    const points = member.points || 0;
    const eligibleRewards = activeRewards.filter(r => points >= r.pointsRequired);

    if(eligibleRewards.length > 0) {
      const rewardNames = eligibleRewards.map(r => `${r.name}(${r.pointsRequired}分)`).join(', ');
      alert(`【积分福利提醒】\n会员 ${member.name} 当前积分 ${points}，可兑换以下福利：\n${rewardNames}`);
    }
  }

  // 工具函数
  function formatMoney(v){ return '¥' + (Number(v||0).toFixed(2)); }
  function $(id){ return document.getElementById(id); }
  function show(el){ if(el) el.classList.remove('hidden'); }
  function hide(el){ if(el) el.classList.add('hidden'); }
  function safeInner(el, html){ if(el) el.innerHTML = html; }

  function renderCart(){
    const body = $('cart-body');
    const empty = $('cart-empty');
    if(!body) return;
    if(cart.length === 0){
      safeInner(body,'');
      show(empty);
      updateSummary();
      return;
    }
    hide(empty);
    safeInner(body, cart.map(item => {
      const subtotal = item.price * item.quantity;
      return `<tr data-id="${item.medicineId}">
        <td class="px-4 py-2 whitespace-nowrap">${item.name || item.tradeName || item.genericName || item.medicineId}</td>
        <td class="px-4 py-2 whitespace-nowrap">${item.spec || '-'}</td>
        <td class="px-4 py-2 whitespace-nowrap">${formatMoney(item.price)}</td>
        <td class="px-4 py-2 whitespace-nowrap">
          <div class="flex items-center gap-1">
            <button class="btn btn-outline text-xs px-2 py-1" data-action="dec" data-id="${item.medicineId}">-</button>
            <input type="number" min="1" class="input w-16 text-center text-xs" data-action="qty" data-id="${item.medicineId}" value="${item.quantity}" />
            <button class="btn btn-outline text-xs px-2 py-1" data-action="inc" data-id="${item.medicineId}">+</button>
          </div>
        </td>
        <td class="px-4 py-2 whitespace-nowrap">${formatMoney(subtotal)}</td>
        <td class="px-4 py-2 whitespace-nowrap">
          <button class="btn btn-outline text-xs" data-action="remove" data-id="${item.medicineId}"><i class="fa fa-times"></i></button>
        </td>
      </tr>`;
    }).join(''));
    updateSummary();
  }

  function updateSummary(){
    const original = cart.reduce((sum,i)=> sum + i.price * i.quantity, 0);
    const discountInput = $('discount-amount');
    const discount = Number(discountInput && discountInput.value || 0);
    // 禁用积分抵扣
    const pointsAmount = 0;
    const payable = Math.max(original - discount - pointsAmount, 0);
    safeInner($('summary-original'), formatMoney(original));
    safeInner($('summary-discount'), formatMoney(discount));
    safeInner($('summary-points'), formatMoney(pointsAmount));
    safeInner($('summary-payable'), formatMoney(payable));
  }

  function levelMultiplier(member){
    // 根据会员等级应用倍率；未指定则 1.0
    if(!member) return 1.0;
    const lvl = (member.levelName || member.level || '').toString().toLowerCase();
    // 假设规则：普通=1.0，白银=0.95，黄金=0.90，铂金=0.85
    if(lvl.includes('白银') || lvl.includes('silver')) return 0.95;
    if(lvl.includes('黄金') || lvl.includes('gold')) return 0.90;
    if(lvl.includes('铂金') || lvl.includes('platinum')) return 0.85;
    return 1.0; // 普通或未知
  }

  function addToCart(med){
    if(!med || !med.medicineId) return;
    const useMember = !!selectedMember;
    const baseRetail = Number(med.retailPrice || med.price || med.unitPrice || 0);
    const baseMember = med.memberPrice!=null ? Number(med.memberPrice) : null;
    const mult = levelMultiplier(selectedMember);
    // 优先会员价；无会员价则按等级倍率折算零售价
    const chosenPrice = useMember ? (baseMember!=null ? baseMember : baseRetail * mult) : baseRetail;
    const existing = cart.find(i=> i.medicineId === med.medicineId);
    if(existing){ existing.quantity += 1; } else {
      cart.push({
        medicineId: med.medicineId,
        name: med.genericName || med.tradeName || med.name,
        spec: med.spec,
        price: Number(chosenPrice||0),
        quantity: 1,
        _retailPrice: baseRetail,
        _memberPrice: baseMember,
        usageDosage: med.usageDosage,
        contraindication: med.contraindication
      });
    }
    renderCart();
    showRecentAddedHint(med);
  }

  function showRecentAddedHint(med){
    const hint = $('recent-added-hint');
    if(!hint) return;
    safeInner(hint, `<i class="fa fa-plus-circle text-primary mr-1"></i> 已添加：${med.genericName || med.tradeName || med.name || med.medicineId}`);
    show(hint);
    clearTimeout(hint._timer);
    hint._timer = setTimeout(()=> hide(hint), 3000);
  }

  function bindCartEvents(){
    const body = $('cart-body');
    if(!body) return;
    body.addEventListener('click', e => {
      const btn = e.target.closest('button');
      if(!btn) return;
      const action = btn.getAttribute('data-action');
      const id = btn.getAttribute('data-id');
      if(!action || !id) return;
      const item = cart.find(i=> i.medicineId === id);
      if(!item) return;
      if(action === 'remove'){ cart.splice(cart.indexOf(item),1); renderCart(); }
      if(action === 'inc'){ item.quantity += 1; renderCart(); }
      if(action === 'dec'){ item.quantity = Math.max(1, item.quantity - 1); renderCart(); }
    });
    body.addEventListener('input', e => {
      const input = e.target;
      if(input && input.getAttribute('data-action') === 'qty'){
        const id = input.getAttribute('data-id');
        const item = cart.find(i=> i.medicineId === id);
        if(item){ item.quantity = Math.max(1, Number(input.value || 1)); renderCart(); }
      }
    });
  }

  // 药品搜索
  async function searchMedicines(){
    const keyword = $('medicine-search-input').value.trim();
    const category = $('medicine-category-filter').value.trim();
    const resultBox = $('medicine-search-result');
    if(!resultBox) return;
    resultBox.classList.remove('hidden');
    safeInner(resultBox, '<div class="p-3 text-center text-gray-400"><i class="fa fa-spinner fa-spin"></i> 搜索中...</div>');
    try {
      // 使用包含库存的搜索接口，便于收银页展示库存相关信息
      const res = await medicineAPI.searchWithStock(keyword, category, 1, 30);
      const list = res && res.data ? res.data : [];
      if(list.length === 0){
        safeInner(resultBox,'<div class="p-3 text-center text-gray-400">未找到匹配药品</div>');
        return;
      }
      safeInner(resultBox, list.map(m => `<div class="px-3 py-2 hover:bg-blue-50 cursor-pointer text-sm" data-id="${m.medicineId}">
        <div class="flex justify-between"><span>${m.genericName || m.tradeName || m.name}</span><span class="text-gray-500">${formatMoney(m.retailPrice || m.price)}</span></div>
        <div class="text-xs text-gray-400">${m.spec || ''} ${m.manufacturer || ''}${typeof m.stockQuantity!== 'undefined' ? ` · 库存:${m.stockQuantity ?? 0}` : ''}</div>
      </div>`).join(''));
    } catch(err){
      safeInner(resultBox, `<div class='p-3 text-center text-red-500 text-xs'>搜索失败: ${err.message}</div>`);
    }
  }

  function bindMedicineSearchEvents(){
    const resultBox = $('medicine-search-result');
    if(resultBox){
      resultBox.addEventListener('click', e => {
        const itemDiv = e.target.closest('[data-id]');
        if(!itemDiv) return;
        const id = itemDiv.getAttribute('data-id');
        // 简单再次查找单个药品详情
        medicineAPI.getById(id).then(m => { addToCart(m); }).catch(()=>{});
      });
    }
    $('medicine-search-btn').addEventListener('click', searchMedicines);
    $('medicine-search-input').addEventListener('keydown', e => { if(e.key==='Enter'){ e.preventDefault(); searchMedicines(); }});
  }

  // 会员搜索
  async function searchMembers(){
    const keyword = $('member-search-input').value.trim();
    const resultBox = $('member-search-result');
    if(!resultBox) return;
    if(!keyword){ hide(resultBox); return; }
    show(resultBox);
    safeInner(resultBox,'<div class="p-3 text-center text-gray-400"><i class="fa fa-spinner fa-spin"></i> 搜索中...</div>');
    try {
      const res = await memberAPI.search(keyword);
      const list = res && res.data ? res.data : [];
      if(list.length === 0){ safeInner(resultBox,'<div class="p-3 text-center text-gray-400">无匹配会员</div>'); return; }
      safeInner(resultBox, list.map(m => `<div class="px-3 py-2 hover:bg-blue-50 cursor-pointer text-sm" data-id="${m.memberId}">
        <div class="flex justify-between"><span>${m.name || '会员'}</span><span class="text-gray-500">积分:${m.points||0}</span></div>
        <div class="text-xs text-gray-400">${m.phone || ''} 等级:${m.levelName || ''}</div>
      </div>`).join(''));
    } catch(err){
      safeInner(resultBox, `<div class='p-3 text-center text-red-500 text-xs'>会员搜索失败: ${err.message}</div>`);
    }
  }

  function updateMemberUI(member){
    const infoBox = $('selected-member-info');
    if(!infoBox) return;
    show(infoBox);
    safeInner(infoBox, `
      <div class="font-bold">${member.name} <span class="text-xs font-normal text-gray-500">(${member.levelName||'会员'})</span></div>
      <div class="text-xs text-gray-500">积分: ${member.points||0} | 余额: ${formatMoney(member.balance)}</div>
    `);
    checkPointRewards(member);
  }

  function bindMemberSearchEvents(){
    const resultBox = $('member-search-result');
    if(resultBox){
      resultBox.addEventListener('click', e => {
        const div = e.target.closest('[data-id]');
        if(!div) return;
        const memberId = div.getAttribute('data-id');
        memberAPI.getById(memberId).then(m => {
          selectedMember = m;
          hide(resultBox);

          updateMemberUI(m);

          const clearBtn = $('clear-member-btn');
          if(clearBtn){ clearBtn.classList.remove('hidden'); }
          // 选中会员后，按规则重定价购物车：优先 memberPrice，否则零售价*等级倍率
          const mult = levelMultiplier(selectedMember);
          cart.forEach(it=>{
            if(it._memberPrice!=null){ it.price = Number(it._memberPrice); }
            else { it.price = Number((it._retailPrice||0) * mult); }
          });
          renderCart();
        }).catch(()=>{});
      });
    }
    $('member-search-input').addEventListener('input', () => { searchMembers(); });
    $('clear-member-btn').addEventListener('click', () => {
      selectedMember = null;
      hide($('selected-member-info'));
      hide($('clear-member-btn'));
      // 清除会员后恢复零售价
      cart.forEach(it=>{ it.price = Number(it._retailPrice||it.price||0); });
      renderCart();
    });
  }

  // 提交订单
  async function submitOrder(){
    if(cart.length === 0){ feedback('购物车为空，无法提交', 'error'); return; }
    const orderData = {
      customerName: selectedMember ? selectedMember.name : '散客',
      memberId: selectedMember ? selectedMember.memberId : null,
      paymentMethod: $('payment-method').value,
      originalAmount: cart.reduce((s,i)=> s + i.price * i.quantity, 0),
      discountAmount: Number($('discount-amount').value || 0),
      totalAmount: cart.reduce((s,i)=> s + i.price * i.quantity, 0),
      items: cart.map(i => ({ productId: i.medicineId, quantity: i.quantity, unitPrice: Number(i.price.toFixed? i.price.toFixed(2): i.price) }))
    };
    feedback('<i class="fa fa-spinner fa-spin"></i> 正在提交订单...', 'info');
    try {
      const res = await orderAPI.create(orderData);
      feedback(`<i class='fa fa-check-circle text-green-600'></i> 订单创建成功：${res.orderId || (res.data && res.data.orderNumber)||''}`, 'success');
      // 清空购物车
      cart.length = 0; renderCart();
    } catch(err){
      feedback(`<i class='fa fa-times-circle text-red-600'></i> 提交失败: ${err.message}`, 'error');
    }
  }

  function feedback(msg, type){
    const box = $('order-feedback');
    if(!box) return;
    box.className = 'mt-4 text-sm';
    if(type==='error') box.classList.add('text-red-600');
    if(type==='success') box.classList.add('text-green-600');
    if(type==='info') box.classList.add('text-gray-600');
    safeInner(box, msg);
  }

  function saveHangOrders() {
      // No-op for backend persistence, handled by API calls
  }

  // 加载挂单列表
  async function loadHangOrders() {
    try {
      const res = await fetch('/api/hang-orders');
      if(res.ok) {
        const json = await res.json();
        if (Array.isArray(json)) {
            hangOrders = json;
        } else if (json.data && Array.isArray(json.data)) {
            hangOrders = json.data;
        } else {
            hangOrders = [];
        }
        renderHangOrders();
      }
    } catch(e) { console.error('Failed to load hang orders', e); }
  }

  // 挂单（后端持久化）
  async function hangCurrentOrder(){
    if(cart.length === 0){ feedback('当前没有可挂起的购物车', 'error'); return; }

    const hangData = {
        cart: cart,
        memberId: selectedMember ? selectedMember.memberId : null,
        memberName: selectedMember ? selectedMember.name : null
    };

    try {
        const res = await fetch('/api/hang-orders', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(hangData)
        });
        if(res.ok) {
            const json = await res.json();
            feedback(`<i class='fa fa-pause-circle text-blue-600'></i> 已挂单：${json.hangId}`, 'info');
            loadHangOrders();
            cart.length = 0; renderCart();
        } else {
            throw new Error('挂单失败');
        }
    } catch(e) {
        feedback('挂单失败: ' + e.message, 'error');
    }
  }

  function renderHangOrders(){
    const container = $('hang-orders-container');
    const empty = $('hang-orders-empty');
    if(!container) return;
    if(hangOrders.length === 0){ show(empty); safeInner(container,''); return; }
    hide(empty);
    safeInner(container, hangOrders.map(h => `<div class='border rounded p-2 flex justify-between items-center'>
      <div class='text-xs'>挂单号: <span class='font-mono'>${h.hangId}</span><br><span class='text-gray-500'>${h.cart.length} 件商品</span></div>
      <div class='flex gap-2'>
        <button class='btn btn-outline text-xs' data-action='restore' data-id='${h.hangId}'>恢复</button>
        <button class='btn btn-outline text-xs' data-action='discard' data-id='${h.hangId}'><i class='fa fa-trash'></i></button>
      </div>
    </div>`).join(''));
  }

  function bindHangEvents(){
    const container = $('hang-orders-container');
    if(container){
      container.addEventListener('click', async e => {
        const btn = e.target.closest('button');
        if(!btn) return;
        const action = btn.getAttribute('data-action');
        const id = btn.getAttribute('data-id');
        const hang = hangOrders.find(h => h.hangId === id);
        if(!hang) return;
        if(action === 'restore'){
          cart.length = 0;
          hang.cart.forEach(item => cart.push(item));
          renderCart();

          // Delete from backend after restore
          try {
              await apiCall(`/hang-orders/${id}`, { method: 'DELETE' });
              loadHangOrders();
              feedback(`<i class='fa fa-undo text-primary'></i> 已恢复挂单 ${id}`, 'success');
          } catch(e) { console.error(e); }
        }
        if(action === 'discard'){
          try {
              await apiCall(`/hang-orders/${id}`, { method: 'DELETE' });
              loadHangOrders();
              feedback(`<i class='fa fa-trash text-gray-600'></i> 已删除挂单 ${id}`, 'info');
          } catch(e) { console.error(e); }
        }
      });
    }
    $('hang-order-btn').addEventListener('click', hangCurrentOrder);
    $('refresh-hang-btn').addEventListener('click', loadHangOrders);
  }

  function initDiscountAndPoints(){
    // 只监听折扣，禁用积分输入
    const el = $('discount-amount'); if(el){ el.addEventListener('input', updateSummary); }
    const pts = $('use-points'); if(pts){ pts.value=''; pts.setAttribute('disabled','disabled'); }
  }

  function printPreview(){
    if(cart.length === 0){ feedback('购物车为空，无法预览', 'error'); return; }

    const tenant = localStorage.getItem('selectedTenant') || localStorage.getItem('tenant') || 'wx';
    let shopName = '智慧药房';
    if(tenant === 'bht') shopName = '百和堂药店';
    else if(tenant === 'rzt') shopName = '仁智堂药店';
    else if(tenant === 'wx') shopName = '万欣药店';

    const dateStr = new Date().toLocaleString();

    // 收集医嘱和忌口
    let adviceList = [];
    let contraList = [];

    const itemsHtml = cart.map(i => {
      let extra = '';
      if(i.usageDosage) {
          extra += `<div><small>用法: ${i.usageDosage}</small></div>`;
          adviceList.push({name: i.name, content: i.usageDosage});
      }
      if(i.contraindication) {
          // 在单项中也显示，或者只在底部显示？这里保留单项显示
          extra += `<div><small>忌口: ${i.contraindication}</small></div>`;
          contraList.push({name: i.name, content: i.contraindication});
      }
      return `<tr>
        <td style="padding-bottom: 5px;">
            <div>${i.name} <span style="float:right;">x${i.quantity} &nbsp; ${formatMoney(i.price * i.quantity)}</span></div>
            ${extra}
        </td>
      </tr>`;
    }).join('');

    const total = cart.reduce((s,i)=> s + i.price * i.quantity, 0);
    const discount = Number($('discount-amount').value || 0);
    const payable = Math.max(total - discount, 0);

    // 构建医嘱/忌口汇总 HTML
    let adviceHtml = '';
    if(adviceList.length > 0 || contraList.length > 0) {
        adviceHtml += '<div style="border-top: 1px dashed #000; margin-top: 10px; padding-top: 5px;">';
        adviceHtml += '<div style="font-weight: bold; margin-bottom: 5px; text-align: center;">--- 温馨提示 ---</div>';

        if(adviceList.length > 0) {
             adviceHtml += '<div style="margin-top: 5px;"><strong>[用法用量]</strong></div>';
             adviceList.forEach(item => {
                 adviceHtml += `<div style="font-size: 12px; padding-left: 10px;">- ${item.name}: ${item.content}</div>`;
             });
        }

        if(contraList.length > 0) {
             adviceHtml += '<div style="margin-top: 5px;"><strong>[饮食禁忌]</strong></div>';
             contraList.forEach(item => {
                 adviceHtml += `<div style="font-size: 12px; padding-left: 10px;">- ${item.name}: ${item.content}</div>`;
             });
        }
        adviceHtml += '</div>';
    }

    const ticketHtml = `
      <html>
      <head>
        <title>小票预览</title>
        <style>
          body { font-family: 'Courier New', monospace; width: 300px; margin: 0 auto; padding: 20px; }
          .header { text-align: center; margin-bottom: 20px; border-bottom: 1px dashed #000; padding-bottom: 10px; }
          table { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
          th, td { text-align: left; font-size: 12px; }
          td:last-child { text-align: right; }
          .total { border-top: 1px dashed #000; padding-top: 10px; text-align: right; }
          .footer { margin-top: 20px; text-align: center; font-size: 12px; }
        </style>
      </head>
      <body>
        <div class="header">
          <h3>${shopName}</h3>
          <p>${dateStr}</p>
        </div>
        <table>
          <thead><tr><th style="border-bottom:1px solid #eee;padding-bottom:5px;">商品详情</th></tr></thead>
          <tbody>${itemsHtml}</tbody>
        </table>
        <div class="total">
          <p>总计: ${formatMoney(total)}</p>
          <p>折扣: ${formatMoney(discount)}</p>
          <h3>应付: ${formatMoney(payable)}</h3>
        </div>

        ${adviceHtml}

        <div class="footer">
          <p>谢谢惠顾！</p>
        </div>
      </body>
      </html>
    `;

    const win = window.open('', '_blank', 'width=400,height=600');
    if(win){
        win.document.write(ticketHtml);
        win.document.close();
        // 延迟打印，确保样式加载
        setTimeout(() => {
            win.focus();
            win.print();
        }, 500);
    } else {
        feedback('请允许弹出窗口以进行打印预览', 'error');
    }
  }

  function initSubmit(){
    $('submit-order-btn').addEventListener('click', submitOrder);
    $('quick-pay-btn').addEventListener('click', submitOrder);
    const printBtn = $('print-preview-btn');
    if(printBtn) printBtn.addEventListener('click', printPreview);
  }

  document.addEventListener('DOMContentLoaded', () => {
    bindCartEvents();
    bindMedicineSearchEvents();
    bindMemberSearchEvents();
    bindHangEvents();
    initDiscountAndPoints();
    initSubmit();
    renderCart();
    loadHangOrders();
  });
})();
