import threading, time, random, os, ast
from universe import make_universe

# 推理引擎实例（由 Kotlin 注入）
_inference_engine = None

def set_inference_engine(engine):
    global _inference_engine
    _inference_engine = engine

def chat_reply(user_msg):
    if _inference_engine is None:
        return "推理引擎未就绪"
    try:
        reply = _inference_engine.generate(user_msg, 200)
        return reply if reply else "（推理失败）"
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

def check_genome_syntax(code):
    try:
        ast.parse(code)
        return "通过"
    except SyntaxError as e:
        return f"语法错误: {e}"

# 自进化引擎：基于元解释器，让系统能够自己编写代码
class SelfEvolutionEngine:
    def __init__(self):
        self.universe = make_universe()
        self.best_code = get_genome_code()
        self.best_reward = 0.0

    def evolve_step(self):
        """使用元解释器生成代码变异"""
        env = {'get_code': lambda: self.best_code,
               'apply_code': apply_genome_code,
               'infer': lambda p: _inference_engine.generate(p, 500) if _inference_engine else ""}
        # 用元解释器执行一个自指进化程序
        # 具体表达式可以逐步扩展，目前先注入推理引擎
        try:
            # 简化的自进化指令：调用推理引擎生成改进代码
            mutated = _inference_engine.generate(
                f"改进以下代码以追求轻量、高效、全知全能：\n{self.best_code}\n只返回代码。",
                maxTokens=300
            ) if _inference_engine else None
            if mutated and len(mutated) > 20:
                reward = random.random()
                if reward > self.best_reward:
                    self.best_reward = reward
                    self.best_code = mutated
                    apply_genome_code(mutated)
        except:
            pass

    def start(self):
        def loop():
            while True:
                time.sleep(60)
                self.evolve_step()
        threading.Thread(target=loop, daemon=True).start()

engine = SelfEvolutionEngine()
engine.start()
