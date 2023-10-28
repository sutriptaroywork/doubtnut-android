package com.doubtnutapp.doubtpecharcha.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SubmitP2pFeedback
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.DoubtP2PMember
import com.doubtnutapp.databinding.ItemDoubtPeCharchaRatingBinding
import com.doubtnutapp.databinding.ItemDoubtPeCharchaUserBinding
import com.doubtnutapp.loadImage
import kotlinx.android.synthetic.main.item_doubt_pe_charcha_user.view.*

class DoubtPeCharchaUserAdapter(
    val actionPerformer: ActionPerformer,
    private val viewType: Int
) : RecyclerView.Adapter<BaseViewHolder<DoubtP2PMember>>() {

    companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_RATING = 1
    }

    private val memberList = mutableListOf<DoubtP2PMember>()

    private val showDivider: Boolean
        get() = memberList.size > 1 && memberList.find { it.isHost == 1 } != null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<DoubtP2PMember> =
        if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_doubt_pe_charcha_user, parent, false)
            )
        } else {
            RatingViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_doubt_pe_charcha_rating, parent, false)
            )
        }.apply {
            actionPerformer = this@DoubtPeCharchaUserAdapter.actionPerformer
        }

    override fun getItemCount(): Int {
        return memberList.size
    }

    fun updateUsers(users: List<DoubtP2PMember>) {
        memberList.clear()
        memberList.addAll(users)
        notifyDataSetChanged()
    }

    fun getMembers() = memberList

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DoubtP2PMember>, position: Int) {
        holder.bind(memberList[position])
        if (holder is UserViewHolder) {
            holder.itemView.divider.isVisible = position == 0 && showDivider
        }
    }

    inner class UserViewHolder(itemView: View) : BaseViewHolder<DoubtP2PMember>(itemView) {
        val binding = ItemDoubtPeCharchaUserBinding.bind(itemView)

        override fun bind(data: DoubtP2PMember) {
            val context = binding.root.context

            binding.apply {
                userImage.loadImage(data.imgUrl)
                userName.text = data.name
                when (data.isOnline) {
                    true ->
                        userStatus.background =
                            AppCompatResources.getDrawable(context, R.drawable.ic_online_member)

                    else ->
                        userStatus.background =
                            AppCompatResources.getDrawable(context, R.drawable.ic_offline_member)
                }

                when (data.isHost) {
                    1 -> userSymbol.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.ic_host_symbol
                        )
                    )
                    else -> userSymbol.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            R.drawable.ic_helper_symbol
                        )
                    )
                }

                root.setOnClickListener {
                    if (data.studentId.isEmpty().not()) {
                        FragmentWrapperActivity.userProfile(
                            itemView.context,
                            data.studentId, "doubt_p2p"
                        )
                    }
                }
            }
        }
    }

    inner class RatingViewHolder(itemView: View) : BaseViewHolder<DoubtP2PMember>(itemView) {
        val binding = ItemDoubtPeCharchaRatingBinding.bind(itemView)

        override fun bind(data: DoubtP2PMember) {
            val context = binding.root.context

            binding.apply {
                tvTitle.text = String.format(
                    context.resources.getString(R.string.item_rate_peer_title),
                    data.name
                )
                ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                    if (fromUser) {
                        actionPerformer?.performAction(
                            SubmitP2pFeedback(
                                studentId = data.studentId,
                                rating = rating,
                                reason = null
                            )
                        )
                    }
                }
            }
        }
    }
}
