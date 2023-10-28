package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.newglobalsearch.ui.InAppSearchFragment

class SearchResultViewHolderFactory {

    fun getViewHolderFor(
        parent: ViewGroup,
        viewType: Int,
        deeplinkAction: DeeplinkAction?,
        resultCount: Int
    ): BaseViewHolder<*> {
        return when (viewType) {

            R.layout.item_search_header -> SearchHeaderViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_search_header, parent, false
                )
            )

            R.layout.item_all_search_header -> AllSearchHeaderViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_all_search_header, parent, false
                )
            )

            R.layout.item_search_suggestion -> SearchSuggestionViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_search_suggestion, parent, false
                ), resultCount
            )

            R.layout.item_tyd_suggestions -> SearchTydSuggestionViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_tyd_suggestions, parent, false
                )
            )

            R.layout.item_search_playlist -> {
                if (InAppSearchFragment.isTrending == true) {
                    RecentPlaylistViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_trending_playlist, parent, false
                        )
                    )
                } else {
                    SearchPlaylistViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_search_playlist, parent, false
                        ), deeplinkAction, resultCount
                    )
                }
            }

            R.layout.item_search_playlist_full_width -> {
                SearchPlaylistFullWidthViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_search_playlist_full_width, parent, false
                    ), resultCount
                )
            }

            R.layout.item_search_playlist_live_class_full_width -> {
                SearchPlaylistFullWidthLiveClassViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_search_playlist_live_class_full_width, parent, false
                    ), deeplinkAction, resultCount
                )
            }

            R.layout.item_trending_vertical_list -> TrendingVerticalFeedViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_vertical_list, parent, false
                )
            )

            R.layout.item_trending_horizontal_list -> TrendingHorizontalFeedViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_horizontal_list, parent, false
                )
            )

            R.layout.item_trending_staggered_list -> TrendingStaggeredFeedViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_staggered_list, parent, false
                )
            )

            R.layout.item_trending_grid_list -> TrendingGridFeedViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_grid_list, parent, false
                )
            )

            R.layout.item_recent_search -> RecentListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_recent_search, parent, false
                ), resultCount
            )

            R.layout.item_trending_search -> TrendingListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_search, parent, false
                ), resultCount
            )

            R.layout.item_trending_subject -> TrendingSubjectListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_subject, parent, false
                ), resultCount
            )

            R.layout.item_trending_video_feed -> TrendingVideoListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_video_feed, parent, false
                ), resultCount
            )

            R.layout.item_trending_book_feed -> TrendingLibraryBooksViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_book_feed, parent, false
                )
            )

            R.layout.item_search_book_feed -> SearchBooksViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_search_book_feed, parent, false
                ), resultCount
            )

            R.layout.item_trending_exam_paper_search -> TrendingLibraryExamPaperViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_exam_paper_search, parent, false
                )
            )

            R.layout.item_trending_pdf_search -> TrendingLibraryPdfViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_trending_pdf_search, parent, false
                )
            )

            R.layout.books_new_layout -> BooksParentViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.books_new_layout, parent, false
                )
            )

            R.layout.item_youtube_results -> YoutubeVideoListViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_youtube_results, parent, false
                )
            )

            R.layout.item_youtube_banner -> YoutubeBannerViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_youtube_banner, parent, false
                )
            )

            R.layout.item_course_banner -> CourseBannerViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_course_banner, parent, false
                ), deeplinkAction
            )

            R.layout.dialog_advanced_search -> AdvancedFilterViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.dialog_advanced_search, parent, false))

            R.layout.item_see_all_button -> SeeAllButtonViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_see_all_button, parent, false
                )
            )

            R.layout.item_ias_related_lectures -> LiveClassLectureListViewHolder(
                LayoutInflater.from(
                    parent.context
                ).inflate(R.layout.item_ias_related_lectures, parent, false), resultCount
            )

            else -> throw IllegalArgumentException("Wrong View Type")
        }
    }
}