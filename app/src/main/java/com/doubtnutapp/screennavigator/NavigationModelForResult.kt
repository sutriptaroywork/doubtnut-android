package com.doubtnutapp.screennavigator

import androidx.annotation.Keep

/**
 * Created by shrreya on 27/6/19.
 */
@Keep
class NavigationModelForResult(val screen: Screen,
                               val hashMap: HashMap<String, out Any>?,
                               val requestCode: Int)