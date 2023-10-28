package com.doubtnutapp.gamification.mybio.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.mybio.entity.*
import com.doubtnutapp.gamification.mybio.model.*
import com.doubtnutapp.gamification.mybio.model.Language
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserBioMapper @Inject constructor() : Mapper<ApiUserBio, UserBioDataModel> {

    override fun map(srcObject: ApiUserBio): UserBioDataModel =
        with(srcObject) {
            UserBioDataModel(
                name = name.orEmpty(),
                image = image.orEmpty(),
                gender = getUserBioListDataModel(gender),
                userClass = getUserBioListDataModel(userClass),
                board = getUserBoardListDataModel(board),
                exams = getUserBoardListDataModel(exams),
                location = getUserBioLocation(location),
                school = school.orEmpty(),
                coaching = getUserBioCoaching(coaching),
                dob = dob.orEmpty(),
                languages = languages?.map { language ->
                    Language(
                        id = language.id,
                        code = language.code,
                        title = language.title,
                        isSelected = language.isSelected
                    )
                }
            )
        }

    private fun getUserBioListDataModel(apiUserBioList: ApiUserBioList): UserBioListDataModel =
        with(apiUserBioList) {
            UserBioListDataModel(
                isActive = isActive.orEmpty(),
                options = options.map {
                    getUserBioListOptions(it)
                }
            )
        }

    private fun getUserBoardListDataModel(apiUserBoardList: ApiUserBoardList): UserBoardListDataModel =
        with(apiUserBoardList) {
            UserBoardListDataModel(
                isActive = isActive.orEmpty(),
                options = getUserBoardListOptions(options)
            )
        }

    private fun getUserBioListOptions(apiUserBioListOption: ApiUserBioListOption): UserBioListOptionDataModel =
        with(apiUserBioListOption) {
            UserBioListOptionDataModel(
                id = id ?: 0,
                alias = alias.orEmpty(),
                className = className.orEmpty(),
                selected = selected ?: 0,
                imageUrl = imageUrl.orEmpty(),
                custom = custom.orEmpty(),
                streamList = streamList
            )
        }

    private fun getUserBoardListOptions(apiUserBoardOptions: HashMap<String, List<ApiUserBioListOption>>): HashMap<String, List<UserBioListOptionDataModel>> {
        val userBoardOptionsDataModel = hashMapOf<String, List<UserBioListOptionDataModel>>()
        apiUserBoardOptions.forEach {
            userBoardOptionsDataModel[it.key] = it.value.map {
                UserBioListOptionDataModel(
                    id = it.id ?: 0,
                    alias = it.alias.orEmpty(),
                    className = it.className.orEmpty(),
                    selected = it.selected ?: 0,
                    imageUrl = it.imageUrl.orEmpty(),
                    custom = it.custom.orEmpty(),
                    streamList = it.streamList
                )
            }
        }
        return userBoardOptionsDataModel
    }

    private fun getUserBioLocation(apiUserBioLocation: ApiUserBioLocation): UserBioLocationDataModel =
        with(apiUserBioLocation) {
            UserBioLocationDataModel(
                location = location.orEmpty(),
                lat = lat.orEmpty(),
                lon = lon.orEmpty()
            )
        }

    private fun getUserBioCoaching(apiUserBioCoaching: ApiUserBioCoaching): UserBioCoachingDataModel =
        with(apiUserBioCoaching) {
            UserBioCoachingDataModel(
                isActive = isActive.orEmpty(),
                name = name.orEmpty()
            )
        }

    fun mapPostUserBioLocation(userBioLocationDataModel: UserBioLocationDataModel): PostUserBioLocation? {
        return if (userBioLocationDataModel.location.isEmpty() && userBioLocationDataModel.lat.isEmpty() &&
            userBioLocationDataModel.lon.isEmpty()
        )
            null
        else
            with(userBioLocationDataModel) {
                PostUserBioLocation(
                    location = location,
                    lat = lat,
                    long = lon,
                    state = state,
                    country = country
                )
            }
    }
}