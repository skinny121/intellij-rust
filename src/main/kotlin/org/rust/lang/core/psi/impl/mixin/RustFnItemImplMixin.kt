package org.rust.lang.core.psi.impl.mixin

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Iconable
import org.rust.lang.core.psi.RustDeclaringElement
import org.rust.lang.core.psi.RustFnItem
import org.rust.lang.core.psi.impl.RustItemImpl
import org.rust.lang.core.psi.util.isPublic
import org.rust.ide.icons.RustIcons
import org.rust.ide.icons.addTestMark
import org.rust.ide.icons.addVisibilityIcon
import javax.swing.Icon

public abstract class RustFnItemImplMixin(node: ASTNode) : RustItemImpl(node)
                                                         , RustFnItem {

    override fun getDeclarations(): Collection<RustDeclaringElement> =
        fnParams?.paramList.orEmpty().filterNotNull()


    override fun getIcon(flags: Int): Icon? {
        val icon = if (isTest()) RustIcons.FUNCTION.addTestMark() else RustIcons.FUNCTION
        if ((flags and Iconable.ICON_FLAG_VISIBILITY) == 0)
            return icon;

        return icon.addVisibilityIcon(isPublic())
    }

    fun isTest(): Boolean {
        return outerAttrList.map { it.metaItem?.identifier?.text }.find { "test".equals(it) } != null
    }

}

