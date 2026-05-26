import torch
import torch.nn as nn

class EternalModel(nn.Module):
    def __init__(self, vocab_size=32000, embed_dim=256, heads=4):
        super().__init__()
        self.embed = nn.Embedding(vocab_size, embed_dim)
        self.layer = nn.TransformerEncoderLayer(d_model=embed_dim, nhead=heads, batch_first=True)
        self.lm_head = nn.Linear(embed_dim, vocab_size)
        self.internal_steps = 0

    def forward(self, x):
        x = self.embed(x)
        for _ in range(self.internal_steps + 1):
            x = self.layer(x)
        return self.lm_head(x)
