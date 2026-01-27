// kpi.js - 统一 KPI 计算与渲染管理 (订单类 KPI)
(function(global){
  const KPI = global.KPI || {};
  function animateNumber(el, target, prefix='', suffix='', duration=600){
    if(!el) return; const startVal=parseFloat(el.textContent.replace(/[^0-9.]/g,''))||0; const endVal=parseFloat(target)||0; const start=performance.now();
    function frame(now){ const p=Math.min(1,(now-start)/duration); const val=startVal+(endVal-startVal)*p; el.textContent=prefix+val.toFixed( (endVal%1!==0)?2:0 )+suffix; if(p<1) requestAnimationFrame(frame); }
    requestAnimationFrame(frame);
  }
  function applyThreshold(el, value, th){ if(!el) return; if(th && typeof th==='object'){ const v=parseFloat(value)||0; let cls=''; if(th.danger!=null && v>=th.danger) cls='ring-2 ring-red-400'; else if(th.warning!=null && v>=th.warning) cls='ring-2 ring-amber-300'; el.classList.remove('ring-2','ring-red-400','ring-amber-300'); if(cls){ el.classList.add(...cls.split(' ')); } } }

  // 自包含的订单 KPI 渲染
  KPI.updateOrderKpis = function(list,cfg={}){
    const map=Object.assign({salesToday:'kpi-sales-today',ordersToday:'kpi-orders-today',pendingCount:'kpi-pending-count',refundCount:'kpi-refund-count',avgTicket:'kpi-avg-ticket'}, cfg.mapping||{});
    try{
      const orders = Array.isArray(list)? list: [];
      const count = orders.length;
      const totalAmt = orders.reduce((s,o)=>{ const v=(o.actualPayment!=null?o.actualPayment:(o.totalAmount||0)); const n=typeof v==='number'?v:parseFloat(v); return s+(isNaN(n)?0:n); },0);
      const avg = count>0? (totalAmt/count):0;
      const elCount=document.getElementById(map.ordersToday); if(elCount) elCount.textContent=String(count);
      const elSales=document.getElementById(map.salesToday); if(elSales) elSales.textContent='¥'+totalAmt.toFixed(2);
      const elAvg=document.getElementById(map.avgTicket); if(elAvg) elAvg.textContent='¥'+avg.toFixed(2);
      // 阈值与动画
      ['salesToday','ordersToday','avgTicket'].forEach(k=>{ const id=map[k]; const el=document.getElementById(id); if(el){ const raw=el.textContent.replace(/^¥/,''); animateNumber(el, raw, k==='salesToday'||k==='avgTicket'?'¥':''); }});
      applyThreshold(document.getElementById(map.pendingCount), document.getElementById(map.pendingCount)?.textContent, {warning:5,danger:10});
      applyThreshold(document.getElementById(map.refundCount), document.getElementById(map.refundCount)?.textContent, {warning:2,danger:5});
    }catch(e){ console.warn('KPI basic render failed', e); }
  };

  // 其他 KPI 保持原有自包含实现
  KPI.updateInventoryKpis = KPI.updateInventoryKpis || function(data,cfg={}){ const map=Object.assign({total:'kpi-total-medicines',low:'kpi-lowstock',expiry:'kpi-expiry'}, cfg.mapping||{}); if(!data){ ['total','low','expiry'].forEach(k=>{const el=document.getElementById(map[k]); if(el) el.textContent='--';}); return; } const total=data.totalMedicines||0; const low=data.lowStock||0; const expiry=data.expiry||0; const totalEl=document.getElementById(map.total); if(totalEl){ animateNumber(totalEl,total); } const lowEl=document.getElementById(map.low); if(lowEl){ animateNumber(lowEl,low); applyThreshold(lowEl,low,{warning:10,danger:30}); } const expEl=document.getElementById(map.expiry); if(expEl){ animateNumber(expEl,expiry); applyThreshold(expEl,expiry,{warning:5,danger:15}); } };
  KPI.updateStockInKpis = KPI.updateStockInKpis || function(data,cfg={}){ const map=Object.assign({count:'kpi-item-count',cost:'kpi-total-cost',price:'kpi-total-price',profit:'kpi-total-profit'},cfg.mapping||{}); if(!data){ Object.values(map).forEach(id=>{const el=document.getElementById(id); if(el) el.textContent='--';}); return; } animateNumber(document.getElementById(map.count), data.itemCount||0,'',''); animateNumber(document.getElementById(map.cost), data.totalCost||0,'¥'); animateNumber(document.getElementById(map.price), data.totalPrice||0,'¥'); animateNumber(document.getElementById(map.profit), data.totalProfit||0,'¥'); };
  KPI.updateMemberKpis = KPI.updateMemberKpis || function(data,cfg={}){ const map=Object.assign({total:'total-members',vip:'vip-members',new:'new-members',sleep:'sleeping-members'},cfg.mapping||{}); if(!data){ Object.values(map).forEach(id=>{const el=document.getElementById(id); if(el) el.textContent='--';}); return; } const total=data.totalMembers||0; const vip=data.vipMembers||0; const neu=data.newMembers||0; const sleep=data.sleepingMembers||0; animateNumber(document.getElementById(map.total),total,'',' 人'); animateNumber(document.getElementById(map.vip),vip,'',' 人'); animateNumber(document.getElementById(map.new),neu,'',' 人'); animateNumber(document.getElementById(map.sleep),sleep,'',' 人'); applyThreshold(document.getElementById(map.sleep),sleep,{warning:50,danger:100}); };

  global.KPI = KPI;
  document.addEventListener('DOMContentLoaded',()=>{ const rows=document.querySelectorAll('.dashboard-kpis.kpi-horizontal'); rows.forEach(r=>{ if(r.__enhanced) return; r.__enhanced=true; }); });
})(window);
