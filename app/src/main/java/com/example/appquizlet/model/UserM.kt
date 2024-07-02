package com.example.appquizlet.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

//Dùng bằng Singeton
object UserM {
    private val userData = MutableLiveData<UserResponse>()

    //    private var userDataStudySets = MutableLiveData<List<StudySetModel>>()
    private var allStudySets = MutableLiveData<StudySetModel>()

    private var dataAchievement = MutableLiveData<DetectContinueModel>()
    private var dataSetSearch = MutableLiveData<List<SearchSetModel>>()
    private var dataSettings = MutableLiveData<UpdateUserResponse>()
    private var dataRanking = MutableLiveData<RankResultModel>()
    private var dataNotification = MutableLiveData<List<NoticeModel>>()

    fun setUserData(data: UserResponse) {
        userData.value = data
    }

    fun getUserData(): LiveData<UserResponse> {
        return userData
    }

    fun getAllStudySets(): LiveData<StudySetModel> {
        return allStudySets
    }

    fun setAllStudySet(data: StudySetModel) {
        allStudySets.value = data
    }

    fun getDataAchievements(): LiveData<DetectContinueModel> {
        return dataAchievement
    }

    fun setDataAchievements(data: DetectContinueModel) {
        dataAchievement.value = data
    }

    fun getDataSetSearch(): LiveData<List<SearchSetModel>> {
        return dataSetSearch
    }

    fun setDataSetSearch(data: List<SearchSetModel>) {
        dataSetSearch.value = data
    }

    fun getDataSettings(): LiveData<UpdateUserResponse> {
        return dataSettings
    }

    fun setDataSettings(data: UpdateUserResponse) {
        dataSettings.value = data
    }

    fun getDataRanking(): LiveData<RankResultModel> {
        return dataRanking
    }

    fun setDataRanking(data: RankResultModel) {
        dataRanking.value = data
    }

    fun getDataNotification(): LiveData<List<NoticeModel>> {
        return dataNotification
    }

    fun setDataNotification(data: List<NoticeModel>) {
        dataNotification.value = data
    }

}



//Có thể sử dụng bằng ViewModel để tránh mất mát dl khi xoay or thay đổi cấu hình
//class UserViewModel : ViewModel() {
//    private val userData = MutableLiveData<UserResponse>()
//    private val allStudySets = MutableLiveData<StudySetModel>()
//    private val dataAchievement = MutableLiveData<DetectContinueModel>()
//    private val dataSetSearch = MutableLiveData<List<SearchSetModel>>()
//    private val dataSettings = MutableLiveData<UpdateUserResponse>()
//    private val dataRanking = MutableLiveData<RankResultModel>()
//    private val dataNotification = MutableLiveData<List<NoticeModel>>()
//
//    fun setUserData(data: UserResponse) {
//        userData.value = data
//    }
//
//    fun getUserData(): LiveData<UserResponse> {
//        return userData
//    }
//
//    fun getAllStudySets(): LiveData<StudySetModel> {
//        return allStudySets
//    }
//
//    fun setAllStudySet(data: StudySetModel) {
//        allStudySets.value = data
//    }
//
//    // Các phương thức khác tương tự cho các dữ liệu khác...
//}

// // Kết nối ViewModel với Fragment
//        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java

//class UserFragment : Fragment() {
//    //...
//
//    fun addUserToViewModel(user: User) {
//        userViewModel.setUserData(user)
//    }
//
//    fun displayUsers() {
//        val userListLiveData = userViewModel.getUserData()
//        userListLiveData.observe(viewLifecycleOwner, { userList ->
//            // Xử lý và hiển thị danh sách người dùng khi dữ liệu thay đổi
//        })
//    }
//
//    //...
//}