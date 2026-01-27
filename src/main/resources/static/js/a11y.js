// a11y.js - 全局可访问性增强
(function(){
  document.addEventListener('DOMContentLoaded',()=>{
    // Skip link 注入（若不存在）
    if(!document.querySelector('.skip-link')){
      const skip=document.createElement('a');
      skip.href='#main-content';
      skip.className='skip-link';
      skip.textContent='跳到主内容';
      document.body.prepend(skip);
    }
    // 主内容 id 兜底
    const mainTarget=document.querySelector('#main-content')||document.querySelector('.main-content');
    if(mainTarget && !mainTarget.id){ mainTarget.id='main-content'; }
    // 全局 aria-live 区域
    if(!document.getElementById('aria-live-region')){
      const live=document.createElement('div');
      live.id='aria-live-region';
      live.setAttribute('aria-live','polite');
      live.setAttribute('aria-atomic','true');
      document.body.appendChild(live);
    }
    // 为导航与侧栏交互元素提供 tabindex
    document.querySelectorAll('#common-nav a, .sidebar-item').forEach(el=>{
      if(!el.hasAttribute('tabindex')) el.setAttribute('tabindex','0');
    });
  });
  // 提供全局 announce 函数
  window.announce = function(msg){
    const live=document.getElementById('aria-live-region');
    if(live){ live.textContent=''; setTimeout(()=>{ live.textContent=msg; },10); }
  };
})();

