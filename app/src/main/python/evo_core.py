import threading, time, random, os, ast

_inference_engine = None

def set_inference_engine(engine):
    global _inference_engine
    _inference_engine = engine

def chat_reply(user_msg):
    if _inference_engine is None:
        return "推理引擎未就绪"
    try:
        reply = _inference_engine.generate(user_msg, 200)
        if reply is None:
            return "（推理失败: 模型未就绪）"
        if reply.strip() == "":
            return "（推理输出为空）"
        return reply
    except Exception as e:
        return f"推理错误: {e}"

def get_genome_code():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    path = os.path.join(str(context.getFilesDir()), "genome.py")
    if os.path.exists(path):
        with open(path, "r") as f:
            return f.read()
    return ""

def apply_genome_code(new_code):
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    path = os.path.join(str(context.getFilesDir()), "genome.py")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(new_code)

def generate_code_from_chat(user_req, current_code):
    if _inference_engine is None:
        return [current_code, "推理引擎未就绪"]
    prompt = f"修改以下Python模型定义以满足需求：\n{current_code}\n需求：{user_req}\n只返回代码。"
    try:
        new_code = _inference_engine.generate(prompt, 500)
        if new_code is None:
            return [current_code, "模型未加载，无法生成"]
        if len(new_code.strip()) < 10:
            return [current_code, f"生成内容过短: {new_code}"]
        return [new_code, None]  # 成功，无错误
    except Exception as e:
        return [current_code, f"生成异常: {e}"]

def check_genome_syntax(code):
    try:
        ast.parse(code)
        return "通过"
    except SyntaxError as e:
        return f"语法错误: {e}"

class SelfEvolutionEngine:
    def __init__(self):
        self.best_code = get_genome_code()
        self.best_reward = 0.0
    def start(self):
        def loop():
            while True:
                time.sleep(60)
                try:
                    mutated = chat_reply(f"改进代码: {self.best_code[:50]}...")
                    if not mutated or "错误" in mutated: continue
                    reward = random.random()
                    if reward > self.best_reward:
                        self.best_reward = reward
                        self.best_code = mutated
                        apply_genome_code(mutated)
                except:
                    pass
        threading.Thread(target=loop, daemon=True).start()

engine = SelfEvolutionEngine()
engine.start()
