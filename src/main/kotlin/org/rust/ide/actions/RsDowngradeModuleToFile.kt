package org.rust.ide.actions

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.impl.file.PsiFileImplUtil
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BaseRefactoringAction
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesUtil
import org.rust.ide.utils.checkWriteAccessAllowed
import org.rust.lang.RsLanguage
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.ext.RsMod

class RsDowngradeModuleToFile : BaseRefactoringAction() {
    override fun isEnabledOnElements(elements: Array<out PsiElement>): Boolean = elements.all { it.isDirectoryMod }

    override fun getHandler(dataContext: DataContext): RefactoringActionHandler = Handler

    override fun isAvailableInEditorOnly(): Boolean = false

    override fun isAvailableForLanguage(language: Language): Boolean = language.`is`(RsLanguage)

    private object Handler : RefactoringActionHandler {
        override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext?) {
            invoke(project, arrayOf(file), dataContext)
        }

        override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
            WriteCommandAction.runWriteCommandAction(project) {
                for (element in elements) {
                    contractModule(element as PsiFileSystemItem)
                }
            }
        }
    }
}

private fun contractModule(fileOrDirectory: PsiFileSystemItem) {
    checkWriteAccessAllowed()

    val (file, dir) = when (fileOrDirectory) {
        is RsFile -> fileOrDirectory to fileOrDirectory.parent!!
        is PsiDirectory -> fileOrDirectory.children.single() as RsFile to fileOrDirectory
        else -> error("Can contract only files and directories")
    }

    val dst = dir.parent!!
    val fileName = "${dir.name}.rs"
    PsiFileImplUtil.setName(file, fileName)
    MoveFilesOrDirectoriesUtil.doMoveFile(file, dst)
    dir.delete()
}

private val PsiElement.isDirectoryMod: Boolean get() {
    return when (this) {
        is RsFile -> name == RsMod.MOD_RS && containingDirectory?.children?.size == 1
        is PsiDirectory -> {
            val child = children.singleOrNull()
            child is RsFile && child.name == RsMod.MOD_RS
        }
        else -> false
    }
}
