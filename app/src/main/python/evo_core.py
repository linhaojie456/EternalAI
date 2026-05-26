import os

def _get_genome_path():
    from com.chaquo.python import Python
    context = Python.getPlatform().getApplication()
    return os.path.join(str(context.getFilesDir()), "genome.py")

def get_genome_code():
    path = _get_genome_path()
    if os.path.exists(path):
        with open(path, "r") as f:
            return f.read()
    return "internal_steps = 0"

def apply_genome_code(new_code):
    path = _get_genome_path()
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(new_code)

# 自进化相关函数已移走，保留以上接口供开发模式使用
