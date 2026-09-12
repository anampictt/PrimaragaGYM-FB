package com.pws.primaragagym.domain.model

import com.pws.primaragagym.screens.admin.checkin.ActivityType
import com.pws.primaragagym.screens.admin.checkin.CheckinActivityMock
import com.pws.primaragagym.screens.admin.member.MemberStatus
import com.pws.primaragagym.screens.admin.member.MemberUiModel
import com.pws.primaragagym.screens.admin.member.MembershipPlanUiModel
import com.pws.primaragagym.screens.admin.member.MembershipUiModel
import com.pws.primaragagym.screens.admin.member.PlanType
import com.pws.primaragagym.screens.admin.member.TransactionStatus
import com.pws.primaragagym.screens.admin.member.TransactionType
import com.pws.primaragagym.screens.admin.member.TransactionUiModel
import com.pws.primaragagym.screens.superadmin.manajemancabang.BranchUiModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirestoreMapper {

    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    fun Date.toDisplayDate(): String = try { dateFormat.format(this) } catch (e: Exception) { "" }
    fun Date.toTime(): String = try { timeFormat.format(this) } catch (e: Exception) { "" }
    fun Long.toCurrency(): String = try { currencyFormat.format(this) } catch (e: Exception) { "Rp 0" }

    fun FirestoreMember.toUiModel(): MemberUiModel {
        val status = when (this.status.uppercase()) {
            "ACTIVE" -> MemberStatus.ACTIVE
            "EXPIRING_SOON" -> MemberStatus.EXPIRING_SOON
            "EXPIRED" -> MemberStatus.EXPIRED
            "SUSPENDED" -> MemberStatus.SUSPENDED
            else -> MemberStatus.ACTIVE
        }

        val initials = fullName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }

        return MemberUiModel(
            id = memberId,
            memberCode = memberCode,
            name = fullName,
            phone = phoneNumber,
            email = email,
            address = address,
            planName = "",
            status = status,
            startDate = joinedAt?.toDisplayDate() ?: "",
            expiredDate = "",
            avatarInitial = initials
        )
    }

    fun FirestoreMembershipPlan.toUiModel(): MembershipPlanUiModel {
        val planType = when (durationType.uppercase()) {
            "DAY" -> PlanType.DAILY
            "MONTH" -> PlanType.MONTHLY
            "YEAR" -> PlanType.YEARLY
            else -> PlanType.MONTHLY
        }

        val durationStr = when (durationType.uppercase()) {
            "DAY" -> "$durationValue Hari"
            "MONTH" -> "$durationValue Bulan"
            "YEAR" -> "$durationValue Tahun"
            else -> "$durationValue"
        }

        return MembershipPlanUiModel(
            id = planId,
            name = name,
            type = planType,
            price = price.toCurrency(),
            duration = durationStr,
            maxMembers = maxMembers ?: 0,
            isActive = isActive
        )
    }

    fun FirestoreMembership.toUiModel(): MembershipUiModel {
        val status = when (this.status.uppercase()) {
            "ACTIVE" -> MemberStatus.ACTIVE
            "EXPIRED" -> MemberStatus.EXPIRED
            "SUSPENDED" -> MemberStatus.SUSPENDED
            else -> MemberStatus.ACTIVE
        }

        return MembershipUiModel(
            id = membershipId,
            memberId = memberId,
            memberName = memberName,
            planName = planName,
            startDate = startDate?.toDisplayDate() ?: "",
            endDate = endDate?.toDisplayDate() ?: "",
            status = status,
            price = price.toCurrency()
        )
    }

    fun FirestorePayment.toUiModel(): TransactionUiModel {
        val transactionType = when (paymentType.uppercase()) {
            "RENEWAL" -> TransactionType.PERPANJANGAN
            "UPGRADE" -> TransactionType.UPGRADE
            "DOWNGRADE" -> TransactionType.DOWNGRADE
            "NEW_MEMBERSHIP" -> TransactionType.REGISTRASI
            else -> TransactionType.REGISTRASI
        }

        val transactionStatus = when (status.uppercase()) {
            "PAID" -> TransactionStatus.PAID
            "PENDING" -> TransactionStatus.PENDING
            "CANCELLED" -> TransactionStatus.CANCELLED
            else -> TransactionStatus.PAID
        }

        return TransactionUiModel(
            id = paymentId,
            type = transactionType,
            description = "",
            date = paidAt?.toDisplayDate() ?: "",
            amount = amount.toCurrency(),
            status = transactionStatus
        )
    }

    fun FirestoreBranch.toUiModel(): BranchUiModel {
        return BranchUiModel(
            id = branchId,
            name = name,
            address = address
        )
    }

    fun FirestoreCheckin.toActivityMock(): CheckinActivityMock {
        val activityType = if (status == "CHECKED_IN") ActivityType.CHECK_IN else ActivityType.CHECK_OUT

        return CheckinActivityMock(
            memberId = memberId,
            name = memberName,
            time = checkInAt?.toTime() ?: "",
            type = activityType
        )
    }
}
