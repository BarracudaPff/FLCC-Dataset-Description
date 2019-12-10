package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.layout.*
import org.jetbrains.completion.full.line.models.FullLineCompletionAlgorithm
import org.jetbrains.completion.full.line.models.FullLineCompletionMode
import org.jetbrains.completion.full.line.settings.MLServerCompletionBundle.Companion.message
import java.awt.event.ActionEvent

class MLServerCompletionConfigurable(private val event: (event: ActionEvent) -> Unit)
    : BoundConfigurable("Server Code Completion"), SearchableConfigurable {
    private lateinit var gpt: ComponentPredicate
    private val settings = MLServerCompletionSettings.getInstance()

    override fun createPanel(): DialogPanel {
        return panel {
            titledRow(message("ml.server.completion.settings.group")) {
                row {
                    cell {
                        gpt = checkBox(message("ml.server.completion.gpt"), settings::enable).selected
                        button("Test Connection", actionListener = event)
                    }
                    row {
                        row {
                            buttonGroup(settings::mode) {
                                label(message("ml.server.completion.mode"))
                                row { radioButton(message("ml.server.completion.mode.full.line"), FullLineCompletionMode.FULL_LINE) }
                                row { radioButton(message("ml.server.completion.mode.one.token"), FullLineCompletionMode.ONE_TOKEN) }
                            }
                        }

                        row {
                            checkBox(message("ml.server.completion.autoPopup"), settings::autoPopup, message("ml.server.completion.autoPopup.desc"))
                        }

                        if (Registry.get("ml.server.completion.expand.settings").asBoolean()) {
                            row {
                                buttonGroup(settings::algorithm) {
                                    label(message("ml.server.completion.algorithm"))
                                    row {
                                        radioButton(message("ml.server.completion.algorithm.df"), FullLineCompletionAlgorithm.DEFAULT)
                                        row(message("ml.server.completion.tokens")) {
                                            intTextFieldFixed(settings::tokens, 1, IntRange(0, 100))
                                        }
                                        row(message("ml.server.completion.suggestions")) {
                                            intTextFieldFixed(settings::suggestions, 1, IntRange(0, 100))
                                        }
                                    }

                                    row {
                                        radioButton(message("ml.server.completion.algorithm.bs"), FullLineCompletionAlgorithm.BEAM_SEARCH)
                                        row(message("ml.server.completion.num.iterations")) {
                                            intTextFieldFixed(settings::numIterations, 1, IntRange(0, 100))
                                        }
                                        row(message("ml.server.completion.beam.size")) {
                                            intTextFieldFixed(settings::beamSize, 1, IntRange(0, 20))
                                        }
                                        row(message("ml.server.completion.diversity.groups")) {
                                            intTextFieldFixed(settings::diversityGroups, 1, IntRange(0, 20))
                                        }
                                        row(message("ml.server.completion.diversity.strength")) {
                                            doubleTextField(settings::diversityStrength, 1, IntRange(0, 10))
                                        }
                                        row {
                                            val use = checkBox(message("ml.server.completion.topn.use"), settings::useTopN).selected
                                            row { intTextFieldFixed(settings::topN, 1, IntRange(0, 100)) }.enableIf(use)
                                        }
                                    }

                                    row {
                                        radioButton(message("ml.server.completion.algorithm.viterbi"), FullLineCompletionAlgorithm.VITERBI)
                                    }.enabled = false
                                }
                            }

                            row {
                                checkBox(message("ml.server.completion.filter"), settings::filter)
                            }

                            row {
                                checkBox(message("ml.server.completion.experimental"), settings::experimental)
                            }
                        }

                        row {
                            checkBox(message("ml.server.completion.enable.template.walking"), settings::enableTemplateWalking)
                        }

                        row {
                            checkBox(message("ml.server.completion.enable.comments"), settings::enableComments)
                        }

                        row {
                            checkBox(message("ml.server.completion.only.full"), settings::onlyFullLines)
                        }
                    }.enableSubRowsIf(gpt)
                }
            }
        }
    }

    override fun getHelpTopic(): String {
        return "ml.server.completion"
    }

    override fun getId(): String {
        return helpTopic
    }
}
