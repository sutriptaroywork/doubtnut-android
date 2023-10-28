package com.doubtnutapp.scheduledquiz.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import com.doubtnutapp.scheduledquiz.ui.viewholders.*

@Suppress("UNCHECKED_CAST")
class ScheduledQuizNotificationAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<BaseViewHolder<ScheduledQuizNotificationModel>>() {

    companion object {
        const val TRENDING_GAME = 1;
        const val MISSED_TRENDING_CLASS = 2;
        const val QUIZ_SOLUTION = 3;
        const val QUIZ_LEADERBOARD = 4;
        const val QUIZ_ASK = 5;
        const val QUIZ_PLAYLIST = 6;
        const val QUIZ_EXPLORE = 7;
        const val QUIZ_SUBJECT = 8;
        const val QUIZ_WORD = 9;
        const val QUIZ_POST = 10;
        const val QUIZ_NCERT = 11;
        const val QUIZ_MOTIVATION = 12;
        const val QUIZ_MCQ = 13;
    }

    var actionPerformer: ActionPerformer? = null
    val listings = mutableListOf<ScheduledQuizNotificationModel>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ScheduledQuizNotificationModel> {
        return when (viewType) {
            TRENDING_GAME -> {
                ScheduleNotificationViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_trending_game_day,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_NCERT -> {
                QuizNcertViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_ncert_video,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            MISSED_TRENDING_CLASS -> {
                MissedClassViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_quiz_video,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_SOLUTION -> {
                QuizSolutionViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_trending_question,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_EXPLORE -> {
                QuizExploreViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_explore_notification,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_ASK -> {
                QuizAskViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_ask_question,
                        parent,
                        false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_PLAYLIST -> {
                QuizPlaylistViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_quiz_playlist,
                        parent,
                        false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_SUBJECT -> {
                QuizSubjectViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_download_notification,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_WORD -> {
                QuizWordViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_quiz_word,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_POST -> {
                QuizPostViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_trending_post,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_MOTIVATION -> {
                QuizMotivationViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_quiz_motivation,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_LEADERBOARD -> {
                QuizLeaderboardViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_quiz_leaderboard,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            QUIZ_MCQ -> {
                QuizMcqViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_quiz_mcq,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
            else -> {
                MissedClassViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.layout_quiz_video,
                        parent, false
                    )
                )
                    .apply { actionPerformer = actionsPerformer }
            }
        }
    }

    override fun getItemCount(): Int = listings.size

    fun updateList(recentListings: List<ScheduledQuizNotificationModel>) {
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (listings[position].viewType.equals("quiz_game"))
            return TRENDING_GAME
        else if (listings.get(position).viewType.equals("quiz_video"))
            return MISSED_TRENDING_CLASS
        else if (listings.get(position).viewType.equals("quiz_solution"))
            return QUIZ_SOLUTION
        else if (listings.get(position).viewType.equals("quiz_leaderboard"))
            return QUIZ_LEADERBOARD
        else if (listings.get(position).viewType.equals("quiz_subject"))
            return QUIZ_SUBJECT
        else if (listings.get(position).viewType.equals("quiz_explore"))
            return QUIZ_EXPLORE
        else if (listings.get(position).viewType.equals("quiz_word"))
            return QUIZ_WORD
        else if (listings.get(position).viewType.equals("quiz_post"))
            return QUIZ_POST
        else if (listings.get(position).viewType.equals("quiz_ask"))
            return QUIZ_ASK
        else if (listings.get(position).viewType.equals("quiz_ncert"))
            return QUIZ_NCERT
        else if (listings.get(position).viewType.equals("quiz_playlist"))
            return QUIZ_PLAYLIST
        else if (listings.get(position).viewType.equals("quiz_motivation"))
            return QUIZ_MOTIVATION
        else if (listings.get(position).viewType.equals("quiz_mcq"))
            return QUIZ_MCQ
        else
            return -1
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ScheduledQuizNotificationModel>,
        position: Int
    ) {
        when (getItemViewType(position)) {
            TRENDING_GAME -> {
                (holder as ScheduleNotificationViewHolder).bind(listings[position])
            }
            MISSED_TRENDING_CLASS -> {
                (holder as MissedClassViewHolder).bind(listings[position])
            }
            QUIZ_SOLUTION -> {
                (holder as QuizSolutionViewHolder).bind(listings[position])
            }
            QUIZ_ASK -> {
                (holder as QuizAskViewHolder).bind(listings[position])
            }
            QUIZ_SUBJECT -> {
                (holder as QuizSubjectViewHolder).bind(listings[position])
            }
            QUIZ_WORD -> {
                (holder as QuizWordViewHolder).bind(listings[position])
            }
            QUIZ_EXPLORE -> {
                (holder as QuizExploreViewHolder).bind(listings[position])
            }
            QUIZ_POST -> {
                (holder as QuizPostViewHolder).bind(listings[position])
            }
            QUIZ_NCERT -> {
                (holder as QuizNcertViewHolder).bind(listings[position])
            }
            QUIZ_PLAYLIST -> {
                (holder as QuizPlaylistViewHolder).bind(listings[position])
            }
            QUIZ_MOTIVATION -> {
                (holder as QuizMotivationViewHolder).bind(listings[position])
            }
            QUIZ_LEADERBOARD -> {
                (holder as QuizLeaderboardViewHolder).bind(listings[position])
            }
            QUIZ_MCQ -> {
                (holder as QuizMcqViewHolder).bind(listings[position])
            }

        }
    }
}

