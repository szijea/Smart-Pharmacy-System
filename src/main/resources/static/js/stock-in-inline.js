;(function(){
  const BASE = (window.api && window.api.BASE_URL) || (window.location.origin + '/api');
  // 兼容：如果没选租户，退回到 'wx'（后端默认租户/初始化脚本常用）
  const tenant = localStorage.getItem('selectedTenant') || localStorage.getItem('tenant') || 'wx';
  const state = { items: [], submitting: false };
  function toCurrency(n){ n = Number(n||0); return '¥' + n.toFixed(2); }
  function qs(id){ return document.getElementById(id); }
  function showToast(msg){
    try{
      if(window.api && typeof window.api.showMessage === 'function') return window.api.showMessage(msg, 'success');
      if(typeof window.showMessage === 'function') return window.showMessage(msg, 'success');
    }catch(e){}

    let toast = document.getElementById('toast-message');
    if(!toast) {
        toast = document.createElement('div');
        toast.id = 'toast-message';
        toast.style.cssText = 'position:fixed;top:20px;right:20px;background:rgba(0,0,0,0.8);color:#fff;padding:10px 20px;border-radius:4px;z-index:9999;transition:opacity 0.3s;';
        document.body.appendChild(toast);
    }
    toast.textContent = msg;
    toast.style.opacity = '1';
    toast.style.display = 'block';
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(()=>toast.style.display='none', 300); }, 3000);
    console.log('[stock-in]', msg);
  }
  function calcKpis(){
    const itemCount = state.items.length;
    const totalCost = state.items.reduce((s,i)=> s + (Number(i.unitPrice||0) * Number(i.quantity||0)), 0);
    const totalPrice = state.items.reduce((s,i)=> s + (Number(i.retailPrice||0) * Number(i.quantity||0)), 0);
    const totalProfit = totalPrice - totalCost;
    qs('kpi-item-count').textContent = itemCount;
    qs('kpi-total-cost').textContent = toCurrency(totalCost);
    qs('kpi-total-price').textContent = toCurrency(totalPrice);
    qs('kpi-total-profit').textContent = toCurrency(totalProfit);
  }
  function renderTable(){
    const tbody = qs('medicine-table-body');
    if(!tbody) return;
    if(state.items.length === 0){
      tbody.innerHTML = '<tr><td colspan="11" class="text-center py-8 text-gray-500"><i class="fa fa-inbox text-3xl mb-2"></i><p>暂无入库药品数据</p></td></tr>';
      qs('item-count').textContent = 0;
      qs('total-quantity').textContent = 0;
      qs('total-cost').textContent = '¥0.00';
      qs('total-price').textContent = '¥0.00';
      qs('total-profit').textContent = '¥0.00';
      calcKpis();
      return;
    }
    let rows = '';
    let seq = 1, totalQty=0, totalCost=0, totalPrice=0;
    state.items.forEach(i=>{
      const profit = Number((i.retailPrice||0)) - Number((i.unitPrice||0));
      totalQty += Number(i.quantity||0);
      totalCost += Number(i.unitPrice||0) * Number(i.quantity||0);
      totalPrice += Number((i.retailPrice||0)) * Number(i.quantity||0);
      const removeKey = (i.internalId!=null? String(i.internalId) : String(i.medicineId||''));
      rows += '<tr>'+
        '<td class="px-6 py-3">'+(seq++)+'</td>'+
        '<td class="px-6 py-3">'+(i.medicineName||i.medicineId)+'</td>'+
        '<td class="px-6 py-3">'+(i.spec||'-')+'</td>'+
        '<td class="px-6 py-3">'+(i.manufacturer||'-')+'</td>'+
        '<td class="px-6 py-3">'+(i.batchNumber||'-')+'</td>'+
        '<td class="px-6 py-3">'+(i.expiryDate||'-')+'</td>'+
        '<td class="px-6 py-3">'+(i.quantity||0)+'</td>'+
        '<td class="px-6 py-3">'+toCurrency(i.unitPrice||0)+'</td>'+
        '<td class="px-6 py-3">'+toCurrency(i.retailPrice||0) + (i.memberPrice!=null? (' / 会员价 '+toCurrency(i.memberPrice)): '') +'</td>'+
        '<td class="px-6 py-3">'+toCurrency(profit*(i.quantity||0))+'</td>'+
        '<td class="px-6 py-3"><button class="btn btn-outline btn-sm" data-remove="'+removeKey+'">移除</button></td>'+
      '</tr>';
    });
    tbody.innerHTML = rows;
    qs('item-count').textContent = String(state.items.length);
    qs('total-quantity').textContent = String(totalQty);
    qs('total-cost').textContent = toCurrency(totalCost);
    qs('total-price').textContent = toCurrency(totalPrice);
    qs('total-profit').textContent = toCurrency(totalPrice-totalCost);
    calcKpis();
    tbody.querySelectorAll('button[data-remove]').forEach(btn=>{
      btn.addEventListener('click', function(){ const id=this.getAttribute('data-remove'); state.items = state.items.filter(x=> String(x.internalId||x.medicineId||'') !== String(id)); renderTable(); });
    });
  }
  async function searchMedicine(keyword){
    const trySearch = async (page)=>{
      const url = BASE + '/medicines/search?keyword=' + encodeURIComponent(keyword) + '&page=' + page + '&size=10';
      const r = await fetch(url,{ headers:{ 'X-Shop-Id': tenant } });
      if(!r.ok) return { ok:false, status:r.status };
      const d = await r.json().catch(()=>({data:[] }));
      const list = Array.isArray(d.data)? d.data: (Array.isArray(d)? d: []);
      return { ok:true, data:list };
    };
    let res = await trySearch(1);
    if(!res.ok){
      console.warn('[searchMedicine] page=1 failed status=', res.status, 'retrying page=0');
      res = await trySearch(0);
    }
    if(!res.ok) {
      console.error('[searchMedicine] search failed status=', res.status);
      return [];
    }
    return res.data || [];
  }
  async function createMedicine(payload){
    const r = await fetch(BASE + '/medicines', {
      method:'POST',
      headers:{ 'Content-Type':'application/json', 'Accept':'application/json', 'X-Shop-Id': tenant },
      body: JSON.stringify(payload)
    });
    const txt = await r.text();
    let data;
    try{ data = JSON.parse(txt); }catch(e){ data = txt; }
    if(!r.ok){
      const msg = (typeof data === 'object' && (data.message || data.error))? (data.message||data.error) : txt;
      throw new Error(typeof msg === 'string' ? msg : JSON.stringify(msg));
    }
    if(data && data.data) return data.data;
    return data;
  }
  async function openAddMedicine(){
    const genericName = prompt('通用名'); if(!genericName) return;
    const tradeName = prompt('商品名(可选)', '');
    const spec = prompt('规格');
    const manufacturer = prompt('生产厂家');
    const unit = prompt('单位(盒/瓶/粒等)', '盒');
    const isRxStr = prompt('是否处方药(是/否)', '否');
    const isRx = /^是|yes|true$/i.test(isRxStr||'');
    const approvalNo = prompt('批准文号(必填)', '');
    if(!approvalNo || !approvalNo.trim()){ alert('批准文号不能为空'); return; }
    const barcode = prompt('条形码(可选，建议填写以支持扫码搜索)', '') || '';
    const retailPriceStr = prompt('零售价(数字)', '0');
    const retailPrice = Number(retailPriceStr||0);
    const memberPriceStr = prompt('会员价(数字，可选)', '');
    const memberPrice = memberPriceStr !== '' ? Number(memberPriceStr) : null;
    const categoryIdStr = prompt('分类ID(数字，可选)', '1');
    const categoryId = Number(categoryIdStr||1);
    try{
      const med = await createMedicine({
        genericName, tradeName, spec, manufacturer, unit,
        isRx, approvalNo, retailPrice, memberPrice, categoryId,
        barcode: barcode || undefined,
        status:'ACTIVE', deleted:false
      });
      let normalized = (med && med.data)? med.data : med;
      const medId = normalized.medicineId || String(normalized.id || ('M'+Date.now()));
      const internalId = (normalized.medicineId!=null? normalized.medicineId : (normalized.id!=null? String(normalized.id) : null));
      try {
        await window.api.medicineAPI.update(medId, {
          genericName, tradeName, spec, manufacturer, unit,
          isRx, approvalNo, retailPrice, memberPrice, categoryId,
          barcode: barcode || undefined,
          status:'ACTIVE'
        });
        normalized = await window.api.medicineAPI.getById(medId).catch(()=>normalized);
        if(normalized && normalized.data) normalized = normalized.data;
      } catch (e) { console.warn('[stock-in] 更新药品主数据失败(忽略并继续)', e); }
      const batch = prompt('批号(如 B20251203001)', 'B'+new Date().toISOString().slice(0,10).replace(/-/g,'')+'001');
      const productionDate = prompt('生产日期(YYYY-MM-DD)', '');
      const expiry = prompt('到期日期(YYYY-MM-DD)', '');
      const qty = Number(prompt('入库数量', '10')||0);
      const unitPrice = Number(prompt('进货价(单价)', String(retailPrice||0))||0);
      state.items.push({
        internalId: internalId,
        medicineId: medId,
        medicineName: normalized.genericName||genericName,
        spec: normalized.spec||spec,
        manufacturer: normalized.manufacturer||manufacturer,
        unit: normalized.unit||unit,
        approvalNo: normalized.approvalNo||approvalNo,
        barcode: normalized.barcode || barcode || '',
        batchNumber: batch,
        productionDate: productionDate,
        expiryDate: expiry,
        quantity: qty,
        unitPrice: unitPrice,
        retailPrice: Number((normalized.retailPrice!=null? normalized.retailPrice : retailPrice)||0),
        memberPrice: (normalized.memberPrice!=null? Number(normalized.memberPrice) : (memberPrice!=null? memberPrice: undefined))
      });
      renderTable();
    }catch(err){ alert('新建药品失败: '+err.message); }
  }
  async function openQuickAdd(){
    const name = prompt('输入药品关键词（名称/厂家/规格）');
    if(!name) return;
    searchMedicine(name).then(list=>{
      if(!list.length){ alert('未找到药品: ' + name); return; }
      const med = list[0];
      const qty = Number(prompt('入库数量', '10')||0);
      const suggestedUnit = (med.memberPrice!=null? med.memberPrice : (med.retailPrice||0));
      const unitPrice = Number(prompt('进货价(单价)', String(suggestedUnit||0))||0);
      const batch = prompt('批号(如 B20251203001)', 'B'+new Date().toISOString().slice(0,10).replace(/-/g,'')+'001');
      const expiry = prompt('有效期(YYYY-MM-DD)', (med.expiryDate||'').toString().slice(0,10));
      state.items.push({
        internalId: med.id,
        medicineId: med.medicineId || String(med.id||''),
        medicineName: med.genericName||med.tradeName||med.medicineId,
        spec: med.spec,
        manufacturer: med.manufacturer,
        batchNumber: batch,
        expiryDate: expiry,
        quantity: qty,
        unitPrice: unitPrice,
        retailPrice: Number(med.retailPrice||0),
        memberPrice: (med.memberPrice!=null? Number(med.memberPrice) : undefined)
      });
      renderTable();
    });
  }
  function openMedDrawer(){
    const drawer = qs('add-med-drawer');
    const backdrop = document.getElementById('drawer-backdrop');
    if(!drawer) return;
    drawer.style.transform = 'translateX(0)';
    if(backdrop) backdrop.style.display = 'block';
    const fields = ['fm-genericName','fm-tradeName','fm-spec','fm-manufacturer','fm-unit','fm-isRx','fm-approvalNo','fm-barcode','fm-retailPrice','fm-memberPrice','fm-categoryId','fm-batch','fm-production','fm-expiry','fm-qty','fm-unitPrice','fm-usageDosage','fm-contraindication'];
    fields.forEach(id=>{ const el = qs(id); if(el) el.value = ''; });
    if(qs('fm-unit')) qs('fm-unit').value = '盒';
    if(qs('fm-isRx')) qs('fm-isRx').value = 'false';
    if(qs('fm-categoryId')) qs('fm-categoryId').value = '1';
    if(qs('fm-qty')) qs('fm-qty').value = '10';
    if(qs('fm-retailPrice')) qs('fm-retailPrice').value = '0';
  }
  function closeMedDrawer(){
    const drawer = qs('add-med-drawer');
    const backdrop = document.getElementById('drawer-backdrop');
    if(drawer) drawer.style.transform = 'translateX(100%)';
    if(backdrop) backdrop.style.display = 'none';
  }
  async function submitMedForm(){
    try{
      const genericName = (qs('fm-genericName') && qs('fm-genericName').value) || '';
      const tradeName = (qs('fm-tradeName') && qs('fm-tradeName').value) || '';
      const spec = (qs('fm-spec') && qs('fm-spec').value) || '';
      const manufacturer = (qs('fm-manufacturer') && qs('fm-manufacturer').value) || '';
      const unit = (qs('fm-unit') && qs('fm-unit').value) || '盒';
      const isRx = (qs('fm-isRx') && qs('fm-isRx').value)==='true';
      const approvalNo = (qs('fm-approvalNo') && qs('fm-approvalNo').value) || '';
      const barcode = (qs('fm-barcode') && qs('fm-barcode').value) || '';
      const retailPrice = Number((qs('fm-retailPrice') && qs('fm-retailPrice').value) || 0);
      const memberPriceVal = qs('fm-memberPrice') && qs('fm-memberPrice').value;
      const memberPrice = (memberPriceVal!==undefined && memberPriceVal!=='')? Number(memberPriceVal) : null;
      const categoryId = Number((qs('fm-categoryId') && qs('fm-categoryId').value) || 1);
      const usageDosage = (qs('fm-usageDosage') && qs('fm-usageDosage').value) || '';
      const contraindication = (qs('fm-contraindication') && qs('fm-contraindication').value) || '';

      if(!genericName || !approvalNo){ alert('通用名与批准文号为必填项'); return; }
      const med = await createMedicine({
        genericName, tradeName, spec, manufacturer, unit, isRx, approvalNo, retailPrice, memberPrice, categoryId,
        barcode: barcode||undefined, status:'ACTIVE', deleted:false,
        usageDosage, contraindication
      });
      const normalized = (med && med.data)? med.data : med;
      const medId = normalized.medicineId || String(normalized.id || ('M'+Date.now()));
      const batch = (qs('fm-batch') && qs('fm-batch').value) || ('B'+new Date().toISOString().slice(0,10).replace(/-/g,'')+'001');
      const productionDate = (qs('fm-production') && qs('fm-production').value) || new Date().toISOString().slice(0,10);
      const expiry = (qs('fm-expiry') && qs('fm-expiry').value) || '';
      const qty = Number((qs('fm-qty') && qs('fm-qty').value) || 1);
      const unitPrice = Number((qs('fm-unitPrice') && qs('fm-unitPrice').value) || retailPrice || 0);
      state.items.push({
        internalId: normalized.medicineId || normalized.id || medId,
        medicineId: medId,
        medicineName: normalized.genericName || genericName,
        spec: normalized.spec || spec,
        manufacturer: normalized.manufacturer || manufacturer,
        batchNumber: batch,
        productionDate: productionDate,
        expiryDate: expiry,
        quantity: qty,
        unitPrice: unitPrice,
        retailPrice: Number((normalized.retailPrice!=null? normalized.retailPrice : retailPrice)||0),
        memberPrice: (normalized.memberPrice!=null? Number(normalized.memberPrice) : memberPrice)
      });
      renderTable();
      closeMedDrawer();
    }catch(err){ console.error('[submitMedForm] error', err); alert('保存药品失败: '+(err.message||err)); }
  }
  function newStock(){
    state.items = [];
    if(qs('stock-date')) qs('stock-date').value = new Date().toISOString().slice(0,10);
    if(qs('remark-input')) qs('remark-input').value = '';

    const tenant = localStorage.getItem('selectedTenant') || localStorage.getItem('tenant') || 'wx';
    let shopName = '智慧药房';
    if(tenant === 'bht') shopName = '百和堂药店';
    else if(tenant === 'rzt') shopName = '仁智堂药店';
    else if(tenant === 'wx') shopName = '万欣药店';

    if(qs('org-name')) qs('org-name').value = shopName;

    renderTable();
  }
  function setImportProgress(percent, text){
    const bar = document.getElementById('import-progress-bar');
    const box = document.getElementById('import-progress');
    const txt = document.getElementById('import-progress-text');
    if(box){ box.style.display = 'block'; box.classList.remove('hidden'); }
    if(bar){ bar.style.width = Math.min(Math.max(percent,0),100) + '%'; }
    if(txt && text){ txt.textContent = text; }
  }
  function hideImportProgress(){
    const box = document.getElementById('import-progress');
    if(box){ box.style.display = 'none'; }
  }

  function parseCsvToRows(text){
    if(!text) return [];
    return text.split(/\r?\n/).map(line => line.split(','));
  }

  function processRowsArray(rows, filename){
    if(!rows || !rows.length) return;
    let start = 0;
    // 简单判断首行是否标题
    if(rows[0] && rows[0].some(c => String(c).includes('名称') || String(c).includes('Name'))) start = 1;

    let count = 0;
    for(let i=start; i<rows.length; i++){
      const row = rows[i];
      if(!row || row.length < 1) continue;
      // 默认顺序：名称(0), 规格(1), 厂家(2), 数量(3), 进价(4), 售价(5), 批号(6), 有效期(7), 批准文号(8)
      const name = row[0];
      if(!name) continue;

      const spec = row[1] || '';
      const manufacturer = row[2] || '';
      const qty = Number(row[3] || 10);
      const unitPrice = Number(row[4] || 0);
      const retailPrice = Number(row[5] || 0);
      const batch = row[6] || ('B'+new Date().toISOString().slice(0,10).replace(/-/g,'')+'001');
      const expiry = row[7] || '';
      const approvalNo = row[8] || ('TMP'+Date.now()+Math.floor(Math.random()*1000));

      state.items.push({
        internalId: 'IMP'+Date.now()+i,
        medicineId: 'IMP'+Date.now()+i,
        medicineName: name,
        spec: spec,
        manufacturer: manufacturer,
        batchNumber: batch,
        expiryDate: expiry,
        quantity: qty,
        unitPrice: unitPrice,
        retailPrice: retailPrice,
        approvalNo: approvalNo
      });
      count++;
    }
    showToast('已导入 ' + count + ' 条数据');
    renderTable();
  }

  // wrap heavy steps with progress updates
  async function handleBulkFileChange(e){
    // Safety check for XLSX removed to allow CSV fallback in inner logic
    const file = e.target.files[0];
    try{
      setImportProgress(5, '读取文件...');
      if(typeof XLSX === 'undefined'){
        const fcheck = (e && e.target && e.target.files && e.target.files[0]) || e;
        const fname = fcheck && fcheck.name ? fcheck.name.toLowerCase() : '';
        const isCsvLike = fname.endsWith('.csv') || fname.endsWith('.txt');
        if(!isCsvLike){
          const msg = '解析表格失败: XLSX 库未加载。\n请把 xlsx.full.min.js 下载到项目静态目录：src/main/resources/static/js/xlsx.full.min.js，\n下载地址：https://cdn.jsdelivr.net/npm/xlsx/dist/xlsx.full.min.js ，或将文件另存为 CSV 后重试。';
          alert(msg);
          console.error('[bulk-import] missing XLSX library and file is not CSV-like');
          hideImportProgress();
          return;
        }
        const text = await fcheck.text();
        setImportProgress(25, '解析 CSV...');
        const rows = parseCsvToRows(text);
        if(!rows || rows.length === 0){ alert('未读取到有效数据，请检查 CSV 文件'); hideImportProgress(); return; }
        setImportProgress(60, '映射数据...');
        processRowsArray(rows, fcheck.name || 'csv');
        setImportProgress(100, '完成');
        setTimeout(hideImportProgress, 500);
        return;
      }
      const f = (e && e.target && e.target.files && e.target.files[0]) || e;
      if(!f){ hideImportProgress(); return; }
      showToast('[bulk-import] 读取文件: ' + f.name);
      setImportProgress(15, '读取文件...');
      const arrayBuffer = await f.arrayBuffer();
      setImportProgress(35, '解析工作表...');
      const workbook = XLSX.read(arrayBuffer, { type: 'array' });
      const firstSheetName = workbook.SheetNames && workbook.SheetNames[0];
      if(!firstSheetName){ alert('未读取到工作表，请检查文件'); hideImportProgress(); return; }
      const sheet = workbook.Sheets[firstSheetName];
      const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, raw: false });
      if(!rows || rows.length === 0){ alert('未读取到有效数据，请检查表格格式'); hideImportProgress(); return; }
      setImportProgress(70, '映射数据...');
      processRowsArray(rows, f.name);
      setImportProgress(100, '完成');
      setTimeout(hideImportProgress, 500);
    }catch(err){ console.error('[bulk-import] parse error', err); alert('解析表格失败: '+(err && err.message)); hideImportProgress(); }
  }
  async function submitStockIn(){
    if(state.submitting) return;
    if(!state.items.length){ alert('请先添加入库药品'); return; }
    state.submitting = true;
    try{
      setImportProgress(10, '创建药品...');
      const realItems = await ensureRealMedicineIds(state.items);
      setImportProgress(60, '提交入库单...');
      const payload = {
        stockInNo: undefined,
        stockInDate: (qs('stock-date') && qs('stock-date').value)? (qs('stock-date').value + 'T00:00:00') : undefined,
        remark: (qs('remark-input') && qs('remark-input').value) || '',
        supplier: { supplierId: 1 },
        items: realItems.map(i => ({
          medicineId: i.medicineId,
          batchNumber: i.batchNumber || 'DEFAULT_BATCH',
          productionDate: i.productionDate || null,
          expiryDate: i.expiryDate || null,
          quantity: Number(i.quantity||0),
          unitPrice: Number(i.unitPrice||0)
        }))
      };
      const resp = await fetch(BASE + '/stock-ins', {
        method:'POST',
        headers:{ 'Content-Type':'application/json', 'Accept':'application/json', 'X-Shop-Id': tenant },
        body: JSON.stringify(payload)
      });
      const txt = await resp.text();
      let data; try{ data = JSON.parse(txt); }catch(e){ data = txt; }
      if(!resp.ok){ throw new Error((data && data.message) ? data.message : txt); }
      setImportProgress(100, '入库完成');
      setTimeout(hideImportProgress, 800);
      alert('入库成功');
      state.items = [];
      renderTable();
    }catch(err){
      console.error('[submitStockIn] failed', err);
      alert('入库失败: ' + (err.message||err));
      hideImportProgress();
    }finally{ state.submitting = false; }
    }

    async function ensureRealMedicineIds(items){
    const result = [];
    for(const item of items){
      if(item.medicineId && !String(item.medicineId).startsWith('IMP')){ result.push(item); continue; }
      const name = item.medicineName || '未命名';
      const payload = {
        genericName: name,
        tradeName: item.tradeName || '',
        spec: item.spec || '',
        manufacturer: item.manufacturer || '',
        unit: item.unit || '盒',
        isRx: false,
        approvalNo: item.approvalNo || ('TMP' + Date.now()),
        retailPrice: Number(item.retailPrice||0),
        memberPrice: item.memberPrice!=null? Number(item.memberPrice): null,
        categoryId: 1,
        barcode: item.barcode || undefined,
        status:'ACTIVE',
        deleted:false
      };
      try{
        const med = await createMedicine(payload);
        const normalized = (med && med.data)? med.data : med;
        const mid = normalized.medicineId || String(normalized.id || ('M'+Date.now()));
        result.push({ ...item, medicineId: mid });
      }catch(err){
        console.error('[ensureRealMedicineIds] 创建药品失败', err);
        throw err;
      }
    }
    return result;
    }

    function bindUi(){
      console.log('[stock-in] Binding UI events...');
      const on = (id, evt, fn) => {
        const el = qs(id);
        if(!el) { console.warn('[stock-in] Element not found:', id); return; }
        el.addEventListener(evt, fn);
      };

      // 顶部与中部“批量导入”都触发隐藏 file input
      const triggerImport = () => { const f = qs('bulk-file'); if(f) { f.value = ''; f.click(); } };
      on('import-data-top', 'click', triggerImport);
      on('import-data-bulk', 'click', triggerImport);

      // file 选择后解析
      on('bulk-file', 'change', handleBulkFileChange);

      // 入库单
      on('new-stock', 'click', (e)=>{ e.preventDefault(); newStock(); });
      on('complete-stock', 'click', (e)=>{ e.preventDefault(); submitStockIn(); });

      // 添加药品
      on('quick-add', 'click', (e)=>{ e.preventDefault(); openQuickAdd(); });
      on('add-medicine', 'click', (e)=>{ e.preventDefault(); openMedDrawer(); });

      // 右侧抽屉
      on('close-med-drawer', 'click', (e)=>{ e.preventDefault(); closeMedDrawer(); });
      on('fm-cancel', 'click', (e)=>{ e.preventDefault(); closeMedDrawer(); });
      on('fm-submit', 'click', (e)=>{ e.preventDefault(); submitMedForm(); });

      const backdrop = document.getElementById('drawer-backdrop');
      if(backdrop){ backdrop.addEventListener('click', closeMedDrawer); }
    }

  // 初始化：默认填入日期 + 绑定事件 + 渲染空表
  if(document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', ()=>{ newStock(); bindUi(); });
  }else{
    newStock(); bindUi();
  }
})();
