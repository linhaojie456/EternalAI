import threading, time, random, os
from model_loader import ModelLoader

model = ModelLoader()

def _get_genome_path():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    return os.path.join(str(context.getFilesDir()), "genome.py")

def chat_reply(user_msg):
    try:
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
    with open(path, "w") as f: f.write(new_code)

def generate_code_from_chat(user_req, current_code):
    # 让模型生成代码修改
    prompt = f"修改以下Python模型定义以满足需求：\n{current_code}\n需求：{user_req}\n只返回代码。"
    try:
        return model.generate(prompt, max_tokens=500)
    except:
        return current_code

# 增强自进化引擎：衡量轻量（参数量）、高效（推理速度）、全知全能（探测数据集准确率）
class SelfEvolutionEngine:
    def __init__(self):
        self.best_code = get_genome_code()
        self.best_reward = 0.0
        self.target_params = 500000  # 目标参数量
        self.target_speed = 0.1      # 目标推理时间(秒)

    def evaluate(self, code):
        # 模拟评估（实际应加载模型测量）
        params = random.randint(300000, 700000)
        speed = random.uniform(0.05, 0.2)
        # 轻量得分
        L = max(0, 1 - abs(params - self.target_params) / self.target_params)
        # 高效得分
        E = max(0, 1 - speed / self.target_speed)
        # 全知全能得分（模拟）
        O = random.random()
        return 0.3*L + 0.25*E + 0.25*O + 0.2*random.random()

    def start(self):
        def loop():
            while True:
                time.sleep(60)  # 每 60 秒进化一次
                try:
                    # 生成变异代码
                    mutated = model.generate(
                        f"改进以下代码以追求轻量、高效、全知全能：\n{self.best_code}\n只返回代码。",
                        max_tokens=300
                    )
                    if not mutated: continue
                    reward = self.evaluate(mutated)
                    if reward > self.best_reward:
                        self.best_reward = reward
                        self.best_code = mutated
                        apply_genome_code(mutated)
                except Exception as e:
                    pass
        threading.Thread(target=loop, daemon=True).start()

engine = SelfEvolutionEngine()
engine.start()
