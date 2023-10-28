package com.doubtnutapp.librarylisting.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.doubtnutapp.librarylisting.model.HeaderInfo
import com.doubtnutapp.librarylisting.ui.LibraryListingFragment
import com.doubtnutapp.resourcelisting.ui.ResourceListingFragment

/**
 * Created by Anand Gaurav on 2019-09-30.
 */
class LibraryListingPagerAdapter(
    fragmentManager: FragmentManager,
    val headerInfoList: List<HeaderInfo>,
    val page: String? = null
) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return if (headerInfoList[position].isLast.equals("1")) {
            ResourceListingFragment.newInstance(
                headerInfoList[position].id,
                headerInfoList[position].title.orEmpty(),
                false,
                hideToolbar = true,
                isFromVideoTag = false,
                tagName = "",
                questionId = "",
                packageDetailsId = headerInfoList[position].packageDetailsId,
                page = page
            )
        } else {
            return LibraryListingFragment.newInstance(
                headerInfoList[position].id,
                "",
                position,
                headerInfoList[position].packageDetailsId,
                page
            )
        }
    }

    override fun getCount(): Int {
        return headerInfoList.size
    }

    override fun getPageTitle(position: Int): CharSequence? = headerInfoList[position].title
}