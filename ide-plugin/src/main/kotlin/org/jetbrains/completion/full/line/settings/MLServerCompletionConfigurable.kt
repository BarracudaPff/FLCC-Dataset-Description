package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.JBColor
import com.intellij.ui.layout.*
import com.intellij.util.ui.AsyncProcessIcon
import org.jetbrains.completion.full.line.models.FullLineCompletionMode
import org.jetbrains.completion.full.line.settings.MLServerCompletionBundle.Companion.message
import java.awt.Color
import javax.swing.JLabel

class MLServerCompletionConfigurable(
        private val statusChecker: () -> ActionCallback
) : BoundConfigurable("Server Code Completion"), SearchableConfigurable {
    private lateinit var gpt: ComponentPredicate
    private lateinit var statusText: JLabel
    private lateinit var loadingIcon: AsyncProcessIcon
    private val settings = MLServerCompletionSettings.getInstance().state

    override fun createPanel(): DialogPanel {
        return panel {
            titledRow(message("ml.server.completion.settings.group")) {
                row {
                    gpt = checkBox(message("ml.server.completion.title"), settings::enable).selected
                    row {
                        cell {
                            button("Test Connection") { checkStatus() }
                            loadingIcon = loadingIcon().component
                            statusText = statusText("").component
                        }
                    }
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
                        checkBox(message("ml.server.completion.autoPopup"), settings::autoPopup)
                    }

                    row {
                        checkBox(message("ml.server.completion.enable.strings.walking"), settings::stringsWalking)
                    }

                    row {
                        checkBox(message("ml.server.completion.enable.comments"), settings::enableComments)
                    }

                    row {
                        checkBox(message("ml.server.completion.only.full"), settings::onlyFullLines)
                    }

                    row {
                        val use = checkBox(message("ml.server.completion.top.n.use")).selected
                        row { intTextFieldFixed(settings::topN, 1, IntRange(0, 100)) }.enableIf(use)
                    }

                    if (Registry.get("ml.server.completion.expand.settings").asBoolean()) {
                        row(message("ml.server.completion.bs")) {
                            row(message("ml.server.completion.bs.num.iterations")) {
                                intTextFieldFixed(settings::numIterations, 1, IntRange(0, 50))
                            }
                            row(message("ml.server.completion.bs.beam.size")) {
                                intTextFieldFixed(settings::beamSize, 1, IntRange(0, 20))
                            }
                            row(message("ml.server.completion.bs.diversity.strength")) {
                                doubleTextField(settings::diversityStrength, 1, IntRange(0, 1))
                            }
                            row(message("ml.server.completion.bs.diversity.groups")) {
                                intTextFieldFixed(settings::diversityGroups, 1, IntRange(0, 10))
                                row {
                                    val groupUse = checkBox(message("ml.server.completion.group.top.n.use")).selected
                                    row { intTextFieldFixed(settings::groupTopN, 1, IntRange(0, 100)) }.enableIf(groupUse)
                                }
                            }
                        }
                    }
                }.enableSubRowsIf(gpt)
            }
        }
    }

    private fun checkStatus() {
        loadingIcon.resume()
        loadingIcon.isVisible = true
        statusText.isVisible = false

        statusChecker().doWhenDone {
            statusText.text = "Successful"
            statusText.foreground = JBColor(JBColor.GREEN.darker(), JBColor.GREEN.brighter())
        }.doWhenRejected(Runnable {
            statusText.text = "Invalid"
            statusText.foreground = JBColor.RED
        }).doWhenProcessed {
            loadingIcon.suspend()
            loadingIcon.isVisible = false
            statusText.isVisible = true
        }
    }

    override fun getHelpTopic(): String {
        return "ml.server.completion"
    }

    override fun getId(): String {
        return helpTopic
    }
}
