import tokenize
from abc import ABC, abstractmethod

import libcst as cst


class ConverterBase(ABC):

    @abstractmethod
    def convert_file(self, filename):
        pass


class CommentsConverter(ConverterBase):

    def convert_file(self, filename):
        result = ""
        source = open(filename, 'r')
        tokgen = tokenize.generate_tokens(source.readline)

        last_col = 0
        last_lineno = -1
        skip_new_line = False

        for toktype, ttext, (slineno, scol), (elineno, ecol), ltext in tokgen:
            if slineno > last_lineno:
                last_col = 0
            if not toktype == tokenize.COMMENT and scol > last_col:
                result += " " * (scol - last_col)
            if toktype == tokenize.COMMENT:
                assert slineno == elineno
                if ttext.strip() == ltext.strip():
                    skip_new_line = True
            else:
                if skip_new_line:
                    skip_new_line = False
                else:
                    result += ttext
            last_col = ecol
            last_lineno = elineno
        source.close()
        source = open(filename, 'w')
        source.write(result)


class CSTConverter(ConverterBase):
    modes = ["i", "c", "ic", "ci"]

    class ImportsTTransformer(cst.CSTTransformer):
        def leave_Import(self, original_node, updated_node):
            return cst.RemoveFromParent()

        def leave_ImportFrom(self, original_node, updated_node):
            return cst.RemoveFromParent()

    class CommentsTTransformer(cst.CSTTransformer):
        def leave_Comment(self, original_node, updated_node):
            return cst.RemoveFromParent()

    class ImportsCommentsTTransformer(ImportsTTransformer):
        def leave_Comment(self, original_node, updated_node):
            return cst.RemoveFromParent()

    def __init__(self, mode):
        super().__init__()
        assert mode in self.modes
        if mode == 'i':
            self.visitor = self.ImportsTTransformer()
        elif mode == 'c':
            self.visitor = self.CommentsTTransformer()
        else:
            self.visitor = self.ImportsCommentsTTransformer()

    def convert_file(self, filename):
        source = open(filename, 'r')
        code = source.read()

        tree = cst.parse_module(code)
        new_code = tree.visit(self.visitor).code.lstrip()

        source.close()
        source = open(filename, 'w')
        source.write(new_code)
