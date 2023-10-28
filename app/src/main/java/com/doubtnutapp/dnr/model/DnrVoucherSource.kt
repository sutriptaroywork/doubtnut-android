package com.doubtnutapp.dnr.model

enum class DnrVoucherSource(val type: String) {
    VOUCHER_LIST("voucher_list"),
    SPIN_THE_WHEEL("spin_wheel"),
    MYSTERY_BOX("mystery_box"),
    BETTER_LUCK_NEXT_TIME("better_luck_next_time"),
    REDEEM_VOUCHER("redeem_voucher"),
    UNLOCKED_VOUCHER("unlocked_vouchers")
}
