import threading, time, random, os, ast
from universe import make_universe

_inference_engine = None
def set_inference_engine(engine): global _inference_engine; _inference_engine = engine

def chat_reply(user_msg):
    if _inference_engine is None: return "推理引擎未就绪"
    try: reply = _inference_engine.generate(user_msg, 200); return reply if reply else "（推理失败）"
    except Exception as e: return f"推理错误: {e}"

def get_genome_code():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    path = os.path.join(str(context.getFilesDir()), "genome.py")
    if os.path.exists(path): return open(path, "r").read()
    return ""

def apply_genome_code(new_code):
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    path = os.path.join(str(context.getFilesDir()), "genome.py")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: f.write(new_code)

def check_genome_syntax(code):
    try: ast.parse(code); return "通过"
    except SyntaxError as e: return f"语法错误: {e}"

def evolve_step():
    global _inference_engine
    if _inference_engine is None: return
    try:
        current = get_genome_code()
        mutated = _inference_engine.generate(f"改进以下代码以追求轻量、高效、全知全能：\n{current}\n只返回代码。", 300)
        if mutated and len(mutated) > 20: apply_genome_code(mutated)
    except: pass

class SelfEvolutionEngine:
    def __init__(self): self.best_code = get_genome_code(); self.best_reward = 0.0
    def start(self):
        def loop():
            while True: time.sleep(60); evolve_step()
        threading.Thread(target=loop, daemon=True).start()

engine = SelfEvolutionEngine(); engine.start()
