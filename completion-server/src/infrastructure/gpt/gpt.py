"""Generate samples from a model.

Note: only works for BPE-based models.
Based on https://github.com/huggingface/pytorch-pretrained-BERT/blob/master/examples/run_gpt2.py
"""
import argparse
import os
from typing import List

import torch
import torch.nn.functional as F
import tqdm

from pytorch_pretrained_bert import GPT2Tokenizer


class Parameters:
    def __init__(self, length: int, batch_size: int, max_context: int, temperature: float = 1.0, top_k: int = 0,
                 top_p: float = 0.0) -> None:
        super().__init__()
        self.length = length
        self.batch_size = batch_size
        self.max_context = max_context
        self.temperature = temperature
        self.top_k = top_k
        self.top_p = top_p


class GPTModel:

    def __init__(self, device, model, tokenizer, parameters: Parameters) -> None:
        super().__init__()
        self.__device = device
        self.__model = model
        self.__tokenizer = tokenizer
        self.__parameters = parameters

    def predict(self, context: str) -> List[str]:
        ## Init
        device, model, tokenizer = self.__device, self.__model, self.__tokenizer
        params = self.__parameters
        NL = self.__tokenizer.encode('\n')
        data = torch.tensor(NL * 4 + self.__tokenizer.encode(context)).to(device)
        # Turn into a batch.
        data.unsqueeze_(1)
        data = data.repeat_interleave(params.batch_size, dim=1)

        if not hasattr(model, 'init_mems'):
            model = model.module
        mems = model.init_mems()

        for i in tqdm.trange(params.length):
            ## Grab a sample from the last frame, append to result list, append to `data`
            pred_hid, mems = predict(model, data[-params.max_context:] if i == 0 else data[-1:], mems)
            softmax = hidden_to_softmax(model, pred_hid[-1], top_k=params.top_k, temperature=params.temperature,
                                        top_p=params.top_p)

            new_sample = torch.multinomial(softmax, num_samples=1).unsqueeze(-1).squeeze(2)
            data = torch.cat((data, new_sample.t()), dim=0)

        result = set()
        for i in range(data.size(1)):
            print('=' * 40, 'sample', i + 1, '=' * 40)
            # Chop off the newlines before printing
            res: str = tokenizer.decode(data[4:, i].tolist())
            for line in res.splitlines():
                line = line.strip('\n\t ')
                if len(line) != 0:
                    print(line)
                    result.add(line)

        return list(result)


def init_model(model_path: str, params: Parameters) -> GPTModel:
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

    # Load the best saved model.
    with open(os.path.join(model_path), 'rb') as f:
        model = torch.load(f, map_location='cuda' if torch.cuda.is_available() else 'cpu')

    if not torch.cuda.is_available():
        model = model.float()

    tokenizer = GPT2Tokenizer.from_pretrained('gpt2')

    model = model.to(device)
    model.eval()
    return GPTModel(device, model, tokenizer, params)


def predict(model, data, mems):
    tgt_len = data.size(0)
    with torch.no_grad():
        hidden, new_mems = model._forward(data, mems=mems)
    pred_hid = hidden[-tgt_len:]
    return pred_hid, new_mems


def hidden_to_softmax(model, hidden, temperature: float = 1, top_k: int = 0, top_p: float = 0):
    """Turn a hidden projection into log softmax.

    Adapted from utils/proj_adaptive_softmax.py
    """
    # pas stands for ProjectedAdaptiveSoftmax
    pas = model.crit
    logits = pas._compute_logit(hidden, pas.out_layers[0].weight,
                                pas.out_layers[0].bias, pas.out_projs[0])
    logits = top_k_top_p_filtering(logits, top_k=top_k, top_p=top_p)

    logits /= temperature
    softmax = F.softmax(logits, dim=-1)
    return softmax


def top_k_top_p_filtering(logits, top_k=0, top_p=0.0, filter_value=-float('Inf')):
    """ Filter a distribution of logits using top-k and/or nucleus (top-p) filtering

    https://gist.github.com/thomwolf/1a5a29f6962089e871b94cbd09daf317

        Args:
            logits: logits distribution shape (..., vocabulary size)
            top_k >0: keep only top k tokens with highest probability (top-k filtering).
            top_p >0.0: keep the top tokens with cumulative probability >= top_p (nucleus filtering).
    """
    top_k = min(top_k, logits.size(-1))  # Safety check
    if top_k > 0:
        # Remove all tokens with a probability less than the last token of the top-k
        indices_to_remove = logits < torch.topk(logits, top_k)[0][..., -1, None]
        logits[indices_to_remove] = filter_value

    if top_p > 0.0:
        sorted_logits, sorted_indices = torch.sort(logits, descending=True)
        cumulative_probs = torch.cumsum(F.softmax(sorted_logits, dim=-1), dim=-1)

        # Remove tokens with cumulative probability above the threshold
        sorted_indices_to_remove = cumulative_probs >= top_p
        # Shift the indices to the right to keep also the first token above the threshold
        sorted_indices_to_remove[..., 1:] = sorted_indices_to_remove[..., :-1].clone()
        sorted_indices_to_remove[..., 0] = 0

        indices_to_remove = torch.zeros_like(logits, dtype=torch.uint8).scatter_(
            dim=-1, index=sorted_indices, src=sorted_indices_to_remove)
        logits[indices_to_remove] = filter_value
    return logits
