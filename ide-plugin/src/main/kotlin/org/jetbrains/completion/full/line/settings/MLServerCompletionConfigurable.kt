package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.layout.*
import org.jetbrains.completion.full.line.models.FullLineCompletionMode
import org.jetbrains.completion.full.line.settings.MLServerCompletionBundle.Companion.message
import org.jetbrains.concurrency.runAsync
import kotlin.reflect.KFunction1

class MLServerCompletionConfigurable(
        private val exceptionMessage: KFunction1<Throwable, String>,
        private val getStatus: () -> Boolean,
        private val getModels: () -> List<String>
) : BoundConfigurable("Server Code Completion"), SearchableConfigurable {
    private lateinit var gpt: ComponentPredicate
    private val settings = MLServerCompletionSettings.getInstance().state

    override fun createPanel(): DialogPanel {
        return panel {
            titledRow(message("ml.server.completion.settings.group")) {
                row {
                    gpt = checkBox(message("ml.server.completion.title"), settings::enable).selected
                    row { cell { statusButton() } }
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
                        checkBox(message("ml.server.completion.only.full"), settings::onlyFullLines)
                    }

                    row {
                        checkBox(message("ml.server.completion.group.answers"), settings::groupAnswers)
                    }

                    row {
                        val use = checkBox(message("ml.server.completion.top.n.use"), settings::useTopN).selected
                        row { intTextFieldFixed(settings::topN, 1, IntRange(0, 100)) }.enableIf(use)
                    }

                    if (Registry.get("ml.server.completion.expand.settings").asBoolean()) {
                        expandedSettingsPanel()
                    }
                }.enableSubRowsIf(gpt)
            }
        }
    }

    private fun Row.expandedSettingsPanel() {
        row { checkBox(message("ml.server.completion.score"), settings::showScore) }

        row(message("ml.server.completion.bs")) {
            row(message("ml.server.completion.model.pick")) { modelsBox() }

            row(message("ml.server.completion.bs.num.iterations")) {
                intTextFieldFixed(settings::numIterations, 1, IntRange(0, 50))
            }
            row(message("ml.server.completion.bs.beam.size")) {
                intTextFieldFixed(settings::beamSize, 1, IntRange(0, 20))
            }
            row(message("ml.server.completion.bs.diversity.strength")) {
                doubleTextField(settings::diversityStrength, 1, IntRange(0, 1))
            }
            row(message("ml.server.completion.bs.len.base")) {
                doubleTextField(settings::lenBase, 1, IntRange(0, 1))
            }
            row(message("ml.server.completion.bs.len.pow")) {
                doubleTextField(settings::lenPow, 1, IntRange(0, 1))
            }
            row(message("ml.server.completion.bs.diversity.groups")) {
                intTextFieldFixed(settings::diversityGroups, 1, IntRange(0, 10))
                row {
                    val groupUse = checkBox(message("ml.server.completion.group.top.n.use"), settings::useGroupTopN).selected
                    row { intTextFieldFixed(settings::groupTopN, 1, IntRange(0, 10)) }.enableIf(groupUse)
                }
            }
        }
    }

    private fun Row.modelsBox() {
        val loadingIcon = LoadingComponent()
        val modelBox = comboBox(MutableCollectionComboBoxModel(listOf("best")), settings::model).component.apply {
            isVisible = false
        }
        loadingIcon.changeState(LoadingComponent.State.LOADING)

        runAsync(getModels).onSuccess {
            modelBox.model = MutableCollectionComboBoxModel(it)
            loadingIcon.changeState(LoadingComponent.State.SUCCESS)
        }.onError {
            modelBox.isEnabled = false
            loadingIcon.changeState(LoadingComponent.State.ERROR, exceptionMessage(it))
        }.onProcessed {
            modelBox.isVisible = true
            modelBox()
            loadingIcon.changeState(LoadingComponent.State.PROCESSED)
        }

        loadingIcon.loadingIcon()
        loadingIcon.statusText()
    }

    private fun Cell.statusButton() {
        val loadingIcon = LoadingComponent()

        button(message("ml.server.completion.connection")) {
            loadingIcon.changeState(LoadingComponent.State.LOADING)

            val t1 = System.currentTimeMillis()
            runAsync(getStatus).onSuccess {
                val message = if (it) {
                    "Successful with ${System.currentTimeMillis() - t1}ms delay"
                } else {
                    "Status failed with ${System.currentTimeMillis() - t1}ms delay"
                }
                loadingIcon.changeState(LoadingComponent.State.SUCCESS, message)
            }.onError {
                loadingIcon.changeState(LoadingComponent.State.ERROR, exceptionMessage(it))
            }.onProcessed {
                loadingIcon.changeState(LoadingComponent.State.PROCESSED)
            }
        }
        loadingIcon.loadingIcon()
        loadingIcon.statusText()
    }

    override fun getHelpTopic(): String {
        return "ml.server.completion"
    }

    override fun getId(): String {
        return helpTopic
    }
}
