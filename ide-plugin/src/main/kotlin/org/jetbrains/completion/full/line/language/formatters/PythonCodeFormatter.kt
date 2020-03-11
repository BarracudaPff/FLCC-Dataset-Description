package org.jetbrains.completion.full.line.language.formatters

import org.jetbrains.completion.full.line.language.CodeFormatter
import org.jetbrains.completion.full.line.language.formatters.python.*

class PythonCodeFormatter : CodeFormatter(arrayOf(
        WhitespaceFormatter()
        , NumericalFormatter()
        , ImportFormatter()
        , ParenthesizedWithoutTuplesFormatter()
        , StringFormatter()
        , ListLiteralFormatter()
        , ArgumentListFormatter()
        , SequenceFormatter()
        , ParameterListFormatter()
        , DumpFormatter()
))
