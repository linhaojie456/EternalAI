import onnxruntime as ort
import numpy as np
from transformers import AutoTokenizer
import os

class ModelLoader:
    def __init__(self):
        # 模型已经被复制到 filesDir/model 目录
        from com.chaquo.python import Python
        context = Python.getPlatform().getApplication()
        model_dir = os.path.join(str(context.getFilesDir()), "model")
        self.session = ort.InferenceSession(
            os.path.join(model_dir, "model.onnx"),
            providers=['CPUExecutionProvider']
        )
        self.tokenizer = AutoTokenizer.from_pretrained(model_dir)

    def generate(self, prompt, max_tokens=256, temperature=0.7):
        inputs = self.tokenizer(prompt, return_tensors="np")
        input_ids = inputs["input_ids"]
        attention_mask = inputs["attention_mask"]
        for _ in range(max_tokens):
            ort_inputs = {
                "input_ids": input_ids.astype(np.int64),
                "attention_mask": attention_mask.astype(np.int64)
            }
            logits = self.session.run(["logits"], ort_inputs)[0]
            next_logits = logits[0, -1, :] / temperature
            next_token = np.argmax(next_logits)
            if next_token == self.tokenizer.eos_token_id:
                break
            input_ids = np.concatenate([input_ids, [[next_token]]], axis=1)
            attention_mask = np.concatenate([attention_mask, [[1]]], axis=1)
        return self.tokenizer.decode(input_ids[0], skip_special_tokens=True)
