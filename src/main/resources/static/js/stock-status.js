// stock-status.js
// 统一库存与效期状态工具
(function(){
  const EXPIRY_WARNING_DAYS = 60;
  function getStockStatus(rawCurrent, rawSafety){
    let current = Number(rawCurrent)||0;
    let safety = Number(rawSafety)||0;
    if(safety<=0) safety = 1;
    const ratio = current / safety;
    let code, label, cls;
    if(current===0){ code='OUT'; label='缺货'; cls='stock-critical'; }
    else if(ratio<=0.1){ code='CRITICAL'; label='严重不足'; cls='stock-critical'; }
    else if(ratio<=0.3){ code='LOW'; label='库存不足'; cls='stock-low'; }
    else if(ratio<=0.8){ code='MEDIUM'; label='库存一般'; cls='stock-medium'; }
    else { code='HIGH'; label='库存充足'; cls='stock-high'; }
    return { code, ratio, label, text: label, class: cls };
  }
  function getExpiryStatus(expiryDate){
    if(!expiryDate || expiryDate==='-' || expiryDate==='未知日期') return { code:'UNKNOWN', label:'未知', text:'未知', class:'stock-medium', days:null };
    try{
      const today = new Date();
      const exp = new Date(expiryDate);
      const days = Math.ceil((exp - today)/(1000*60*60*24));
      let code, label, cls;
      if(days < 0){ code='EXPIRED'; label='已过期'; cls='stock-critical'; }
      else if(days <= 30){ code='NEAR_EXPIRY'; label=`近效期(${days}天)`; cls='stock-low'; }
      else if(days <= EXPIRY_WARNING_DAYS){ code='WARNING'; label=`近效期(${days}天)`; cls='stock-low'; }
      else { code='NORMAL'; label='正常'; cls='stock-high'; }
      return { code, label, text: label, class: cls, days };
    }catch(e){ return { code:'INVALID_DATE', label:'日期错误', text:'日期错误', class:'stock-medium', days:null }; }
  }
  function mapStockBadgeClass(code){
    switch(code){
      case 'OUT':
      case 'CRITICAL': return 'stock-critical';
      case 'LOW': return 'stock-low';
      case 'MEDIUM': return 'stock-medium';
      case 'HIGH': return 'stock-high';
      default: return 'stock-medium';
    }
  }
  function mapBorderClass(code){
    switch(code){
      case 'OUT':
      case 'CRITICAL': return 'border-red-500';
      case 'LOW': return 'border-orange-500';
      case 'MEDIUM': return 'border-yellow-500';
      case 'HIGH': return 'border-green-500';
      default: return 'border-gray-300';
    }
  }
  function mapExpiryClass(code){
    switch(code){
      case 'EXPIRED': return 'stock-critical';
      case 'NEAR_EXPIRY':
      case 'WARNING': return 'stock-low';
      case 'NORMAL': return 'stock-high';
      default: return 'stock-medium';
    }
  }
  function mapStockStatusText(code){
    switch(code){
      case 'OUT': return '缺货';
      case 'CRITICAL': return '严重不足';
      case 'LOW': return '库存不足';
      case 'MEDIUM': return '库存一般';
      case 'HIGH': return '库存充足';
      default: return '--';
    }
  }
  window.StockStatusUtil = {
    getStockStatus,
    getExpiryStatus,
    mapStockBadgeClass,
    mapBorderClass,
    mapExpiryClass,
    mapStockStatusText
  };
  // 兼容旧调用：部分页面仍使用 StockStatus.getStockStatus / getExpiryStatus
  window.StockStatus = window.StockStatus || { getStockStatus, getExpiryStatus, mapStockStatusText, getBadgeClass: mapStockBadgeClass, getExpiryClass: mapExpiryClass };
})();
