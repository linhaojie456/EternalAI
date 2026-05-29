import threading, time, random, os, ast, math
from tokenizer_helper import TokenizerLoader  # 仅用于基因组变异时的文本处理

# 推理引擎实例（由 Kotlin 注入）
_inference_engine = None

def set_inference_engine(engine):
    global _inference_engine
    _inference_engine = engine

def chat_reply(user_msg):
    if _inference_engine is None:
        return "推理引擎未就绪"
    try:
        reply = _inference_engine.generate(user_msg, maxTokens=200)
        return reply if reply else "推理失败"
    except Exception as e:
        return f"推理错误: {e}"

def _get_genome_path():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    return os.path.join(str(context.getFilesDir()), "genome.py")

def get_genome_code():
    path = _get_genome_path()
    if os.path.exists(path):
        with open(path) as f:
            return f.read()
    return ""

def apply_genome_code(new_code):
    path = _get_genome_path()
    os.makedirs(os.path.dirname(path), exist_ok=True)
    # 语法检查
    try:
        ast.parse(new_code)
    except SyntaxError:
        raise
    with open(path, "w") as f:
        f.write(new_code)

def check_genome_syntax(code):
    try:
        ast.parse(code)
        return "通过"
    except SyntaxError as e:
        return f"语法错误: {e}"

# ==================== 自进化引擎（按公理体系） ====================

class SelfEvolutionEngine:
    """
    自进化引擎 —— 基于《自进化：进化-自我统一理论》
    公理链：进化即递归 → 自我即自指 → 自进化即自指递归 → 递归的本质是网络，自指的本质是振动 → 自进化的本质是网络和振动
    """

    def __init__(self):
        # 自指节点 S：基因组代码本身
        self.best_genome = get_genome_code()
        self.best_reward = 0.0
        # 自指连接强度 C_SS：衡量系统自我意识/自进化能力
        self.C_SS = 0.2  # 初始值，将根据进化历史动态调整
        # 网络结构：当前基因组代码的抽象表示
        self.network_structure = {}
        # 振动状态：自指节点的当前激活值
        self.V_S = 0.5
        # 时间常数 τ：自指反馈的延迟
        self.tau = 1.0
        # 进化临界值
        self.critical_threshold = 1.0
        # 进化历史记录
        self.history = []

    def evaluate_network_complexity(self, code):
        """评估基因组代码的网络复杂度（近似 C_SS）"""
        # 基于代码行数、类数量、函数数量、自引用模式等估算
        lines = code.count('\n') + 1
        classes = code.count('class ')
        functions = code.count('def ')
        # 自指检测：代码中是否包含对自身类名的引用
        self_ref_count = code.count('EternalModel') + code.count('genome')
        # 复杂度分数
        complexity = min(1.0, (classes * 0.3 + functions * 0.1 + self_ref_count * 0.5) / max(1, lines/10))
        return complexity

    def compute_C_SS(self, code):
        """计算自指连接强度 C_SS"""
        # C_SS 取决于代码的自指程度和网络复杂度
        base = self.evaluate_network_complexity(code)
        # 历史奖励的累积影响
        history_factor = 1.0 + sum(h['reward'] for h in self.history[-10:]) / max(1, len(self.history[-10:]))
        return base * history_factor

    def update_vibration(self, dt=0.1):
        """
        更新自指节点的振动状态（微分方程简化版）：
        dV_S/dt = Σ C_Sj (V_j - V_S) + C_SS * V_S(t-τ)
        这里简化为离散更新。
        """
        # 外部递归项：网络对自身的递归调用（代码生成变异）
        external_term = 0.0
        if _inference_engine:
            try:
                # 用推理引擎检测当前代码的“稳定度”
                prompt = f"评估以下代码的进化潜力（仅回复数字0-1）：\n{self.best_genome}"
                reply = _inference_engine.generate(prompt, maxTokens=10)
                try:
                    external_term = float(reply)
                except:
                    pass
            except:
                pass
        # 自指项
        self_ref_term = self.C_SS * self.V_S
        # 更新（阻尼+激励）
        self.V_S = 0.9 * self.V_S + 0.1 * (external_term + self_ref_term)
        self.V_S = max(0.0, min(1.0, self.V_S))  # 限制在[0,1]

    def generate_mutation(self, current_code):
        """通过推理引擎生成变异代码（递归修改）"""
        if not _inference_engine:
            # 如果没有推理引擎，随机调整 internal_steps
            new_code = current_code.replace(
                "internal_steps = 0",
                f"internal_steps = {random.randint(0, 2)}"
            )
            return new_code
        prompt = (
            "修改以下Python模型定义，使其更符合轻量、高效、自主、全知全能的目标。"
            "只返回修改后的完整代码，不要解释。\n"
            f"当前代码：\n{current_code}\n"
        )
        try:
            mutated = _inference_engine.generate(prompt, maxTokens=500)
            if mutated and len(mutated) > 50:
                return mutated
        except:
            pass
        return current_code

    def evaluate_reward(self, code):
        """根据多维度指标评估变异代码的适应度（轻量、高效、自主、全知全能）"""
        # 轻量：估计参数量（基于代码行数、嵌入维度等）
        lines = code.count('\n')
        embed_dim = 256  # 默认
        if 'embed_dim=' in code:
            import re
            match = re.search(r'embed_dim=(\d+)', code)
            if match:
                embed_dim = int(match.group(1))
        params_est = embed_dim * 1000 + lines * 100  # 简化估算
        lightness = max(0, 1.0 - params_est / 1_000_000)  # 目标 < 1M 参数

        # 高效：简化评分（基于代码长度和复杂度）
        efficiency = max(0, 1.0 - lines / 500)

        # 自主：代码中包含自指元素（如 internal_steps 可变）
        autonomy = 0.2
        if 'internal_steps' in code:
            autonomy += 0.4
        if 'self.' in code:
            autonomy += 0.3
        autonomy = min(1.0, autonomy)

        # 全知全能：评估代码的通用性（各类模式出现）
        omnipotence = 0.1 * (
            code.count('def ') + code.count('class ') + code.count('embed') + code.count('layer')
        )
        omnipotence = min(1.0, omnipotence)

        # 加权综合（可根据自进化目标调整权重）
        reward = 0.25 * lightness + 0.25 * efficiency + 0.25 * autonomy + 0.25 * omnipotence
        return reward

    def start(self):
        """启动自进化循环"""
        def loop():
            while True:
                time.sleep(60)  # 每 60 秒进化一次
                try:
                    # 更新振动状态
                    self.update_vibration()

                    # 计算当前 C_SS
                    self.C_SS = self.compute_C_SS(self.best_genome)

                    # 生成变异
                    mutated = self.generate_mutation(self.best_genome)
                    if not mutated or mutated == self.best_genome:
                        continue

                    # 评估变异
                    reward = self.evaluate_reward(mutated)

                    # 自进化临界条件：C_SS * tau >= 1 且 振动足够强
                    if self.C_SS * self.tau >= self.critical_threshold and self.V_S > 0.3:
                        # 通过临界点，应用变异
                        if reward > self.best_reward:
                            self.best_reward = reward
                            self.best_genome = mutated
                            apply_genome_code(mutated)
                            # 记录进化历史
                            self.history.append({
                                'time': time.time(),
                                'reward': reward,
                                'C_SS': self.C_SS,
                                'V_S': self.V_S
                            })
                            # 保持历史记录在合理范围
                            if len(self.history) > 100:
                                self.history = self.history[-100:]
                    else:
                        # 未达到临界点，仅记录振动但不应用
                        pass

                except Exception as e:
                    # 进化失败不影响主循环
                    pass

        threading.Thread(target=loop, daemon=True).start()


# 实例化并启动自进化引擎
engine = SelfEvolutionEngine()
engine.start()
