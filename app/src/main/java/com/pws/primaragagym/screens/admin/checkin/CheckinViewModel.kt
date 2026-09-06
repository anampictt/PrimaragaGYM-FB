package com.pws.primaragagym.screens.admin.checkin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CheckinMockDataSource {
    val members = MutableStateFlow(
        listOf(
            MemberMock("MBR-001", "John Smith", "Premium Monthly", MembershipStatus.ACTIVE),
            MemberMock("MBR-002", "Sarah Connor", "Basic Monthly", MembershipStatus.ACTIVE),
            MemberMock("MBR-003", "Michael Brown", "Annual Premium", MembershipStatus.EXPIRING_SOON),
            MemberMock("MBR-004", "David Miller", "Basic Monthly", MembershipStatus.EXPIRED),
            MemberMock("MBR-005", "Jessica Wilson", "Premium Monthly", MembershipStatus.SUSPENDED)
        )
    )
    val activities = MutableStateFlow<List<CheckinActivityMock>>(emptyList())
}

class CheckinViewModel : ViewModel() {

    val members: StateFlow<List<MemberMock>> = CheckinMockDataSource.members.asStateFlow()
    val activities: StateFlow<List<CheckinActivityMock>> = CheckinMockDataSource.activities.asStateFlow()

    fun getMemberById(id: String): MemberMock? {
        return CheckinMockDataSource.members.value.find { it.memberId.equals(id, ignoreCase = true) }
    }

    fun performCheckin(memberId: String): Boolean {
        var success = false
        CheckinMockDataSource.members.update { currentList ->
            currentList.map { member ->
                if (member.memberId.equals(memberId, ignoreCase = true)) {
                    // Validasi: hanya bisa checkin jika ACTIVE atau EXPIRING_SOON
                    if (member.membershipStatus == MembershipStatus.ACTIVE || member.membershipStatus == MembershipStatus.EXPIRING_SOON) {
                        success = true
                        val timeNow = getCurrentTime()
                        addActivity(member.memberId, member.name, timeNow, ActivityType.CHECK_IN)
                        member.copy(
                            checkinStatus = CheckinStatus.CHECKED_IN,
                            checkInTime = timeNow
                        )
                    } else {
                        member
                    }
                } else {
                    member
                }
            }
        }
        return success
    }

    fun performCheckout(memberId: String): Boolean {
        var success = false
        CheckinMockDataSource.members.update { currentList ->
            currentList.map { member ->
                if (member.memberId.equals(memberId, ignoreCase = true)) {
                    if (member.checkinStatus == CheckinStatus.CHECKED_IN) {
                        success = true
                        val timeNow = getCurrentTime()
                        addActivity(member.memberId, member.name, timeNow, ActivityType.CHECK_OUT)
                        member.copy(
                            checkinStatus = CheckinStatus.CHECKED_OUT,
                            checkOutTime = timeNow
                        )
                    } else {
                        member
                    }
                } else {
                    member
                }
            }
        }
        return success
    }

    private fun addActivity(memberId: String, name: String, time: String, type: ActivityType) {
        CheckinMockDataSource.activities.update { currentList ->
            val newActivity = CheckinActivityMock(
                memberId = memberId,
                name = name,
                time = time,
                type = type
            )
            // Tambahkan di awal agar yang terbaru di atas
            listOf(newActivity) + currentList
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm 'WIB'", Locale.getDefault())
        return sdf.format(Date())
    }
}
