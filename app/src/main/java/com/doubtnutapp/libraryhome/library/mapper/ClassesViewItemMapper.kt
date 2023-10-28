package com.doubtnutapp.libraryhome.library.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.doubtnutapp.domain.library.entities.ClassListEntityItem
import com.doubtnutapp.domain.library.entities.ClassListViewEntity
import com.doubtnutapp.domain.library.entities.ClassListViewItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassesViewItemMapper @Inject constructor() : Mapper<ClassListEntity, ClassListViewEntity> {

    override fun map(srcObject: ClassListEntity) = with(srcObject) {
        ClassListViewEntity(
                getClassList(classList, studentClass),
                studentClass.toString()
        )
    }

    private fun getClassList(classList: List<ClassListEntityItem>, studentClass: Int): List<ClassListViewItem> {
        val list = classList.map {
            ClassListViewItem(
                    classNo = it.classNo,
                    className = it.className
            )
        }
        var indexOfCurrentClass = -1
        for (it in list) {
            if (it.classNo == studentClass) {
                indexOfCurrentClass = list.indexOf(it)
                break
            }
        }
        if (indexOfCurrentClass >= 0) {
            val currentClassItem = list[indexOfCurrentClass]
            (list as ArrayList).removeAt(indexOfCurrentClass)
            list.add(0, currentClassItem)
        }
        return list
    }
}