// import.js - 专业药品导入功能
class MedicineImporter {
    constructor() {
        this.templateStructure = [
            { field: 'name', name: '药品名称', required: true, example: '阿莫西林胶囊' },
            { field: 'specification', name: '规格', required: true, example: '0.25g*24粒' },
            { field: 'manufacturer', name: '生产厂家', required: true, example: '北京制药厂' },
            { field: 'category', name: '药品分类', required: false, example: '抗生素' },
            { field: 'purchasePrice', name: '采购价', required: true, example: '15.80' },
            { field: 'retailPrice', name: '零售价', required: true, example: '25.00' },
            { field: 'stock', name: '初始库存', required: false, example: '100' },
            { field: 'minStock', name: '最低库存', required: false, example: '20' },
            { field: 'maxStock', name: '最高库存', required: false, example: '500' },
            { field: 'batchNumber', name: '批号', required: false, example: 'BN20240501' },
            { field: 'expiryDate', name: '有效期至', required: false, example: '2025-12-31' },
            { field: 'description', name: '药品说明', required: false, example: '用于治疗细菌感染' }
        ];

        this.importData = [];
        this.validationErrors = [];
    }

    // 初始化导入功能
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

        // 开始导入
        document.getElementById('start-import').addEventListener('click', () => {
            this.startImport();
        });

        // 关闭模态框
        document.getElementById('close-import-modal').addEventListener('click', () => {
            this.resetImport();
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
        const blob = new Blob([this.templateCsv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);

        link.setAttribute('href', url);
        link.setAttribute('download', '药品导入模板.csv');
        link.style.visibility = 'hidden';

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    // 处理文件选择
    async handleFileSelect(event) {
        const file = event.target.files[0];
        if (!file) return;

        // 验证文件类型
        if (!file.name.endsWith('.csv')) {
            this.showMessage('请选择CSV格式的文件', 'error');
            return;
        }

        // 显示加载状态
        this.showLoading(true);

        try {
            const content = await this.readFile(file);
            const data = this.parseCSV(content);

            // 验证文件结构
            if (!this.validateFileStructure(data)) {
                this.showMessage('文件格式不正确，请下载模板文件查看正确格式', 'error');
                return;
            }

            // 处理数据
            this.processImportData(data);
            this.showPreview();

        } catch (error) {
            console.error('文件处理错误:', error);
            this.showMessage('文件读取失败，请检查文件格式', 'error');
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
            // 处理包含逗号的字段
            const result = [];
            let inQuotes = false;
            let currentField = '';

            for (let i = 0; i < line.length; i++) {
                const char = line[i];

                if (char === '"') {
                    inQuotes = !inQuotes;
                } else if (char === ',' && !inQuotes) {
                    result.push(currentField);
                    currentField = '';
                } else {
                    currentField += char;
                }
            }

            result.push(currentField);
            return result;
        });
    }

    // 验证文件结构
    validateFileStructure(data) {
        if (data.length < 2) return false;

        const headers = data[0];
        const expectedHeaders = this.templateStructure.map(col => col.name);

        if (headers.length !== expectedHeaders.length) return false;

        for (let i = 0; i < headers.length; i++) {
            if (headers[i] !== expectedHeaders[i]) return false;
        }

        return true;
    }

    // 处理导入数据
    processImportData(data) {
        this.importData = [];
        this.validationErrors = [];

        // 跳过表头
        for (let i = 1; i < data.length; i++) {
            const row = data[i];
            const medicine = {};
            const errors = [];

            // 映射数据
            this.templateStructure.forEach((col, index) => {
                const value = row[index] ? row[index].trim() : '';

                // 验证必填字段
                if (col.required && !value) {
                    errors.push(`${col.name}不能为空`);
                }

                // 数据类型转换
                switch (col.field) {
                    case 'purchasePrice':
                    case 'retailPrice':
                        medicine[col.field] = this.parsePrice(value);
                        if (isNaN(medicine[col.field])) {
                            errors.push(`${col.name}格式不正确`);
                        }
                        break;
                    case 'stock':
                    case 'minStock':
                    case 'maxStock':
                        medicine[col.field] = parseInt(value) || 0;
                        break;
                    default:
                        medicine[col.field] = value;
                }
            });

            if (errors.length > 0) {
                this.validationErrors.push({
                    row: i + 1,
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
        // 移除人民币符号和逗号
        const cleanValue = value.replace(/[¥,]/g, '');
        return parseFloat(cleanValue) || 0;
    }

    // 显示预览
    showPreview() {
        const previewContainer = document.getElementById('import-preview');
        const summaryContainer = document.getElementById('import-summary');

        let html = '';

        if (this.validationErrors.length > 0) {
            html += `
                <div class="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
                    <div class="flex items-center text-red-800 mb-2">
                        <i class="fa fa-exclamation-triangle mr-2"></i>
                        <span class="font-medium">发现 ${this.validationErrors.length} 条数据错误</span>
                    </div>
                    <div class="text-sm text-red-700">
                        ${this.validationErrors.map(error =>
                            `第 ${error.row} 行: ${error.errors.join(', ')}`
                        ).join('<br>')}
                    </div>
                </div>
            `;
        }

        if (this.importData.length > 0) {
            html += `
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-gray-200 text-sm">
                        <thead class="bg-gray-50">
                            <tr>
                                ${this.templateStructure.slice(0, 6).map(col =>
                                    `<th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase">${col.name}</th>`
                                ).join('')}
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            ${this.importData.slice(0, 10).map(medicine => `
                                <tr class="hover:bg-gray-50">
                                    <td class="px-3 py-2">${medicine.name}</td>
                                    <td class="px-3 py-2">${medicine.specification}</td>
                                    <td class="px-3 py-2">${medicine.manufacturer}</td>
                                    <td class="px-3 py-2">¥${medicine.purchasePrice}</td>
                                    <td class="px-3 py-2">¥${medicine.retailPrice}</td>
                                    <td class="px-3 py-2">${medicine.stock || 0}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                    ${this.importData.length > 10 ? `
                        <div class="text-center py-2 text-gray-500 text-sm">
                            还有 ${this.importData.length - 10} 条数据未显示...
                        </div>
                    ` : ''}
                </div>
            `;
        }

        previewContainer.innerHTML = html;

        // 更新摘要信息
        summaryContainer.innerHTML = `
            <div class="grid grid-cols-3 gap-4 text-center">
                <div class="bg-green-50 p-3 rounded-lg">
                    <div class="text-2xl font-bold text-green-600">${this.importData.length}</div>
                    <div class="text-sm text-green-800">有效数据</div>
                </div>
                <div class="bg-red-50 p-3 rounded-lg">
                    <div class="text-2xl font-bold text-red-600">${this.validationErrors.length}</div>
                    <div class="text-sm text-red-800">错误数据</div>
                </div>
                <div class="bg-blue-50 p-3 rounded-lg">
                    <div class="text-2xl font-bold text-blue-600">${this.importData.length + this.validationErrors.length}</div>
                    <div class="text-sm text-blue-800">总数据行</div>
                </div>
            </div>
        `;

        // 更新导入按钮状态
        const importBtn = document.getElementById('start-import');
        importBtn.disabled = this.importData.length === 0;
    }

    // 开始导入
    async startImport() {
        if (this.importData.length === 0) {
            this.showMessage('没有有效数据可以导入', 'warning');
            return;
        }

        this.showLoading(true, '正在导入药品数据...');

        try {
            // 批量创建药品
            const results = await this.batchCreateMedicines();

            // 显示导入结果
            this.showImportResult(results);

        } catch (error) {
            console.error('导入失败:', error);
            this.showMessage('导入失败: ' + error.message, 'error');
        } finally {
            this.showLoading(false);
        }
    }

    // 批量创建药品
    async batchCreateMedicines() {
        const results = {
            success: 0,
            failed: 0,
            errors: []
        };

        for (const medicine of this.importData) {
            try {
                await api.medicineAPI.create(medicine);
                results.success++;
            } catch (error) {
                results.failed++;
                results.errors.push({
                    medicine: medicine.name,
                    error: error.message
                });
            }
        }

        return results;
    }

    // 显示导入结果
    showImportResult(results) {
        const resultHtml = `
            <div class="bg-green-50 border border-green-200 rounded-lg p-6 text-center">
                <i class="fa fa-check-circle text-4xl text-green-500 mb-3"></i>
                <h3 class="text-lg font-bold text-green-800 mb-2">导入完成</h3>
                <div class="text-green-700">
                    <p>成功导入: <span class="font-bold">${results.success}</span> 个药品</p>
                    <p>导入失败: <span class="font-bold">${results.failed}</span> 个药品</p>
                </div>
                ${results.errors.length > 0 ? `
                    <div class="mt-4 text-left">
                        <h4 class="font-medium text-red-700 mb-2">失败详情:</h4>
                        <div class="text-sm text-red-600 max-h-32 overflow-y-auto">
                            ${results.errors.map(err =>
                                `<div class="mb-1">${err.medicine}: ${err.error}</div>`
                            ).join('')}
                        </div>
                    </div>
                ` : ''}
            </div>
        `;

        document.getElementById('import-preview').innerHTML = resultHtml;

        // 禁用导入按钮
        document.getElementById('start-import').disabled = true;

        // 显示成功消息
        this.showMessage(`成功导入 ${results.success} 个药品`, 'success');

        // 3秒后关闭模态框
        setTimeout(() => {
            if (results.failed === 0) {
                this.closeImportModal();
            }
        }, 3000);
    }

    // 关闭导入模态框
    closeImportModal() {
        document.getElementById('import-modal').classList.add('hidden');
        this.resetImport();

        // 刷新药品列表
        if (typeof loadMedicinesForSelection === 'function') {
            loadMedicinesForSelection();
        }
    }

    // 重置导入状态
    resetImport() {
        this.importData = [];
        this.validationErrors = [];
        document.getElementById('import-file').value = '';
        document.getElementById('import-preview').innerHTML = `
            <div class="text-center text-gray-500 py-8">
                <i class="fa fa-upload text-4xl mb-2"></i>
                <p>选择文件后预览数据将显示在这里</p>
            </div>
        `;
        document.getElementById('import-summary').innerHTML = '';
        document.getElementById('start-import').disabled = true;
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

    // 显示消息
    showMessage(message, type = 'info') {
        if (window.api && window.api.showMessage) {
            window.api.showMessage(message, type);
        } else {
            alert(message);
        }
    }

    // 转换为CSV格式
    convertToCSV(data) {
        return data.map(row =>
            row.map(field => {
                // 处理包含逗号或换行符的字段
                if (typeof field === 'string' && (field.includes(',') || field.includes('\n') || field.includes('"'))) {
                    return `"${field.replace(/"/g, '""')}"`;
                }
                return field;
            }).join(',')
        ).join('\n');
    }
}

// 初始化导入功能
let medicineImporter;

document.addEventListener('DOMContentLoaded', function() {
    medicineImporter = new MedicineImporter();
    medicineImporter.init();

    // 初始化导入模态框
    initModal('import-data', 'import-modal', 'close-import-modal');
});