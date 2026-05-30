import threading, time, random, os, ast

_inference_engine = None

def set_inference_engine(engine):
    global _inference_engine
    _inference_engine = engine

def chat_reply(user_msg):
    if _inference_engine is None:
        return "推理引擎未就绪"
    try:
        reply = _inference_engine.generate(user_msg, maxTokens=200)
        return reply if reply else "（模型返回空）"
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
        return current_code
    prompt = f"修改以下Python模型定义以满足需求：\n{current_code}\n需求：{user_req}\n只返回代码。"
    try:
        new_code = _inference_engine.generate(prompt, maxTokens=500)
        return new_code if new_code else current_code
    except:
        return current_code

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
                    mutated = chat_reply(f"改进以下代码以追求轻量、高效、全知全能：\n{self.best_code}\n只返回代码。")
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
