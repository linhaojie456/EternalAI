import threading, time, random, os, ast
from tokenizers import Tokenizer

# 全局状态
_inference_engine = None
_tokenizer = None

def set_inference_engine(engine):
    global _inference_engine
    _inference_engine = engine

def _get_tokenizer():
    global _tokenizer
    if _tokenizer is None:
        from com.chaquo.python import Python
        context = Python.getPlatform().getApplication()
        model_dir = os.path.join(str(context.getFilesDir()), "model")
        _tokenizer = Tokenizer.from_file(os.path.join(model_dir, "tokenizer.json"))
    return _tokenizer

def chat_reply(user_msg):
    if _inference_engine is None:
        return "推理引擎未就绪"
    try:
        tokenizer = _get_tokenizer()
        encoded = tokenizer.encode(user_msg)
        input_ids = encoded.ids
        attention_mask = [1] * len(input_ids)

        # 调用 Kotlin 推理引擎
        result = _inference_engine.generate(input_ids, attention_mask, maxTokens=200)
        if result is not None:
            reply = tokenizer.decode(result)
            return reply
        else:
            return "推理失败"
    except Exception as e:
        return f"推理错误: {e}"

def get_genome_code():
    path = os.path.join(str(os.environ.get("FILES_DIR", "")), "genome.py")
    if os.path.exists(path):
        with open(path) as f: return f.read()
    return ""

def apply_genome_code(new_code):
    path = os.path.join(str(os.environ.get("FILES_DIR", "")), "genome.py")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: f.write(new_code)

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
