// ui-global.js - 全局统一交互增强 (滚动进度 / 主题切换 / 键盘导航 / 无障碍提示)
(function(){
  const doc = document;
  function initScrollProgress(){
    const bar = doc.getElementById('scroll-progress');
    if(!bar) return;
    bar.classList.add('enhanced-bar');
    function update(){
      const h = doc.documentElement.scrollHeight - window.innerHeight;
      const sc = window.scrollY; bar.style.width = h>0? (sc/h)*100 + '%' : '0%';
    }
    window.addEventListener('scroll', update, {passive:true});
    update();
  }
  function initThemeToggle(){
    if(doc.getElementById('theme-toggle')) return;
    const btn = doc.createElement('button');
    btn.id='theme-toggle'; btn.className='theme-toggle header-icon-btn'; btn.type='button';
    btn.setAttribute('aria-label','切换主题');
    btn.innerHTML='<i class="fa fa-moon-o"></i>';
    btn.onclick=()=>{ const dark=doc.documentElement.classList.toggle('dark'); btn.innerHTML='<i class="fa '+(dark?'fa-sun-o':'fa-moon-o')+'"></i>'; announce && announce(dark?'已切换到暗色模式':'已切换到浅色模式'); };
    doc.body.appendChild(btn);
  }
  function initSidebarKeyboard(){
    const items = Array.from(doc.querySelectorAll('aside .sidebar-item'));
    if(!items.length) return;
    items.forEach(it=>{ it.setAttribute('tabindex','0'); });
    doc.addEventListener('keydown',e=>{
      if(['ArrowDown','ArrowUp'].includes(e.key)){
        const activeEl = doc.activeElement;
        if(!items.includes(activeEl)) return;
        e.preventDefault();
        let idx = items.indexOf(activeEl);
        idx = e.key==='ArrowDown'? (idx+1)%items.length : (idx-1+items.length)%items.length;
        items[idx].focus();
      }
      if(e.key==='Enter' && items.includes(doc.activeElement)){
        doc.activeElement.click();
      }
    });
  }
  function markAriaCurrent(){
    const active = doc.querySelector('aside .sidebar-item.active');
    if(active) active.setAttribute('aria-current','page');
  }
  function initRipple(){
    doc.querySelectorAll('.btn').forEach(btn=>btn.setAttribute('data-ripple',''));
  }
  function applyARIA(){
    const aside = doc.querySelector('aside'); if(aside){ aside.setAttribute('role','navigation'); aside.setAttribute('aria-label','侧边主导航'); }
    const header = doc.querySelector('header'); if(header){ header.setAttribute('role','banner'); }
  }
  function init(){ initScrollProgress(); initThemeToggle(); initSidebarKeyboard(); markAriaCurrent(); initRipple(); applyARIA(); }
  if(doc.readyState==='loading'){ doc.addEventListener('DOMContentLoaded', init); } else init();
})();
(function(){
  const doc=document;
  function autoDarkMode(){
    try {
      const prefersDark=window.matchMedia('(prefers-color-scheme: dark)');
      function apply(e){ if(e.matches && !doc.documentElement.classList.contains('dark') && !doc.body.classList.contains('force-light')){ doc.body.classList.add('auto-dark'); } else { doc.body.classList.remove('auto-dark'); } }
      prefersDark.addEventListener('change',apply); apply(prefersDark);
    } catch(err){ console.warn('DarkMode media query unsupported',err); }
  }
  function markUltraWide(){ if(window.innerWidth>=1920){ doc.body.classList.add('ultra-wide'); } else { doc.body.classList.remove('ultra-wide'); } }
  function normalizeKPIHeights(){
    const cards=Array.from(doc.querySelectorAll('.dashboard-kpis .card'));
    if(!cards.length) return; let max=0; cards.forEach(c=>{ c.style.minHeight=''; const h=c.getBoundingClientRect().height; if(h>max) max=h; });
    const target=Math.max(148,Math.min(max,220)); cards.forEach(c=> c.style.minHeight=target+'px');
  }
  function dynamicAutoGrid(){
    // 查找自动栅格容器：可用于未来的 KPI 或图表区动态调整
    doc.querySelectorAll('.auto-grid').forEach(g=>{
      const items=g.children; let count=0; Array.from(items).forEach(it=>{ if(it.offsetParent!==null) count++; });
      g.setAttribute('data-count',String(count));
      if(count===0){ g.classList.add('hide-empty'); return; }
      let cols='5'; if(window.innerWidth<1600) cols='4'; if(window.innerWidth<1280) cols='3'; if(window.innerWidth<960) cols='2'; if(window.innerWidth<640) cols='1';
      g.setAttribute('data-cols',cols);
    });
  }
  function hideExplicitEmpty(){
    doc.querySelectorAll('[data-empty-check]').forEach(sec=>{
      const targetSelector=sec.getAttribute('data-empty-check');
      const target= targetSelector? doc.querySelector(targetSelector): null; if(!target) return;
      const hasContent= target.textContent.trim().length>0 || target.querySelector('tr, div, section, article');
      if(!hasContent){ sec.classList.add('hide-empty'); sec.setAttribute('data-count','0'); } else { sec.classList.remove('hide-empty'); }
    });
  }
  function debounce(fn,wait){ let t; return function(){ clearTimeout(t); const args=arguments; t=setTimeout(()=>fn.apply(this,args),wait); }; }
  function initEnhancements(){ autoDarkMode(); markUltraWide(); dynamicAutoGrid(); hideExplicitEmpty(); normalizeKPIHeights(); }
  window.addEventListener('resize',debounce(()=>{ markUltraWide(); dynamicAutoGrid(); normalizeKPIHeights(); },200));
  if(doc.readyState==='loading'){ doc.addEventListener('DOMContentLoaded',initEnhancements); } else initEnhancements();
})();
