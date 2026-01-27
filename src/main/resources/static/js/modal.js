// modal.js - 模态框管理
function initModal(openBtnId, modalId, closeBtnId) {
    const openBtn = document.getElementById(openBtnId);
    const modal = document.getElementById(modalId);
    const closeBtn = document.getElementById(closeBtnId);

    if (openBtn) {
        openBtn.addEventListener('click', () => {
            modal.classList.remove('hidden');
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            modal.classList.add('hidden');
        });
    }

    // 点击模态框外部关闭
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.add('hidden');
        }
    });
}

// 初始化所有模态框
function initAllModals() {
    // 药品选择模态框
    initModal('select-medicine', 'medicine-select-modal', 'close-select-modal');
    document.getElementById('close-select-modal-2').addEventListener('click', () => {
        document.getElementById('medicine-select-modal').classList.add('hidden');
    });

    // 入库单模态框
    initModal('add-stock-in', 'stock-in-modal', 'close-stock-in-modal');
    document.getElementById('cancel-stock-in').addEventListener('click', () => {
        document.getElementById('stock-in-modal').classList.add('hidden');
    });

    // 导入模态框
    initModal('import-data', 'import-modal', 'close-import-modal');
    document.getElementById('close-import-modal-2').addEventListener('click', () => {
        document.getElementById('import-modal').classList.add('hidden');
    });
}

console.log('modal.js 加载完成');