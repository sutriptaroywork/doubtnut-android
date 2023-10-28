package com.doubtnutapp.similarVideo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.InsertItemsOfATabKey
import com.doubtnutapp.EventBus.ShuffleNcertSimilarVideoList
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.InsertChildrenAtNode
import com.doubtnutapp.base.NcertBookClick
import com.doubtnutapp.base.NcertVideoClick
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.course.widgets.CollapsedWidget
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.similarVideo.ui.NcertSimilarFragment
import com.doubtnutapp.similarVideo.ui.NcertSimilarFragment.Companion.SOURCE_NCERT_PAGE
import com.doubtnutapp.similarVideo.widgets.NcertBookWidget
import com.doubtnutapp.similarVideo.widgets.NcertSimilarWidget
import com.doubtnutapp.similarVideo.widgets.ParentGridSelectionWidget
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.widgets.ParentTabWidget
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayDeque

class NcertSimilarViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val videoPageEventManager: VideoPageEventManager,
) : BaseViewModel(compositeDisposable) {

    var questionId: String? = null
    var page: String? = null

    private var previouslyPlayingNode: NcertTreeNode<*>? = null
    private var currentPlayingNode: NcertTreeNode<*>? = null

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _scrollUp: MutableLiveData<Boolean> = MutableLiveData()
    val scrollUp: LiveData<Boolean>
        get() = _scrollUp

    private val _refreshVideoPage: MutableLiveData<String> = MutableLiveData()
    val refreshVideoPage: LiveData<String>
        get() = _refreshVideoPage

    private val _playingVideoResource: MutableLiveData<Pair<String, NcertSimilarWidget.Data>> =
        MutableLiveData()
    val playingVideoResource: LiveData<Pair<String, NcertSimilarWidget.Data>>
        get() = _playingVideoResource

    private val _ncertSimilarVideoList: MutableLiveData<List<WidgetEntityModel<*, *>>> =
        MutableLiveData()
    val ncertSimilarVideoList: LiveData<List<WidgetEntityModel<*, *>>>
        get() = _ncertSimilarVideoList

    private val _bookWidgetData: MutableLiveData<List<WidgetEntityModel<*, *>>> = MutableLiveData()
    val bookWidgetData: LiveData<List<WidgetEntityModel<*, *>>>
        get() = _bookWidgetData

    // store all the playing video in current session, will be used to play when user presses back
    private val playingVideoStack = Stack<NcertTreeNode<*>>()

    fun hasUserWatchedMoreThanOneVideo() = playingVideoStack.size > 0

    var collapsedWidgetEntityModel: CollapsedWidget.Model? = null

    private val root = NcertTreeNode("NCERT", "NCERT", "NCERT")

    /**
     * This method request api to get response for a node if tree is already build else request for the first time.
     * @param playlistId - playlist id attached to a node or extracted from a deeplink
     * @param type - type attached to a node or extracted from deeplink
     * @param questionId - extracted from deeplink or empty
     * @param rootNode - if request for the first time - null else node for which data is requested
     * @param playNext - need to play next video after fetching data from api
     */
    fun getNcertSimilarWidgets(
        playlistId: String, type: String, questionId: String?,
        rootNode: NcertTreeNode<*>?, playNext: Boolean? = false
    ) {

        _loading.postValue(true)

        compositeDisposable.add(
            DataHandler.INSTANCE.ncertSimilarVideoRepository.getNcertVideoAdditionalData(
                playlistId,
                type,
                questionId
            )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    { apiResponse ->
                        apiResponse.data.ncertSimilar.let { ncertWidgets ->

                            // build the tree from the node for which data is requested else from root node.
                            buildNcertSimilarDataTree(ncertWidgets, rootNode ?: root)

                            if (rootNode == null) {
                                // first time request - i.e. data requested for first time
                                _ncertSimilarVideoList.postValue(ncertWidgets)
                            } else {
                                // Update the children nodes of a node for which data is requested
                                DoubtnutApp.INSTANCE.bus()
                                    ?.send(InsertItemsOfATabKey(playlistId, ncertWidgets))
                            }
                            if (playNext == true) {
                                playNextVideo()
                            } else {
                                reshufflingVideoList()
                            }
                        }
                        apiResponse.data.ncertBooks?.let { bookWidgets ->
                            _bookWidgetData.postValue(bookWidgets)
                        }

                        _loading.postValue(false)
                    },
                    {
                        _loading.postValue(false)
                        it.printStackTrace()
                    }
                )
        )
    }

    /**
     * This class defines a Node in NCERT data tree.
     * @param playlistId - unique id for each node in the tree
     * @param type - type of each node (each level in the tree has same type)
     * @param value - value associated with each node (Here WidgetEntityModel<*, *> represents value)
     * @constructor - creates a node with specified values
     */
    data class NcertTreeNode<T>(
        val playlistId: String,
        val type: String,
        val value: T?,
        var isPlayed: Boolean? = false,
        var nudges: Map<String, WidgetEntityModel<*, *>>? = null, // Nudges to show between list
    ) {

        private var parent: NcertTreeNode<*>? = null

        private val children: MutableList<NcertTreeNode<*>> = mutableListOf()

        /**
         * This method inserts a node to the parent's children list and set its parent
         * @param node - node to be added
         */
        fun addChildNode(node: NcertTreeNode<*>) {
            children.add(node)
            node.parent = this
        }

        /**
         * This method deletes all the children attached to this node.
         */
        fun deleteChildren() = children.clear()

        /**
         * This method removes item from its position
         * and add at last position in the sibling list
         */
        fun addAtLastPosition() {
            val siblings = getSiblings() ?: return
            val indexOfCurrentNode = siblings.indexOfFirst {
                it.playlistId == playlistId
            }
            if (indexOfCurrentNode == -1) return
            val item = siblings.removeAt(indexOfCurrentNode)
            item.isPlayed = true
            siblings.add(item)
        }

        /**
         * This method add list of nodes at the end and mark all as played.
         */
        fun moveNodesAtLastPosition(startPosition: Int, currentNodePosition: Int) {
            val siblings = getSiblings()?.toMutableList() ?: return
            if (startPosition == currentNodePosition || startPosition == -1 || currentNodePosition == -1 ||
                startPosition > siblings.size - 1 || currentNodePosition > siblings.size - 1
            ) return
            val nodesAfterCurrentNode = siblings.subList(currentNodePosition, siblings.size)
            val nodesBeforeCurrentNode = mutableListOf<NcertTreeNode<*>>()
            (startPosition until currentNodePosition).forEach { position ->
                val item = siblings[position]
                item.isPlayed = true
                nodesBeforeCurrentNode.add(item)
            }

            val reorderedList = nodesAfterCurrentNode + nodesBeforeCurrentNode
            siblings.clear()
            siblings.addAll(reorderedList)
            getParentNode()?.setChildren(siblings)
        }

        /**
         * This method returns node position in sibling list
         * @return - position of node
         */
        fun getNodePositionInChildren(): Int {
            val children = getParentNode()?.children ?: return -1
            return children.indexOfFirst {
                it.playlistId == playlistId
            }
        }

        /**
         * This method add node to its sibling list at last position.
         */
        fun addAsALastSibling() {
            val siblings = getSiblings() ?: return
            siblings.add(this)
        }

        /**
         * This method returns the next node in the children list
         * @return if found node (next sibling) else null
         */
        fun nextSiblingNode(): NcertTreeNode<*>? {

            val parentNode = getParentNode() ?: return null

            if (parentNode.children.isEmpty()) return null

            val siblingList = parentNode.children
            val firstIndexOfNode = siblingList.indexOfFirst {
                this.playlistId == it.playlistId
            }
            val lastIndexOfNode = siblingList.indexOfLast {
                this.playlistId == it.playlistId
            }
            if (firstIndexOfNode != lastIndexOfNode) return null
            if (firstIndexOfNode == -1 || firstIndexOfNode == siblingList.size - 1) return null
            val nextItem = siblingList[firstIndexOfNode + 1]
            if (nextItem.isPlayed == true) return null
            return nextItem
        }

        /**
         * This method returns parent node of a node.
         */
        fun getParentNode() = parent

        /**
         * This method returns children node list
         */
        fun getChildren() = children

        /**
         * This method uses to find left most children in the tree.
         * Searching starts from this node only.
         * @return node whose children list is empty
         */
        fun findLeftMostLeafNode(): NcertTreeNode<*> {
            val children = children
            if (children.isEmpty()) return this
            return children[0].findLeftMostLeafNode()
        }

        private fun setChildren(childrenNodes: List<NcertTreeNode<*>>) {
            children.clear()
            children.addAll(childrenNodes)
        }

        /**
         * This method returns sibling i.e. children at same level including that node.
         */
        private fun getSiblings(): MutableList<NcertTreeNode<*>>? = getParentNode()?.children

        override fun toString(): String {
            var s = "$value"
            if (children.isNotEmpty()) {
                s += " {" + children.map { it.toString() } + " }"
            }
            return s
        }
    }

    /**
     * This method searches node with playlistId as key starting from the node
     * using BFS(Breadth First Search).
     * @param key - key of a node to be searched
     * @param rootNode - start node from where searching starts.
     * @return - node if found else null
     */
    private fun searchNode(key: String, rootNode: NcertTreeNode<*>): NcertTreeNode<*>? {
        val queue = ArrayDeque<NcertTreeNode<*>>()
        queue.add(rootNode)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.playlistId == key) return node
            else queue.addAll(node.getChildren())
        }
        return null
    }

    /**
     * This method adds node list to its children list
     * @param key - playlistId of a node to which children will be added
     * @param children - node list to be added
     */
    private fun insertChildrenNodes(key: String, children: List<WidgetEntityModel<*, *>>?) {
        val node = searchNode(key, root) ?: return
        node.deleteChildren()
        buildNcertSimilarDataTree(children, node)
    }

    /**
     * This method gets top of playing video stack and start to play.
     * This item should be removed from stack
     */
    fun playPreviousVideo() {
        if (playingVideoStack.size == 0) return
        val previousPlayingNode = playingVideoStack.pop() ?: return
        val ncertSimilarModel = previousPlayingNode.value as? NcertSimilarWidget.Model ?: return
        _playingVideoResource.postValue(
            Pair(
                previousPlayingNode.playlistId,
                ncertSimilarModel.data
            )
        )
    }

    /*
     * This method plays next video in currently playing video list - sibling of current node
     * For reference '<-S->' : same level (sibling) '<-P->' : one level up (parent) '<-C->' : one level down (child)
     * [Video1] <-S-> [video2]
     * ----------------------------------------------------------------------------------------------------------------------------------------
     * If next video not found, go to parent node (exercise) and try to find children of (sibling of parent - i.e. next exercise).
     * [Video1] <-S-> [Not Found] <-P-> [ex 1] <-S-> [ex 2] <-C-> [Video1 video2...]
     * ----------------------------------------------------------------------------------------------------------------------------------------
     * If next exercise not found, go to parent i.e. chapter and try to find exercise under next chapter (sibling of chapter).
     * If next chapter is found, play video either directly if we have data or after fetching data from server for that chapter.
     * [Video1] <-S-> [Not Found] <-P-> [ex 1] <-S-> [Not Found] <-P-> [chapter 1] <-S-> [chapter 2] <-C-> [ex 1] <-C-> [Video1 video2...]
     * ----------------------------------------------------------------------------------------------------------------------------------------
     * If next chapter is not found, we reach root node.
     * [Video1] <-S-> [Not Found] <-P-> [ex 1] <-S-> [Not Found] <-P-> [chapter 1] <-S-> [Not Found] --> Next Book
     * i.e. the currently playing video is the last item of last exercise of last chapter of this book.
     * We need to fetch next book data - refresh this page with next book.
     * #######################################################################################################################
     *                                      root(parent node)
     *                              --------------------------------
     *                             /               |                \
     *                      [chapter1           chapter2           chapter3...]
     *                     /   |   \            /  |  \            /   |   \
     *                  [ex1  ex2 ex3...]    [ex1  ex2  ex3...]  [ex1 ex2 ex3..]
     *                 / | \                                     / | \
     *              [Video1 video2...]                         [Video1 video2...]
     * ########################################################################################################################
     */
    fun playNextVideo() {

        // current playing node (similar video)
        val currentNode = currentPlayingNode ?: return

        previouslyPlayingNode = currentNode

        // Next node to play i.e. next video in the list (similar video)
        val nextNodeToPlay = currentNode.nextSiblingNode()

        // If current playing node is last in the list
        if (nextNodeToPlay == null) {

            // Parent node of current playing video i.e. exercise under which this video exist
            val parentNode = currentNode.getParentNode() ?: return

            // next exercise node in the list
            val siblingOfParent = parentNode.nextSiblingNode()

            // If this was the last exercise in the chapter, we will not get next exercise in the same chapter
            if (siblingOfParent == null) {
                // Parent node of exercise node (Chapter Node)
                val parentOfParentNode = parentNode.getParentNode() ?: return

                // Next chapter in the list
                val siblingOfParentOfParent = parentOfParentNode.nextSiblingNode()

                // If this is last chapter
                if (siblingOfParentOfParent == null) {
                    // Refresh video page with next book
                    refreshVideoPageWithBookData()
                } else {
                    // Fetch data for chapter node
                    fetchDataForNodeIfVideoNotFoundToPlay(siblingOfParentOfParent)
                }
            } else {
                // If exercise found, Fetch data for this exercise node
                fetchDataForNodeIfVideoNotFoundToPlay(siblingOfParent)
            }
        } else {
            // play this video
            playVideo(nextNodeToPlay)
        }
    }

    /**
     * This method refreshed video page if user finishes watching
     * last video in last exercise of last chapter in this book.
     * Iterate over book's data which is shown at bottom.
     * Open deeplink of first book after resetting current page data.
     */
    private fun refreshVideoPageWithBookData() {
        val bookWidgetData = bookWidgetData.value ?: return
        val item = bookWidgetData.find {
            it.type == WidgetTypes.TYPE_WIDGET_PARENT_TAB
        }?.data as? ParentTabWidget.Data
        item?.let { data ->
            if (data.items?.isNotEmpty() == true) {
                data.items?.forEach { entry ->
                    entry.value.find { widgetEntityModel ->
                        widgetEntityModel.type == WidgetTypes.TYPE_WIDGET_NCERT_BOOK
                    }?.data?.apply {
                        (this as? NcertBookWidget.Data)?.let { bookData ->
                            resetNcertData()
                            sendEvent(
                                EventConstants.NCERT_NEXT_BOOK_AUTO_PLAYED,
                                hashMapOf<String, Any>().apply {
                                    put(EventConstants.PLAYLIST_ID, bookData.playlistId)
                                    put(EventConstants.PLAYLIST_TYPE, bookData.type)
                                    put(EventConstants.SOURCE, SOURCE_NCERT_PAGE)
                                }
                            )
                            _refreshVideoPage.postValue(deeplink)
                        }
                    }
                }
            }
        }
    }

    /**
     * Play video resource attached to node
     * @param nodeToPlay - leaf node similar video
     */
    private fun playVideo(nodeToPlay: NcertTreeNode<*>) {
        val ncertSimilarModel = nodeToPlay.value as? NcertSimilarWidget.Model ?: return
        currentPlayingNode = nodeToPlay
        _playingVideoResource.postValue(Pair(nodeToPlay.playlistId, ncertSimilarModel.data))

        // reshuffle list after playing video.
        reshufflingVideoList()

        val exerciseId: String? =
            if ("BOOK_LIST" == page) ncertSimilarModel.data.activeChapterId else null
        updateNcertLastWatchedDetails(ncertSimilarModel.data.questionId, exerciseId)

        sendEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, NcertSimilarFragment.TAG)
        })

        sendEvent(EventConstants.NCERT_SIMILAR_AUTO_PLAYED, hashMapOf<String, Any>().apply {
            put(EventConstants.PLAYLIST_ID, nodeToPlay.playlistId)
            put(EventConstants.PLAYLIST_TYPE, nodeToPlay.type)
            put(EventConstants.SOURCE, SOURCE_NCERT_PAGE)
        })
    }

    private fun updateNcertLastWatchedDetails(questionId: String, exerciseId: String?) {
        compositeDisposable.add(
            DataHandler.INSTANCE.ncertSimilarVideoRepository.postNcertLastWatchedDetails(
                questionId = questionId,
                exerciseId = exerciseId
            )
                .applyIoToMainSchedulerOnCompletable()
                .subscribe({}, {})
        )
    }

    /**
     * This method shuffles the children, removes currently playing video from the children list and
     * add previously playing video at the end of list.
     * Nodes before currently playing node will be added at the end in children list
     * For ex- from a list of 10 videos indices [0 - 9] if video at index 4 is playing,
     * then we move [0 - 3] to last in the list. indices [5 - 9] will come in the first place.
     * and currently playing node will be removed from the list. i.e. index 4.
     * Handle the case where current and previous playing have different parent.
     */
    private fun reshufflingVideoList() {

        val currentNode = currentPlayingNode ?: return

        val currentNodePosition = currentNode.getNodePositionInChildren()
        if (currentNodePosition != -1) {
            currentNode.isPlayed = true
            currentNode.moveNodesAtLastPosition(
                startPosition = 0,
                currentNodePosition = currentNodePosition
            )
        }

        val parentOfCurrentNode = currentNode.getParentNode() ?: return

        /*
         # If previous playing node is not in same list of current playing node
         # i.e. both have different parent node i.e. both videos are from different exercise.
         # Then add previous playing node at last position to the children of previous exercise.
         */
        val parentOfPreviousNode = previouslyPlayingNode?.getParentNode()
        if (parentOfCurrentNode.playlistId != parentOfPreviousNode?.playlistId) {
            previouslyPlayingNode?.addAtLastPosition()
        }

        if (parentOfCurrentNode.getChildren().isEmpty()) return

        /*
         # Iterate over children of parent of currently playing node i.e. siblings,
         # if only one child in the list and is playing right now, add it to the non playing list.
         # else add all the children except which is currently playing to the list.
         # update children list and tab i.e. (exercise)
         # selected tab will be changed if currently playing node and previously playing node
         # are of different exercise (parents are different).
         */
        val childrenWidgets = mutableListOf<WidgetEntityModel<*, *>>()
        val children = parentOfCurrentNode.getChildren()
        children.forEach { childNode ->
            val childWidget = childNode.value as? WidgetEntityModel<*, *> ?: return
            if (childNode.playlistId != currentNode.playlistId) {
                childrenWidgets.add(childWidget)
            }
        }

        collapsedWidgetEntityModel?.data?.items = childrenWidgets

        // Set nudges if found, will be shown in between video list.
        // currently shown in only video list which is coming under exercises
        parentOfCurrentNode.nudges?.let {
            collapsedWidgetEntityModel?.data?.nudges = it
        }

        // After creating list of children and selected tab (if changed), update using broadcaster
        collapsedWidgetEntityModel?.let {
            DoubtnutApp.INSTANCE.bus()
                ?.send(ShuffleNcertSimilarVideoList(parentOfCurrentNode.playlistId, listOf(it)))
        }
    }

    /**
     * This method find left most child of a node whose children list is empty.
     * i.e. we reached to the last level where children is empty.
     * it can be chapter, exercise or video. If leftmost is video play it.
     * else fetch data for that node from the server.
     * @param node - it can be chapter (level 1) or exercise (level 2) node
     */
    private fun fetchDataForNodeIfVideoNotFoundToPlay(node: NcertTreeNode<*>) {
        val leftMostLeafNode = node.findLeftMostLeafNode()

        // Check if this is the video, which we can play
        val leftMostLeafNodeValue = leftMostLeafNode.value as? NcertSimilarWidget.Model

        // Get data from api for this node (chapter or exercise)
        if (leftMostLeafNodeValue == null) {
            getNcertSimilarWidgets(
                playlistId = node.playlistId,
                type = leftMostLeafNode.type,
                questionId = questionId,
                rootNode = leftMostLeafNode,
                playNext = true
            )
        } else {
            // play video
            playVideo(leftMostLeafNode)
        }
    }

    /**
     * Method to build tree for NCERT Similar Widgets using recursion
     * Each level of the tree contains same type of widgets (children).
     * This tree has 4 level (except - one root to start building tree)
     * Level 0 - root node (NCERT) @see NcertTreeNode
     * Level 1 - Chapter tabs
     * Level 2 - Exercises in each chapters
     * Level 3 - Similar Videos widgets in each exercises
     * #######################################################################################################################
     *  Level0 ->                           root(parent node)                                        [NCERT]
     *                              --------------------------------                                    |
     *                             /               |                \                                   |
     *  Level1 ->             [chapter1            chapter2           chapter3...]                ParentTabWidget
     *                       /   |   \            /  |  \            /   |   \                  /       |        \
     *  Level2 ->         [ex1  ex2 ex3...]    [ex1  ex2  ex3...]  [ex1 ex2 ex3..]            [ParentGridSelectionWidget ...]
     *                  / | \                                     / | \                        /        |          \
     *  Level3 ->  [Video1 video2...]                         [Video1 video2...]           [CollapsedWidget -
     *                                                                               (items as NcertSimilarWidget)...]
     * #######################################################################################################################
     * @param similarData - api response contains widgets
     * @param root - node from which we start adding children
     * @see NcertTreeNode - root node
     * @see NcertSimilarWidget (Video Solutions) - Leaf nodes contains video solution.
     * @see ParentGridSelectionWidget (Exercises) - Parent of leaf nodes
     * @see ParentTabWidget (Chapters) - Parent of ParentGridSelectionWidget
     */
    private fun buildNcertSimilarDataTree(
        similarData: List<WidgetEntityModel<*, *>>?, root: NcertTreeNode<*>
    ) {

        similarData?.forEach { widgetEntityModel ->

            when (widgetEntityModel.type) {

                WidgetTypes.TYPE_WIDGET_PARENT_TAB -> {
                    val model = widgetEntityModel as? ParentTabWidget.Model ?: return
                    val parentTabData = model.data
                    parentTabData.tabs?.forEach { tabData ->
                        val parentTabNode =
                            NcertTreeNode(tabData.playListId, tabData.type, widgetEntityModel)
                        root.addChildNode(parentTabNode)
                        parentTabData.items?.let { itemsMap ->
                            val tabItemList = itemsMap[tabData.key]
                            if (tabItemList != null) {
                                // for each chapter tab add children (exercises)
                                // for next function call each chapter tab will act as a root
                                buildNcertSimilarDataTree(tabItemList, parentTabNode)
                            }
                        }
                    }
                }

                WidgetTypes.TYPE_WIDGET_PARENT_GRID_SELECTION -> {
                    val model = widgetEntityModel as? ParentGridSelectionWidget.Model ?: return
                    val parentGridData = model.data
                    parentGridData.tabs.forEach { tabData ->
                        val parentGridNode =
                            NcertTreeNode(tabData.playListId, tabData.type, widgetEntityModel)
                        root.addChildNode(parentGridNode)
                        parentGridData.items?.let { itemsMap ->
                            val tabItemList = itemsMap[tabData.key]
                            if (tabItemList != null) {
                                // for each exercise add children (CollapsedWidget with items as list of NcertSimilarWidget)
                                // for next function call each exercise will act as a root
                                buildNcertSimilarDataTree(tabItemList, parentGridNode)
                            }
                        }
                    }
                }

                WidgetTypes.TYPE_WIDGET_COLLAPSED -> {
                    collapsedWidgetEntityModel =
                        widgetEntityModel as? CollapsedWidget.Model ?: return
                    val collapsedWidgetData = collapsedWidgetEntityModel?.data ?: return
                    // for each CollapsedWidget add NcertSimilarWidget list to corresponding exercise as root
                    // root will remain same as exercise.
                    root.nudges = collapsedWidgetData.nudges
                    buildNcertSimilarDataTree(collapsedWidgetData.items, root)
                }

                // This is the base case of recursion. When we reached to list of videos we will break after inserting all the nodes
                // ToDo - we can minimize recursive calls (stack size) by inserting all the nodes at once in previous steps.
                WidgetTypes.TYPE_WIDGET_NCERT_SIMILAR -> {
                    val model = widgetEntityModel as? NcertSimilarWidget.Model ?: return
                    val ncertSimilarData = model.data
                    val ncertSimilarNode = NcertTreeNode(
                        playlistId = ncertSimilarData.playlistId,
                        type = ncertSimilarData.type,
                        value = widgetEntityModel,
                        isPlayed = ncertSimilarData.isPlaying
                    )
                    // add each NcertSimilarWidget as children to the exercise
                    root.addChildNode(ncertSimilarNode)
                    if (ncertSimilarData.isPlaying == true) {
                        currentPlayingNode = ncertSimilarNode
                    }
                }
            }
        }
    }

    fun handleAction(action: Any) {
        when (action) {
            is NcertVideoClick -> {
                searchNode(action.playlistId, root)?.let {
                    previouslyPlayingNode = currentPlayingNode
                    playingVideoStack.push(previouslyPlayingNode)
                    playVideo(it)
                }
                _scrollUp.postValue(true)
            }
            is InsertChildrenAtNode -> {
                insertChildrenNodes(action.playlistId, action.children)
            }
            is NcertBookClick -> {
                if (action.openNewPage != true) {
                    resetNcertData()
                }
                _refreshVideoPage.postValue(action.deeplink)
            }
            else -> {
            }
        }
    }

    private fun resetNcertData() {
        _ncertSimilarVideoList.postValue(emptyList())
        _bookWidgetData.postValue(emptyList())
        root.getChildren().forEach {
            it.deleteChildren()
        }
        root.deleteChildren()
    }

    fun getVideoResource(apiVideoResource: ApiVideoResource) = with(apiVideoResource) {
        VideoResource(
            resource = resource,
            drmScheme = drmScheme,
            drmLicenseUrl = drmLicenseUrl,
            mediaType = mediaType,
            isPlayed = false,
            dropDownList = dropDownList?.map {
                VideoResource.PlayBackData(
                    resource = it.resource, drmScheme = it.drmScheme,
                    drmLicenseUrl = it.drmLicenseUrl,
                    mediaType = it.mediaType, display = it.display
                )
            },
            timeShiftResource = timeShiftResource?.let {
                VideoResource.PlayBackData(
                    resource = it.resource, drmScheme = it.drmScheme,
                    drmLicenseUrl = it.drmLicenseUrl,
                    mediaType = it.mediaType, display = it.display
                )
            },
            offset = offset
        )
    }

    fun sendEvent(event: String, param: HashMap<String, Any> = hashMapOf()) {
        videoPageEventManager.eventWith(event, param, ignoreSnowplow = false)
    }
}