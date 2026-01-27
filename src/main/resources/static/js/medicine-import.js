// medicine-import.js - 药品批量导入功能
class MedicineBatchCreator {
    constructor() {
        this.templateStructure = [
            { field: 'name', name: '药品名称', required: true, example: '阿莫西林胶囊', type: 'text' },
            { field: 'commonName', name: '通用名', required: false, example: '阿莫西林', type: 'text' },
            { field: 'specification', name: '规格', required: true, example: '0.25g*24粒', type: 'text' },
            { field: 'manufacturer', name: '生产厂家', required: true, example: '北京制药厂', type: 'text' },
            { field: 'category', name: '药品分类', required: false, example: '抗生素', type: 'text' },
            { field: 'purchasePrice', name: '采购价', required: true, example: '15.80', type: 'number' },
            { field: 'retailPrice', name: '零售价', required: true, example: '25.00', type: 'number' },
            { field: 'stock', name: '初始库存', required: false, example: '100', type: 'number' },
            { field: 'minStock', name: '最低库存', required: false, example: '20', type: 'number' },
            { field: 'maxStock', name: '最高库存', required: false, example: '500', type: 'number' },
            { field: 'batchNumber', name: '批号', required: true, example: 'BN20240501', type: 'text' },
            { field: 'expiryDate', name: '有效期至', required: true, example: '2025-12-31', type: 'date' },
            { field: 'approvalNumber', name: '批准文号', required: false, example: '国药准字H13022166', type: 'text' },
            { field: 'description', name: '药品说明', required: false, example: '用于治疗细菌感染', type: 'text' }
        ];

        this.importData = [];
        this.validationErrors = [];
        this.existingMedicines = [];
    }

    // 初始化功能
    init() {
        this.initEventListeners();
        this.generateTemplate();
    }

    // 初始化事件监听
    initEventListeners() {
        // 下载模板
        document.getElementById('download-template').addEventListener('click', () => {
            this.downloadTemplate();
        });

        // 文件选择
        document.getElementById('import-file').addEventListener('change', (e) => {
            this.handleFileSelect(e);
        });

        // 拖拽上传
        const dropZone = document.getElementById('drop-zone');
        if (dropZone) {
            dropZone.addEventListener('dragover', (e) => {
                e.preventDefault();
                dropZone.classList.add('border-primary', 'bg-blue-50');
            });

            dropZone.addEventListener('dragleave', (e) => {
                e.preventDefault();
                dropZone.classList.remove('border-primary', 'bg-blue-50');
            });

            dropZone.addEventListener('drop', (e) => {
                e.preventDefault();
                dropZone.classList.remove('border-primary', 'bg-blue-50');
                this.handleFileSelect({ target: { files: e.dataTransfer.files } });
            });
        }

        // 开始导入
        document.getElementById('start-import').addEventListener('click', () => {
            this.startImport();
        });

        // 手动添加药品
        document.getElementById('add-manual-medicine').addEventListener('click', () => {
            this.showManualAddForm();
        });
    }

    // 生成模板文件
    generateTemplate() {
        const headers = this.templateStructure.map(col => col.name);
        const examples = this.templateStructure.map(col => col.example);

        const templateData = [headers, examples];
        this.templateCsv = this.convertToCSV(templateData);
    }

    // 下载模板
    downloadTemplate() {
        const blob = new Blob(["\uFEFF" + this.templateCsv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);

        link.setAttribute('href', url);
        link.setAttribute('download', '药品批量创建模板.csv');
        link.style.visibility = 'hidden';

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        if (window.api && window.api.showMessage) {
            window.api.showMessage('模板下载成功，请按照模板格式填写药品信息', 'success');
        }
    }

    // 处理文件选择
    async handleFileSelect(event) {
        const file = event.target.files[0];
        if (!file) return;

        // 验证文件类型
        if (!file.name.endsWith('.csv')) {
            if (window.api && window.api.showMessage) {
                window.api.showMessage('请选择CSV格式的文件', 'error');
            }
            return;
        }

        // 验证文件大小 (5MB)
        if (file.size > 5 * 1024 * 1024) {
            if (window.api && window.api.showMessage) {
                window.api.showMessage('文件大小不能超过5MB', 'error');
            }
            return;
        }

        this.showLoading(true, '正在解析文件...');

        try {
            const content = await this.readFile(file);
            const data = this.parseCSV(content);

            // 验证文件结构
            if (!this.validateFileStructure(data)) {
                if (window.api && window.api.showMessage) {
                    window.api.showMessage('文件格式不正确，请下载模板文件查看正确格式', 'error');
                }
                return;
            }

            // 处理数据
            await this.processImportData(data);
            this.showPreview();

        } catch (error) {
            console.error('文件处理错误:', error);
            if (window.api && window.api.showMessage) {
                window.api.showMessage('文件读取失败，请检查文件格式和编码', 'error');
            }
        } finally {
            this.showLoading(false);
        }
    }

    // 读取文件内容
    readFile(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = (e) => resolve(e.target.result);
            reader.onerror = reject;
            reader.readAsText(file, 'UTF-8');
        });
    }

    // 解析CSV文件
    parseCSV(content) {
        const lines = content.split('\n').filter(line => line.trim());
        return lines.map(line => {
            const result = [];
            let current = '';
            let inQuotes = false;

            for (let i = 0; i < line.length; i++) {
                const char = line[i];

                if (char === '"') {
                    inQuotes = !inQuotes;
                } else if (char === ',' && !inQuotes) {
                    result.push(current);
                    current = '';
                } else {
                    current += char;
                }
            }

            result.push(current);
            return result.map(field => field.trim().replace(/^"|"$/g, ''));
        });
    }

    // 验证文件结构
    validateFileStructure(data) {
        if (data.length < 2) {
            if (window.api && window.api.showMessage) {
                window.api.showMessage('文件内容为空或格式不正确', 'error');
            }
            return false;
        }

        const headers = data[0];
        const expectedHeaders = this.templateStructure.map(col => col.name);

        // 检查表头数量
        if (headers.length !== expectedHeaders.length) {
            if (window.api && window.api.showMessage) {
                window.api.showMessage(`表头数量不正确，期望 ${expectedHeaders.length} 列，实际 ${headers.length} 列`, 'error');
            }
            return false;
        }

        // 检查表头内容
        for (let i = 0; i < headers.length; i++) {
            if (headers[i] !== expectedHeaders[i]) {
                if (window.api && window.api.showMessage) {
                    window.api.showMessage(`表头"${headers[i]}"不正确，应为"${expectedHeaders[i]}"`, 'error');
                }
                return false;
            }
        }

        return true;
    }

    // 处理导入数据
    async processImportData(data) {
        this.importData = [];
        this.validationErrors = [];

        // 检查药品是否已存在
        const medicineNames = [];
        for (let i = 1; i < data.length; i++) {
            const name = data[i][0] ? data[i][0].trim() : '';
            if (name) medicineNames.push(name);
        }

        if (medicineNames.length > 0) {
            try {
                if (window.api && window.api.batchMedicineAPI) {
                    const existsResult = await window.api.batchMedicineAPI.checkExists(medicineNames);
                    this.existingMedicines = existsResult.existing || [];
                }
            } catch (error) {
                console.warn('检查药品存在性失败:', error);
            }
        }

        // 处理每一行数据
        for (let i = 1; i < data.length; i++) {
            const row = data[i];
            const medicine = { id: i };
            const errors = [];

            // 映射数据到对象
            this.templateStructure.forEach((col, index) => {
                const value = row[index] ? row[index].trim() : '';
                medicine[col.field] = value;

                // 验证必填字段
                if (col.required && !value) {
                    errors.push(`${col.name}不能为空`);
                }

                // 数据类型验证和转换
                if (value) {
                    switch (col.field) {
                        case 'purchasePrice':
                        case 'retailPrice':
                            const price = this.parsePrice(value);
                            if (isNaN(price) || price < 0) {
                                errors.push(`${col.name}格式不正确`);
                            } else {
                                medicine[col.field] = price;
                            }
                            break;
                        case 'stock':
                        case 'minStock':
                        case 'maxStock':
                            const num = parseInt(value);
                            if (isNaN(num) || num < 0) {
                                errors.push(`${col.name}必须是正整数`);
                            } else {
                                medicine[col.field] = num;
                            }
                            break;
                        case 'expiryDate':
                            if (!this.isValidDate(value)) {
                                errors.push(`${col.name}格式不正确，应为YYYY-MM-DD`);
                            }
                            break;
                    }
                }
            });

            // 检查药品是否已存在
            if (medicine.name && this.existingMedicines.includes(medicine.name)) {
                errors.push('药品已存在');
            }

            if (errors.length > 0) {
                this.validationErrors.push({
                    row: i + 1,
                    medicine: medicine.name || '未知药品',
                    errors: errors,
                    data: medicine
                });
            } else {
                this.importData.push(medicine);
            }
        }
    }

    // 解析价格
    parsePrice(value) {
        if (!value) return 0;
        const cleanValue = value.replace(/[¥,￥]/g, '');
        return parseFloat(cleanValue) || 0;
    }

    // 验证日期格式
    isValidDate(dateString) {
        const regex = /^\d{4}-\d{2}-\d{2}$/;
        if (!regex.test(dateString)) return false;

        const date = new Date(dateString);
        return date instanceof Date && !isNaN(date);
    }

    // 显示预览
    showPreview() {
        const previewContainer = document.getElementById('import-preview');
        const summaryContainer = document.getElementById('import-summary');

        let html = '';

        // 显示错误信息
        if (this.validationErrors.length > 0) {
            html += `
                <div class="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
                    <div class="flex items-center text-red-800 mb-2">
                        <i class="fa fa-exclamation-triangle mr-2"></i>
                        <span class="font-medium">发现 ${this.validationErrors.length} 条数据错误</span>
                    </div>
                    <div class="text-sm text-red-700 max-h-32 overflow-y-auto">
                        ${this.validationErrors.map(error =>
                            `<div class="mb-1">
                                <span class="font-medium">第 ${error.row} 行 (${error.medicine}):</span>
                                ${error.errors.join(', ')}
                            </div>`
                        ).join('')}
                    </div>
                </div>
            `;
        }

        // 显示有效数据预览
        if (this.importData.length > 0) {
            html += `
                <div class="bg-green-50 border border-green-200 rounded-lg p-4 mb-4">
                    <div class="flex items-center text-green-800 mb-2">
                        <i class="fa fa-check-circle mr-2"></i>
                        <span class="font-medium">发现 ${this.importData.length} 条有效数据</span>
                    </div>
                </div>
                <div class="overflow-x-auto border border-gray-200 rounded-lg">
                    <table class="min-w-full divide-y divide-gray-200 text-sm">
                        <thead class="bg-gray-50">
                            <tr>
                                ${this.templateStructure.slice(0, 6).map(col =>
                                    `<th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">${col.name}</th>`
                                ).join('')}
                                <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            ${this.importData.slice(0, 10).map(medicine => `
                                <tr class="hover:bg-gray-50">
                                    <td class="px-3 py-2 font-medium">${medicine.name}</td>
                                    <td class="px-3 py-2">${medicine.specification}</td>
                                    <td class="px-3 py-2">${medicine.manufacturer}</td>
                                    <td class="px-3 py-2">¥${medicine.purchasePrice?.toFixed(2) || '0.00'}</td>
                                    <td class="px-3 py-2">¥${medicine.retailPrice?.toFixed(2) || '0.00'}</td>
                                    <td class="px-3 py-2">${medicine.stock || 0}</td>
                                    <td class="px-3 py-2">
                                        <span class="badge bg-green-100 text-green-800">可创建</span>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                    ${this.importData.length > 10 ? `
                        <div class="text-center py-2 text-gray-500 text-sm bg-gray-50">
                            还有 ${this.importData.length - 10} 条数据未显示...
                        </div>
                    ` : ''}
                </div>
            `;
        } else if (this.validationErrors.length === 0) {
            html = `
                <div class="text-center text-gray-500 py-8">
                    <i class="fa fa-upload text-4xl mb-2"></i>
                    <p>选择文件后预览数据将显示在这里</p>
                </div>
            `;
        }

        previewContainer.innerHTML = html;

        // 更新摘要信息
        if (this.importData.length > 0 || this.validationErrors.length > 0) {
            summaryContainer.innerHTML = `
                <div class="grid grid-cols-3 gap-4 text-center mb-4">
                    <div class="bg-green-50 p-3 rounded-lg border border-green-200">
                        <div class="text-2xl font-bold text-green-600">${this.importData.length}</div>
                        <div class="text-sm text-green-800">可创建药品</div>
                    </div>
                    <div class="bg-red-50 p-3 rounded-lg border border-red-200">
                        <div class="text-2xl font-bold text-red-600">${this.validationErrors.length}</div>
                        <div class="text-sm text-red-800">错误数据</div>
                    </div>
                    <div class="bg-blue-50 p-3 rounded-lg border border-blue-200">
                        <div class="text-2xl font-bold text-blue-600">${this.importData.length + this.validationErrors.length}</div>
                        <div class="text-sm text-blue-800">总数据行</div>
                    </div>
                </div>
            `;
        } else {
            summaryContainer.innerHTML = '';
        }

        // 更新导入按钮状态
        const importBtn = document.getElementById('start-import');
        if (importBtn) {
            importBtn.disabled = this.importData.length === 0;
            importBtn.innerHTML = this.importData.length > 0 ?
                `<i class="fa fa-upload"></i> 创建 ${this.importData.length} 个药品` :
                `<i class="fa fa-upload"></i> 开始导入`;
        }
    }

    // 开始导入
    async startImport() {
        if (this.importData.length === 0) {
            if (window.api && window.api.showMessage) {
                window.api.showMessage('没有有效数据可以导入', 'warning');
            }
            return;
        }

        this.showLoading(true, `正在创建 ${this.importData.length} 个药品...`);

        try {
            // 批量创建药品
            if (window.api && window.api.batchMedicineAPI) {
                const results = await window.api.batchMedicineAPI.batchCreate(this.importData);

                // 显示导入结果
                this.showImportResult(results);
            }

        } catch (error) {
            console.error('导入失败:', error);
            if (window.api && window.api.showMessage) {
                window.api.showMessage('导入失败: ' + (error.message || '请检查网络连接'), 'error');
            }
        } finally {
            this.showLoading(false);
        }
    }

    // 显示导入结果
    showImportResult(results) {
        const successCount = results.success || this.importData.length;
        const failedCount = results.failed || 0;

        const resultHtml = `
            <div class="bg-green-50 border border-green-200 rounded-lg p-6 text-center">
                <i class="fa fa-check-circle text-4xl text-green-500 mb-3"></i>
                <h3 class="text-lg font-bold text-green-800 mb-2">创建完成</h3>
                <div class="text-green-700 mb-4">
                    <p class="text-xl">成功创建 <span class="font-bold">${successCount}</span> 个药品</p>
                    ${failedCount > 0 ?
                        `<p class="text-red-600">失败: <span class="font-bold">${failedCount}</span> 个药品</p>` :
                        '<p class="text-sm">所有药品已成功添加到系统</p>'
                    }
                </div>
                ${results.errors && results.errors.length > 0 ? `
                    <div class="mt-4 text-left bg-white rounded border p-3">
                        <h4 class="font-medium text-red-700 mb-2">失败详情:</h4>
                        <div class="text-sm text-red-600 max-h-32 overflow-y-auto">
                            ${results.errors.map(err =>
                                `<div class="mb-1 border-b pb-1">${err.medicine || '未知药品'}: ${err.error || '创建失败'}</div>`
                            ).join('')}
                        </div>
                    </div>
                ` : ''}
                <div class="mt-4 flex gap-2 justify-center">
                    <button onclick="medicineBatchCreator.closeImportModal()" class="btn btn-outline btn-sm">
                        关闭
                    </button>
                    <button onclick="medicineBatchCreator.resetImport()" class="btn btn-primary btn-sm">
                        继续导入
                    </button>
                </div>
            </div>
        `;

        document.getElementById('import-preview').innerHTML = resultHtml;

        // 禁用导入按钮
        document.getElementById('start-import').disabled = true;

        // 显示成功消息
        if (window.api && window.api.showMessage) {
            window.api.showMessage(`成功创建 ${successCount} 个药品`, 'success');
        }
    }

    // 显示手动添加表单
    showManualAddForm() {
        // 这里可以实现手动添加单个药品的表单
        // 由于时间关系，先简单跳转到药品管理页面
        window.location.href = 'medicine-management.html?action=add';
    }

    // 关闭导入模态框
    closeImportModal() {
        document.getElementById('import-modal').classList.add('hidden');
        this.resetImport();
    }

    // 重置导入状态
    resetImport() {
        this.importData = [];
        this.validationErrors = [];
        this.existingMedicines = [];

        const fileInput = document.getElementById('import-file');
        if (fileInput) fileInput.value = '';

        document.getElementById('import-preview').innerHTML = `
            <div class="text-center text-gray-500 py-8">
                <i class="fa fa-upload text-4xl mb-2"></i>
                <p>选择文件后预览数据将显示在这里</p>
            </div>
        `;
        document.getElementById('import-summary').innerHTML = '';

        const importBtn = document.getElementById('start-import');
        if (importBtn) {
            importBtn.disabled = true;
            importBtn.innerHTML = `<i class="fa fa-upload"></i> 开始导入`;
        }
    }

    // 显示加载状态
    showLoading(show, message = '处理中...') {
        const loadingElement = document.getElementById('import-loading');
        const loadingText = document.getElementById('loading-text');

        if (loadingText) {
            loadingText.textContent = message;
        }

        if (show) {
            loadingElement.classList.remove('hidden');
        } else {
            loadingElement.classList.add('hidden');
        }
    }

    // 转换为CSV格式
    convertToCSV(data) {
        return data.map(row =>
            row.map(field => {
                if (typeof field === 'string' && (field.includes(',') || field.includes('\n') || field.includes('"'))) {
                    return `"${field.replace(/"/g, '""')}"`;
                }
                return field;
            }).join(',')
        ).join('\n');
    }
}

// 初始化药品批量导入功能
let medicineBatchCreator;

document.addEventListener('DOMContentLoaded', function() {
    medicineBatchCreator = new MedicineBatchCreator();
    medicineBatchCreator.init();
});

console.log('medicine-import.js 加载完成');