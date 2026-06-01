import os
from tokenizers import Tokenizer

_tokenizer = None

def _load_tokenizer():
    global _tokenizer
    if _tokenizer is not None:
        return _tokenizer
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    model_dir = os.path.join(str(context.getFilesDir()), "model")
    _tokenizer = Tokenizer.from_file(os.path.join(model_dir, "tokenizer.json"))
    return _tokenizer

def encode(text):
    tok = _load_tokenizer()
    return tok.encode(text).ids

def decode(ids):
    tok = _load_tokenizer()
    return tok.decode(ids)

def eos_token_id():
    return 151643
