from tokenizers import Tokenizer
from pathlib import Path

class TokenizerLoader:
    def __init__(self):
        model_dir = Path(__file__).parent.parent / "assets" / "model"
        self.tokenizer = Tokenizer.from_file(str(model_dir / "tokenizer.json"))

    def encode(self, text):
        return self.tokenizer.encode(text).ids

    def decode(self, ids):
        return self.tokenizer.decode(ids)
