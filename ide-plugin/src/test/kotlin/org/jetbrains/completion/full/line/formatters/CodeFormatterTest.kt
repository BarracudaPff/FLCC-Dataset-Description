package org.jetbrains.completion.full.line.formatters

import org.jetbrains.completion.full.line.FilesTest
import org.jetbrains.completion.full.line.IdeaTest
import org.jetbrains.completion.full.line.language.CodeFormatter

abstract class CodeFormatterTest(val formatter: CodeFormatter) : IdeaTest(), FilesTest
