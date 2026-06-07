def make_universe():
    def eval_expr(expr, env):
        if isinstance(expr, (int, float, str, bool)): return expr
        if isinstance(expr, list) and len(expr) > 0:
            op = expr[0]
            if op == 'quote': return expr[1]
            elif op == 'atom': return not isinstance(eval_expr(expr[1], env), list)
            elif op == 'eq': return eval_expr(expr[1], env) == eval_expr(expr[2], env)
            elif op == 'lambda': return ('closure', expr[1], expr[2], env)
            elif op == 'apply':
                func = eval_expr(expr[1], env); arg = eval_expr(expr[2], env)
                if func == eval_expr: return eval_expr(arg, env)
                if isinstance(func, tuple) and func[0] == 'closure':
                    _, params, body, closure_env = func; new_env = closure_env.copy(); new_env[params] = arg; return eval_expr(body, new_env)
            elif op == 'self': return eval_expr
            elif op == 'set': env[expr[1]] = eval_expr(expr[2], env); return env[expr[1]]
            elif op == 'define': env[expr[1]] = eval_expr(expr[2], env); return env[expr[1]]
            elif op in env: func = env[op]; args = [eval_expr(a, env) for a in expr[1:]]; return func(*args)
        return expr
    return eval_expr

def eval_string(expr_str, env_dict=None):
    if env_dict is None: env_dict = {}
    try: return eval(expr_str, {"__builtins__": {}}, env_dict)
    except Exception as e: return f"Error: {e}"
