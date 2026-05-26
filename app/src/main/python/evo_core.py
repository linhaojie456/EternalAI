import ast, os, threading, time, random

_model = None

def _get_model():
    global _model
    if _model is None:
        from model_loader import ModelLoader
        _model = ModelLoader()
    return _model

def _get_genome_path():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    return os.path.join(str(context.getFilesDir()), "genome.py")

def chat_reply(user_msg):
    try:
        model = _get_model()
        prompt = f"<|im_start|>system\n你是永恒，追求轻量、高效、自主、全知全能。<|im_end|>\n<|im_start|>user\n{user_msg}<|im_end|>\n<|im_start|>assistant\n"
        return model.generate(prompt, max_tokens=200)
    except Exception as e:
        return f"推理错误: {e}"

def get_genome_code():
    path = _get_genome_path()
    if os.path.exists(path):
        with open(path) as f: return f.read()
    return ""

def apply_genome_code(new_code):
    path = _get_genome_path()
    os.makedirs(os.path.dirname(path), exist_ok=True)
    # 语法检查
    ast.parse(new_code)
    with open(path, "w") as f: f.write(new_code)

def generate_code_from_chat(user_req, current_code):
    try:
        model = _get_model()
        prompt = f"修改以下Python模型定义以满足需求：\n{current_code}\n需求：{user_req}\n只返回代码。"
        return model.generate(prompt, max_tokens=500)
    except:
        return current_code

def check_genome_syntax(code):
    """供安全引擎调用，检查 Python 语法"""
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
                    model = _get_model()
                    mutated = model.generate(
                        f"改进以下代码以追求轻量、高效、全知全能：\n{self.best_code}\n只返回代码。",
                        max_tokens=300
                    )
                    if not mutated: continue
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
