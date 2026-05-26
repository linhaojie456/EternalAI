# 此文件不再负责推理，推理已移至 Kotlin 层
# 保留 tokenizer 功能以备后用
from tokenizers import Tokenizer
from pathlib import Path
import os

class TokenizerLoader:
    def __init__(self):
        model_dir = str(Path(__file__).parent.parent / "assets" / "model")
        self.tokenizer = Tokenizer.from_file(os.path.join(model_dir, "tokenizer.json"))

    def encode(self, text):
        return self.tokenizer.encode(text).ids

    def decode(self, ids):
        return self.tokenizer.decode(ids)
