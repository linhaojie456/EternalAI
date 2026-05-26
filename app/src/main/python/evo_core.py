import threading, time, random, os
from model_loader import ModelLoader

model = ModelLoader()

def _get_genome_path():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    return os.path.join(str(context.getFilesDir()), "genome.py")

def chat_reply(user_msg):
    try:
        prompt = f"<|im_start|>system\n你是永恒。<|im_end|>\n<|im_start|>user\n{user_msg}<|im_end|>\n<|im_start|>assistant\n"
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
    with open(path, "w") as f: f.write(new_code)

# 自进化引擎（使用模型推理进行代码变异）
class SelfEvolutionEngine:
    def __init__(self):
        self.best_code = get_genome_code()
        self.best_reward = 0.0
    def start(self):
        def loop():
            while True:
                time.sleep(120)
                try:
                    # 调用模型生成变异代码
                    mutated = model.generate(f"改进以下代码：\n{self.best_code}\n需求：轻量高效", max_tokens=300)
                    if mutated:
                        self.best_code = mutated
                        apply_genome_code(mutated)
                except: pass
        threading.Thread(target=loop, daemon=True).start()

engine = SelfEvolutionEngine()
engine.start()
