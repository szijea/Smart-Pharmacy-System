// common-nav.js
// 在页面中通过 <div id="common-nav" data-active="inventory"></div> 注入统一的侧边栏和顶部导航。
(function(){
    function escapeHtml(s){ return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
    function parseJsonSafe(str){ try{ return JSON.parse(str); }catch(e){ return null; } }
    function getCurrentUserInfo(){
        // 优先读取登录返回的 authUser 数据
        var authUserRaw = localStorage.getItem('authUser');
        var authUser = authUserRaw && parseJsonSafe(authUserRaw);
        if(authUser && (authUser.username || authUser.name)){
            return {
                username: authUser.username || '未登录',
                displayName: authUser.name || authUser.username || '未登录',
                id: authUser.id,
                roleId: authUser.roleId
            };
        }

        // 备用：读取旧的 currentUser 或 loginUsername
        var raw = localStorage.getItem('currentUser');
        var obj = raw && parseJsonSafe(raw);
        var loginUsername = localStorage.getItem('loginUsername');
        if(obj && (obj.username || obj.displayName)){
            return { username: obj.username || loginUsername || '未登录', displayName: obj.displayName || obj.username || loginUsername || '未登录' };
        }
        if(loginUsername){ return { username: loginUsername, displayName: loginUsername }; }
        if(raw){ return { username: raw, displayName: raw }; }
        return { username: '未登录', displayName: '未登录' };
    }
    function getCurrentRoleText(){
        // 优先从 authUser 中读取 roleId
        var authUserRaw = localStorage.getItem('authUser');
        var authUser = authUserRaw && parseJsonSafe(authUserRaw);
        var roleId = authUser && authUser.roleId ? String(authUser.roleId) : localStorage.getItem('userRoleId');

        switch(roleId){
            case '1': return '管理员';
            case '2': return '店长';
            case '3': return '店员';
            default:
                // 若存在 currentUser 对象但无角色，默认显示"店员"而非访客
                var info = getCurrentUserInfo();
                return info.username === '未登录' ? '访客' : '店员';
        }
    }
    function buildNav(active){
        // 链接列表（新增 sales）
        var links = [
            {href: 'medicine-management.html', icon: 'fa-dashboard', text: '控制台', key: 'dashboard'},
            {href: 'cashier.html', icon: 'fa-shopping-cart', text: '收银管理', key: 'cashier'},
            {href: 'stock-in.html', icon: 'fa-box', text: '药品入库', key: 'stock-in'},
            {href: 'inventory.html', icon: 'fa-warehouse', text: '库存管理', key: 'inventory'},
            {href: 'order-history.html', icon: 'fa-history', text: '历史订单', key: 'order-history'},
            {href: 'members.html', icon: 'fa-users', text: '会员管理', key: 'members'}
        ];
        // 仅管理员可见的链接
        var adminLinks = [
            {href: 'staff.html', icon: 'fa-user-md', text: '员工管理', key: 'staff'},
            {href: 'settings.html', icon: 'fa-cog', text: '系统设置', key: 'settings'}
        ];
        var activeKey = (active||'').toLowerCase();
        var info = getCurrentUserInfo();
        var roleId = localStorage.getItem('userRoleId');
        var roleDisplay = escapeHtml(getCurrentRoleText());
        var primaryDisplay = escapeHtml(info.displayName);
        var secondaryDisplay = roleDisplay;
        var aside = '' +
        '<aside role="navigation" aria-label="侧边主导航" class="w-64 bg-white border-r border-gray-200 flex-shrink-0 flex flex-col h-screen transition-all duration-300 ease-in-out z-10">' +
        '  <div class="p-4 border-b border-gray-200">' +
        '    <div class="flex items-center gap-3">' +
        '      <div class="w-10 h-10 bg-primary/10 rounded-lg flex items-center justify-center">' +
        '        <i class="fa fa-medkit text-primary"></i>' +
        '      </div>' +
        '      <h1 class="font-bold text-lg">智慧药房系统</h1>' +
        '    </div>' +
        '  </div>' +
        '  <div class="flex-1 overflow-y-auto py-4 px-3">' +
        '    <nav class="space-y-1">' +
        '      <p class="text-xs font-medium text-gray-500 px-4 mb-2">主功能</p>';

        links.forEach(function(l){
            var isActive = (l.key === activeKey) ? ' sidebar-item active block' : ' sidebar-item block';
            aside += '<a href="'+escapeHtml(l.href)+'" class="'+isActive+'">' +
                     '<i class="fa '+l.icon+' w-5 text-center"></i>' +
                     '<span>'+escapeHtml(l.text)+'</span>' +
                     '</a>';
        });
        // 仅管理员显示系统管理
        if(roleId === '1'){
            aside += '      <p class="text-xs font-medium text-gray-500 px-4 mb-2 mt-6">系统管理</p>';
            adminLinks.forEach(function(l){
                var isActive = (l.key === activeKey) ? ' sidebar-item active block' : ' sidebar-item block';
                aside += '<a href="'+escapeHtml(l.href)+'" class="'+isActive+'">' +
                         '<i class="fa '+l.icon+' w-5 text-center"></i>' +
                         '<span>'+escapeHtml(l.text)+'</span>' +
                         '</a>';
            });
        }
        aside += '    </nav>' +
        '  </div>' +
        '  <div class="p-4 border-t border-gray-200">' +
        '    <div class="flex items-center gap-3">' +
        '      <div class="w-10 h-10 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 flex items-center justify-center">' +
        '        <i class="fa fa-user text-primary"></i>' +
        '      </div>' +
        '      <div>' +
        '        <p class="font-medium text-sm" id="nav-username">'+ primaryDisplay +'</p>' +
        '        <p class="text-xs text-gray-500" id="nav-role">'+ secondaryDisplay +'</p>' +
        '      </div>' +
        '      <button id="logout-btn" type="button" class="ml-auto text-gray-400 hover:text-accent" title="退出登录">' +
        '        <i class="fa fa-sign-out"></i>' +
        '      </button>' +
        '      <a id="logout-link-fallback" href="index.html" class="hidden"></a>' +
        '    </div>' +
        '  </div>' +
        '</aside>';

        // Header: compact header with toggle + notifications (no search/time)
        // Fix: Added fixed positioning to ensure header sits at top right, aligning with main content pt-16
        var header = '' +
        '<header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 fixed top-0 right-0 left-64 z-20">' +
        '  <div class="flex items-center gap-4">' +
        '    <button id="sidebar-toggle" class="lg:hidden text-gray-500 hover:text-primary">' +
        '      <i class="fa fa-bars text-xl"></i>' +
        '    </button>' +
        '  </div>' +
        '  <div>' +
        '    <button id="notification-btn" class="relative p-2 text-gray-500 hover:text-primary hover:bg-gray-100 rounded-full">' +
        '      <i class="fa fa-bell text-xl"></i>' +
        '      <span class="absolute top-1 right-1 w-2 h-2 bg-accent rounded-full"></span>' +
        '    </button>' +
        '  </div>' +
        '</header>';

        return aside + header;
    }

    function inject(){
        var container = document.getElementById('common-nav');
        if(!container) return;
        var active = container.getAttribute('data-active') || '';
        container.innerHTML = buildNav(active);
        afterLinkBuild();

        // 侧边栏切换
        var toggle = document.getElementById('sidebar-toggle');
        if(toggle){
            toggle.addEventListener('click', function(){
                var aside = document.querySelector('aside');
                if(aside) aside.classList.toggle('-translate-x-full');
            });
        }

        // 更新日期时间（如果页面已实现类似逻辑，这里不会覆盖）
        var dateEl = document.getElementById('current-date');
        var timeEl = document.getElementById('current-time');
        function updateDateTime(){
            var now = new Date();
            try{ dateEl && (dateEl.textContent = now.toLocaleDateString('zh-CN',{year:'numeric',month:'2-digit',day:'2-digit',weekday:'long'})); }catch(e){}
            try{ timeEl && (timeEl.textContent = now.toLocaleTimeString('zh-CN',{hour:'2-digit',minute:'2-digit',second:'2-digit'})); }catch(e){}
        }
        updateDateTime();
        setInterval(updateDateTime,1000);

        // 退出登录按钮
        var logoutBtn = document.getElementById('logout-btn');
        if(logoutBtn){
            logoutBtn.addEventListener('click', async function(){
                try {
                    const tenant = localStorage.getItem('selectedTenant') || '';
                    await fetch('/api/auth/logout',{method:'POST',headers:{'X-Shop-Id':tenant}}).catch(()=>{});
                } catch(e){ console.warn('Logout API 调用失败(忽略):', e); }
                localStorage.removeItem('authToken');
                localStorage.removeItem('loginUsername');
                localStorage.removeItem('currentUser');
                localStorage.removeItem('userRoleId');
                // 保留 selectedTenant 以便再次登录
                window.location.href = 'index.html';
            });
        }
        // 未登录提示
        var info = getCurrentUserInfo();
        if(info.username === '未登录'){
            console.warn('[common-nav] 未检测到登录用户, 显示访客模式');
        }
        refreshUserLabels();
    }

    // 注入后动态刷新用户名/角色，避免缓存旧的“张药师”
    function refreshUserLabels(){
        var info = getCurrentUserInfo();
        var uEl = document.getElementById('nav-username');
        var rEl = document.getElementById('nav-role');
        if(uEl) uEl.textContent = info.displayName;
        if(rEl) rEl.textContent = getCurrentRoleText();
    }

    function afterLinkBuild(){
        document.querySelectorAll('aside .sidebar-item.active').forEach(a=>a.setAttribute('aria-current','page'));
    }

    if(document.readyState === 'loading'){
        document.addEventListener('DOMContentLoaded', inject);
    } else inject();

    // 导出给其它脚本使用
    window.commonNav = {inject: inject, buildNav: buildNav};
})();
