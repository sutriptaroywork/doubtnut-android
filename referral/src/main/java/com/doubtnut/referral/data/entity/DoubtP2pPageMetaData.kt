package com.doubtnut.referral.data.entity

object DoubtP2pPageMetaData {

    var firstAutomatedMessage: String? = null
    var secondAutomatedMessage: String? = null

    var hostResponseText: String? = null


    //Answer Pending State data
    var answerPendingDataTitle: String? = null
    var answerPendingDataSubtitle: String? = null

    //Answer Accepted State Data
    var answerAcceptedDataTitle: String? = null
    var answerAcceptedDataSubtitle: String? = null

    var answerMarkAsSolvedTitle:String?=null
    var answerMarkAsSolvedSubtitle:String?=null

    //Answer Rejected State Data
    var answerRejectedDataTitle: String? = null
    var answerRejectedDataSubtitle: String? = null

    var toastAlreadySolved: String? = null

    var branchIODeeplink: String? = ""

    var doubtnut_whatsapp_number: String? = null
    var notifyOnWhatsappTitle: String? = null
    var whatsappNotifyText: String? = ""
    var isWhatsappNotifyEnable: Boolean? = false

    var starterQuestionText: String? = null

    var questionText: String? = ""
    var questionImageUrl: String? = ""

    var groupLimitReachedMessage: String? = ""
    var whatsappImage: String? = ""

    var isFeedbackSubmitted=false
}