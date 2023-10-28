package com.doubtnutapp.db.entity

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation

@Keep
data class MatchWithQuestions(

    @Embedded
    val matchQuestion: LocalMatchQuestion,

    @Relation(
        parentColumn = "question_id",
        entityColumn = "match_question_id"
    )
    val matchQuestionList: List<LocalMatchQuestionList>,

    @Relation(
        parentColumn = "question_id",
        entityColumn = "match_question_id"
    )
    val facets: List<LocalMatchFacet>,
)
