package com.doubtnutapp.matchquestion.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.matchquestion.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by devansh on 11/08/20.
 */
@Singleton
class AdvancedSearchOptionsMapper @Inject constructor() :
    Mapper<Pair<ApiAdvancedSearchOptions, Boolean>, MatchFilterFacetListViewItem> {

    override fun map(srcObject: Pair<ApiAdvancedSearchOptions, Boolean>): MatchFilterFacetListViewItem =
        MatchFilterFacetListViewItem(
            viewType = R.layout.item_match_filter_facet_list,
            facetList = mapMatchFilterData(srcObject.first.facets, srcObject.second),
            displayFilter = srcObject.first.displayFilter
        )

    private fun mapMatchFilterData(
        facetList: List<ApiAdvanceSearchData>,
        isLanguageVariantEnabled: Boolean
    ): List<MatchFilterFacetViewItem> =
        facetList.map { apiFacetV2 ->
            MatchFilterFacetViewItem(
                facetType = apiFacetV2.facetType,
                display = apiFacetV2.display,
                local = apiFacetV2.local,
                isSelected = apiFacetV2.isSelected,
                isMultiSelect = apiFacetV2.isMultiSelect,
                showDisplayText = apiFacetV2.showDisplayText,
                isUpperFocused = apiFacetV2.upperFocused,
                data = apiFacetV2.data.map { apiTopic ->
                    MatchFilterTopicViewItem(
                        display = apiTopic.display,
                        isSelected = apiTopic.isSelected,
                        isAllTopic = apiTopic.isAllTopic,
                        type = apiTopic.type,
                        data = apiTopic.data,
                        selectable = apiTopic.selectable
                    )
                },
                viewType = R.layout.item_match_filter_facet
            )
        }.toMutableList().also { list ->
            if (list.size > 0 && list.find { it.isSelected } != null) {
                list.add(
                    MatchFilterFacetViewItem(
                        "",
                        "",
                        local = true,
                        isSelected = true,
                        isMultiSelect = false,
                        showDisplayText = true,
                        isUpperFocused = false,
                        data = emptyList(),
                        viewType = R.layout.item_match_clear_filter
                    )
                )
            }
        }.apply {
            if (isLanguageVariantEnabled) {
                sortedBy {
                    it.isUpperFocused
                }
            }
        }

    fun unMapMatchFilterData(facetList: List<MatchFilterFacetViewItem>): List<ApiAdvanceSearchData> =
        facetList.filter {
            it.viewType == R.layout.item_match_filter_facet
        }.sortedBy {
            it.isUpperFocused
        }.map { matchFilterFacetViewItem ->
            ApiAdvanceSearchData(
                facetType = matchFilterFacetViewItem.facetType,
                display = matchFilterFacetViewItem.display,
                local = matchFilterFacetViewItem.local,
                isSelected = matchFilterFacetViewItem.isSelected,
                isMultiSelect = matchFilterFacetViewItem.isMultiSelect,
                showDisplayText = matchFilterFacetViewItem.showDisplayText,
                upperFocused = matchFilterFacetViewItem.isUpperFocused,
                data = matchFilterFacetViewItem.data.map { matchFilterTopicViewItem ->
                    ApiAdvanceSearchTopic(
                        display = matchFilterTopicViewItem.display,
                        isSelected = matchFilterTopicViewItem.isSelected,
                        isAllTopic = matchFilterTopicViewItem.isAllTopic,
                        type = matchFilterTopicViewItem.type,
                        data = matchFilterTopicViewItem.data,
                        selectable = matchFilterTopicViewItem.selectable
                    )
                }
            )
        }
}