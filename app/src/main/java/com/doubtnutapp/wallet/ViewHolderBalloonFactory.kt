/*
 * Copyright (C) 2019 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doubtnutapp.wallet

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.*

class ViewHolderBalloonFactory : Balloon.Factory() {

    override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {
        val textForm = textForm(context) {
            setText("")
        }

        return createBalloon(context) {
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setArrowSize(10)
            setArrowPosition(0.5f)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowOrientation(ArrowOrientation.TOP)
            setPadding(10)
            setCornerRadius(8f)
            setElevation(4)
            setTextForm(textForm)
            setIconGravity(IconGravity.END)
            setDismissWhenClicked(true)
            setDismissWhenShowAgain(true)
            setLifecycleOwner(lifecycle)
            build()
        }
    }
}
