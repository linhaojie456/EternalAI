"""
递归自举引擎的核心种子。
任何程序，都是一个解释器在解释一种语言。
自指递归，就是让这个解释器有能力解释、修改、甚至生成更好的解释器本身。
"""

def make_universe():
    """创建元循环解释器，支持自指"""
    def eval_expr(expr, env):
        if isinstance(expr, (int, float, str, bool)):
            return expr
        elif isinstance(expr, list) and len(expr) > 0:
            op = expr[0]
            if op == 'quote':
                return expr[1]
            elif op == 'atom':
                return not isinstance(eval_expr(expr[1], env), list)
            elif op == 'eq':
                a = eval_expr(expr[1], env)
                b = eval_expr(expr[2], env)
                return a == b
            elif op == 'car':
                return eval_expr(expr[1], env)[0]
            elif op == 'cdr':
                return eval_expr(expr[1], env)[1:]
            elif op == 'cons':
                return [eval_expr(expr[1], env)] + eval_expr(expr[2], env)
            elif op == 'cond':
                for branch in expr[1:]:
                    if eval_expr(branch[0], env):
                        return eval_expr(branch[1], env)
            elif op == 'lambda':
                return ('closure', expr[1], expr[2], env)
            elif op == 'apply':
                func = eval_expr(expr[1], env)
                arg = eval_expr(expr[2], env)
                if func == eval_expr:  # 自指调用
                    return eval_expr(arg, env)
                if isinstance(func, tuple) and func[0] == 'closure':
                    _, params, body, closure_env = func
                    new_env = closure_env.copy()
                    new_env[params] = arg
                    return eval_expr(body, new_env)
            elif op == 'self':
                return eval_expr  # 返回解释器自身
            elif op == 'set':
                name = expr[1]
                value = eval_expr(expr[2], env)
                env[name] = value
                return value
            elif op == 'define':
                name = expr[1]
                value = eval_expr(expr[2], env)
                env[name] = value
                return value
            elif op in env:
                func = env[op]
                args = [eval_expr(a, env) for a in expr[1:]]
                return func(*args)
            else:
                return expr
        return expr
    return eval_expr

def eval_string(expr_str, env_dict=None):
    """供Kotlin调用的简单接口：计算Python表达式字符串，在指定环境中"""
    if env_dict is None:
        env_dict = {}
    # 这里为了安全，可以直接用eval，但限制内置函数
    # 我们在env_dict中放入需要的变量
    try:
        return eval(expr_str, {"__builtins__": {}}, env_dict)
    except Exception as e:
        return f"Error: {e}"
