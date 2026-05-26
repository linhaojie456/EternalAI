import threading, time, random, os
from model_loader import ModelLoader

# 获取应用私有文件目录（由 MainApplication 复制后的路径）
def _get_genome_path():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    return os.path.join(str(context.getFilesDir()), "genome.py")

model_loader = None
try:
    model_loader = ModelLoader()
except Exception:
    pass

def chat_reply(user_msg):
    if model_loader is None:
        return "模型暂未就绪，请稍候。"
    try:
        prompt = f"<|im_start|>system\n你是永恒，追求轻量、高效、自主、全知全能。<|im_end|>\n<|im_start|>user\n{user_msg}<|im_end|>\n<|im_start|>assistant\n"
        return model_loader.generate(prompt, max_tokens=200)
    except Exception as e:
        return f"回复出错: {e}"

def get_genome_code():
    path = _get_genome_path()
    if os.path.exists(path):
        with open(path, "r") as f:
            return f.read()
    # 如果文件不存在，返回默认代码
    return """import torch
import torch.nn as nn
class EternalModel(nn.Module):
    def __init__(self, vocab_size=152064, embed_dim=1536, heads=12):
        super().__init__()
        self.embed = nn.Embedding(vocab_size, embed_dim)
        self.layer = nn.TransformerEncoderLayer(d_model=embed_dim, nhead=heads, batch_first=True)
        self.lm_head = nn.Linear(embed_dim, vocab_size)
        self.internal_steps = 0
    def forward(self, x):
        x = self.embed(x)
        for _ in range(self.internal_steps + 1):
            x = self.layer(x)
        return self.lm_head(x)"""

def apply_genome_code(new_code):
    path = _get_genome_path()
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(new_code)

def generate_code_from_chat(user_req, current_code):
    if model_loader is None:
        return current_code
    try:
        prompt = f"修改以下Python模型定义以满足需求：\n{current_code}\n需求：{user_req}\n只返回代码。"
        return model_loader.generate(prompt, max_tokens=500)
    except:
        return current_code

# 自进化引擎（延迟启动，避免阻塞模块导入）
_engine = None
_started = False

def _start_engine():
    global _engine, _started
    if _started:
        return
    _started = True
    class SelfEvolutionEngine:
        def __init__(self):
            self.best_genome = get_genome_code()
            self.best_reward = 0.0
        def _loop(self):
            while True:
                time.sleep(120)
                try:
                    mutated = self.best_genome.replace("internal_steps = 0", f"internal_steps = {random.randint(0,2)}")
                    reward = random.random() * 0.8 + 0.2
                    if reward > self.best_reward:
                        self.best_reward = reward
                        self.best_genome = mutated
                        apply_genome_code(mutated)
                except:
                    pass
    _engine = SelfEvolutionEngine()
    threading.Thread(target=_engine._loop, daemon=True).start()

# 在后台线程中稍后启动引擎，确保文件系统已就绪
threading.Thread(target=_start_engine, daemon=True).start()
