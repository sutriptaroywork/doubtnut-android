package com.doubtnutapp.ui.base

import androidx.viewbinding.ViewBinding
import com.doubtnut.core.ui.base.CoreBindingDialogFragment
import com.doubtnutapp.base.BaseViewModel

abstract class BaseBindingDialogFragment<VM : BaseViewModel, VB : ViewBinding> :
    CoreBindingDialogFragment<VM, VB>()