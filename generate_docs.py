import os
from zhipuai import ZhipuAI  # 智谱AI的SDK

# 1. 配置大模型（替换为你的智谱API密钥）
client = ZhipuAI(api_key="48e6eb4334ef444a803076c6dc992607.NbXlsBiuFvuIM684")  # 这里填你从智谱控制台获取的密钥

# 2. 配置路径（根据你的项目修改）
CODE_DIR = "C:/Users/Administrator/Desktop/springboot-main"  # 当前目录（springboot项目根目录）
DOCS_DIR = "C:/Users/Administrator/Desktop/springboot-main/auto_docs"  # 生成的文档存放在项目内的auto_docs文件夹
os.makedirs(DOCS_DIR, exist_ok=True)  # 自动创建文档目录

# 3. 定义要处理的文件类型（SpringBoot项目主要是.java和.xml）
INCLUDE_EXTENSIONS = (".java", ".xml")
# 4. 遍历代码文件，生成文档
for root, dirs, files in os.walk(CODE_DIR):
    # 跳过不需要处理的目录（比如编译后的target文件夹）
    if "target" in dirs:
        dirs.remove("target")  # 不遍历target目录
    if "node_modules" in dirs:
        dirs.remove("node_modules")

    for file in files:
        # 只处理指定类型的文件
        if file.endswith(INCLUDE_EXTENSIONS):
            file_path = os.path.join(root, file)
            try:
                # 读取代码内容
                with open(file_path, "r", encoding="utf-8") as f:
                    code_content = f.read()

                # 调用大模型生成文档（提示词很重要，决定文档质量）
                prompt = f"""
                请为以下Java代码生成专业文档，格式为Markdown，包含：
                1. 文件概述：所属模块、设计目的；
                2. 核心类：类的继承关系、注解含义（如@Controller、@Service）；
                3. 方法详情：每个public方法的作用、参数类型/约束、返回值、抛出的异常；
                4. 业务逻辑：关键流程的说明（比如登录流程的步骤）；
                5. 依赖说明：引用的其他类、配置文件或第三方库。


                代码文件路径：{file_path}
                代码内容：
                {code_content}
                """

                # 调用智谱GLM-4模型（如果用OpenAI，替换为对应的调用方式）
                response = client.chat.completions.create(
                    model="glm-4",  # 智谱的大模型名称
                    messages=[{"role": "user", "content": prompt}]
                )
                doc_content = response.choices[0].message.content

                # 保存文档（保持和代码相同的目录结构）
                relative_path = os.path.relpath(file_path, CODE_DIR)
                doc_path = os.path.join(DOCS_DIR, relative_path).replace(".java", ".md").replace(".xml", ".md")
                os.makedirs(os.path.dirname(doc_path), exist_ok=True)  # 创建父目录
                with open(doc_path, "w", encoding="utf-8") as f:
                    f.write(doc_content)

                print(f"✅ 已生成文档：{doc_path}")
            except Exception as e:
                print(f"❌ 处理{file_path}失败：{str(e)}")

print("所有文档生成完成！文档在：" + os.path.abspath(DOCS_DIR))