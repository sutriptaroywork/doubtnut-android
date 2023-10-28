package com.doubtnutapp.ui.base

import androidx.viewbinding.ViewBinding
import com.doubtnut.core.ui.base.CoreBindingFragment
import com.doubtnutapp.base.BaseViewModel

abstract class BaseBindingFragment<VM : BaseViewModel, VB : ViewBinding> :
    CoreBindingFragment<VM, VB>()