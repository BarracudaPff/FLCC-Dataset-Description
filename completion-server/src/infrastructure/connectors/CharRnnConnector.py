import numpy as np
import torch
import torch.nn.functional as F

from src.infrastructure.charrnn.utils.utils import get_model_from_params, one_hot_encode


class CharRnnConnector:
    device = torch.device('cpu')

    # Distribution for branching answers.
    #
    # (Percent, Count, Branch Percent)
    distribution = [
        # (0.90, 1, None),
        # (0.80, 1, None),
        (0.70, 2, 0.20),
        (0.60, 2, 0.15),
        (0.50, 3, 0.15),
        (0.40, 4, 0.15),
        (0.30, 4, 0.30),
        (0.20, 4, 0.10),
        (0.10, 3, 0.10),
        (0.0 , 2, 0   ),
    ]
    # Special Character to split output. It's safe to use character if it's not in main vocabulary.
    # TODO Add check for non-entry into the dictionary
    sp_char = '⁂'

    def __init__(self, params_path: str, model_path: str, max_branches: int = 20, max_chars: int = 20):
        """
        One char predicts for 0.0016 sec, so 20 branches with 20 chars will be maximum 0.64 sec
        Be aware of getting more than 1.5 seconds, also every next branch will have less match percentage

        :param params_path: Path to parameters
        :param model_path: Path to Model data
        :param max_branches: Maximum amount of suggestions
        :param max_chars: Maximum amount of chars in one suggestion
        """
        self.max_branches = max_branches
        self.max_chars = max_chars
        char_rnn = get_model_from_params(params_path, model_path)
        char_rnn.to(self.device)
        char_rnn.eval()
        self.model = char_rnn

    def get_suggestions(self, prime: str, token: str) -> [str]:
        """
        Get suggestions from some prime string

        :param token: Token where complete was invoked
        :param prime: Use some string as initial input for prediction
        :return: Array of suggestions
        """
        # init prime string
        h = self.model.init_hidden(1)
        # chars = prime
        for code in prime:
            p, c, h = self._get_next_chars(code, 1, h)
        # predict
        input = token + c
        return self._recursive(h, input, True).split(self.sp_char)

    def _get_next_chars(self, char: str, top_k: int, h):
        """
        Get predictions for next character. You can control them with count `top_k` and input layer `h`

        :param char: Input character
        :param top_k: Number of results
        :param h: Input layer. Use **self.model.init_hidden(1)** if
        :return:
        """
        x = np.array([[self.model.char2int[char]]])
        x = one_hot_encode(x, len(self.model.chars))
        inputs = torch.from_numpy(x)
        h = tuple([each.data for each in h])

        out, h = self.model(inputs, h)

        p = F.softmax(out, dim=1).data
        p, top_ch = p.topk(top_k)
        p = p.numpy().squeeze().tolist()
        top_ch = top_ch.numpy().squeeze().tolist()

        if top_k == 1:
            chars = self.model.int2char[top_ch]
        else:
            chars = [self.model.int2char[char] for char in top_ch]
        return p, chars, h

    def _recursive(self, h, line: str, expand: bool):
        """
        Recursively get possible variants. Output line must be split by Special Character (sp_char).
        Possible upgrade is to use branch_percent

        :param h: Input layer for model based on previous inputs
        :param line: All previous predictions
        :param expand: Pass True if recursive function will create new branch. Otherwise False
        :return:
        """
        if expand:
            self.max_branches -= 1
        if line[-1] == '\n' or line[-1] == '\t':
            return line[:-1] + self.sp_char
        percents, chars, h = self._get_next_chars(line[-1], 6, h)

        # Check for > 80% Best solutions, intend even if should stop cause of max_branches or max_chars
        if percents[0] > 0.8:
            return self._recursive(h, line + chars[0], False)
        else:
            # Stop if branches more than max_branches.
            # Still, fill up to max characters
            if self.max_branches <= 0 or self.max_branches == -1:
                if len(line) < self.max_chars:
                    return self._recursive(h, line + chars[0], False)
                else:
                    return line + self.sp_char
            # Stop if too much characters
            if len(line) >= self.max_chars:
                return line + self.sp_char
            # Start searching from the top of distribution.
            for explanation in self.distribution:
                percent = explanation[0]
                count = explanation[1]
                # branch_percent = explanation[2]
                if percents[0] > percent:
                    res = ''
                    for num in range(count):
                        # if percents[num] > percent_branch:
                        res += self._recursive(h, line + chars[num], True)
                    return res
