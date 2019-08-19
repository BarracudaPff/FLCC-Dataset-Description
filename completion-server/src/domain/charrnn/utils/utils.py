import json

import numpy as np
import torch

from src.domain.charrnn.model import CharRNN

device = torch.device('cpu')


# TODO (Implement method)
def getOldPrefix(code, cursor_pos) -> (int, int):
    return 1, 2


def get_model_from_params(params_path: str, model_path: str) -> CharRNN:
    """
    Load model and parameters from files

    :param params_path: Path to parameters.
    :param model_path: Path to Model data
    :return: CharRNN model
    """

    # read parameters
    with open(params_path, mode='r') as f:
        params = json.load(f)
    # create and load model
    model = CharRNN(tokens=params['chars'],
                    device=torch.device('cpu'),
                    n_hidden=params['n_hidden'],
                    n_layers=params['n_layers'],
                    drop_prob=params['drop_prob'],
                    lr=params['lr'])
    model.load_state_dict(torch.load(model_path, map_location='cpu'))
    model.to(device)
    model.eval()
    return model


def one_hot_encode(arr, n_labels):
    """
    Simple implementation of One-Hot-Encode
    """
    one_hot = np.zeros((np.multiply(*arr.shape), n_labels), dtype=np.float32)
    one_hot[np.arange(one_hot.shape[0]), arr.flatten()] = 1.
    one_hot = one_hot.reshape((*arr.shape, n_labels))
    return one_hot
