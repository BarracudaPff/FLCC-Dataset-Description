package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.UIUtil
import org.jetbrains.completion.full.line.settings.MLServerCompletionBundle.Companion.message
import org.jetbrains.completion.full.line.utils.MLServerUtils

class ServerCompletionConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable? {
        val project = ProjectManager.getInstance().defaultProject

        return MLServerCompletionConfigurable {
            println(MLServerCompletionSettings.getInstance())
            MLServerUtils.getStatus().doWhenDone {
                showSuccessfulConnectionMessage(project)
            }.doWhenRejected { message ->
                showConnectionFailedMessage(project, message)
            }
        }
    }

    private fun showConnectionFailedMessage(project: Project, message: String) {
        UIUtil.invokeLaterIfNeeded {
            Messages.showMessageDialog(project, message("ml.server.completion.gpt.connection.error"),
                    message, Messages.getErrorIcon())
        }
    }

    private fun showSuccessfulConnectionMessage(project: Project) {
        UIUtil.invokeLaterIfNeeded {
            Messages.showMessageDialog(project, message("ml.server.completion.gpt.connection.successful"),
                    message("ml.server.completion.gpt.connection"), Messages.getInformationIcon())
        }
    }
}
