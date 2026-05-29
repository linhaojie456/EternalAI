from tokenizers import Tokenizer
import os

def get_tokenizer():
    """加载 tokenizer，通过 Chaquopy 获取正确路径"""
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    model_dir = os.path.join(str(context.getFilesDir()), "model")
    tokenizer = Tokenizer.from_file(os.path.join(model_dir, "tokenizer.json"))
    return tokenizer

def encode(text):
    tok = get_tokenizer()
    return tok.encode(text).ids

def decode(ids):
    tok = get_tokenizer()
    return tok.decode(ids)

def eos_token_id():
    tok = get_tokenizer()
    # tokenizers 库中，EOS token 通常对应 eos_token_id 属性
    # 对于 Qwen/DeepSeek 模型，EOS token ID 可能是 151643
    return getattr(tok, 'eos_token_id', 151643)
