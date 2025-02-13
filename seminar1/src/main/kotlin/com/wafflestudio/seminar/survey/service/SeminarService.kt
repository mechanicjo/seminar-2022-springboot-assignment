package com.wafflestudio.seminar.survey.service

import com.wafflestudio.seminar.survey.api.Seminar400
import com.wafflestudio.seminar.survey.api.Seminar404
import com.wafflestudio.seminar.survey.api.request.CreateSurveyRequest
import com.wafflestudio.seminar.survey.database.OperatingSystemEntity
import com.wafflestudio.seminar.survey.database.OsRepository
import com.wafflestudio.seminar.survey.database.SurveyResponseEntity
import com.wafflestudio.seminar.survey.database.SurveyResponseRepository
import com.wafflestudio.seminar.survey.domain.OperatingSystem
import com.wafflestudio.seminar.survey.domain.SurveyResponse
import com.wafflestudio.seminar.user.database.UserEntity
import com.wafflestudio.seminar.user.database.UserRepository
import com.wafflestudio.seminar.user.domain.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface SeminarService {
    fun os(name: String): OperatingSystem
    fun os(id: Long): OperatingSystem
    fun surveyResponseList(): List<SurveyResponse>
    fun surveyResponse(id: Long): SurveyResponse
    
    fun createSurveyResponse(request: CreateSurveyRequest, userId: Long?): SurveyResponse
}

@Service
class SeminarServiceImpl(
    private val surveyResponseRepository: SurveyResponseRepository,
    private val osRepository: OsRepository,
    private val userRepository: UserRepository
) : SeminarService {
    override fun os(id: Long): OperatingSystem {
        val entity = osRepository.findByIdOrNull(id) ?: throw Seminar404("OS를 찾을 수 없어요.")
        return OperatingSystem(entity)
    }

    override fun os(name: String): OperatingSystem {
        val entity = osRepository.findByOsName(name) ?: throw Seminar404("OS ${name}을 찾을 수 없어요.")
        return OperatingSystem(entity)
    }

    override fun surveyResponseList(): List<SurveyResponse> {
        val surveyEntityList = surveyResponseRepository.findAll()
        return surveyEntityList.map(::SurveyResponse)
    }

    override fun surveyResponse(id: Long): SurveyResponse {
        val surveyEntity = surveyResponseRepository.findByIdOrNull(id) ?: throw Seminar404("설문 결과를 찾을 수 없어요.")
        return SurveyResponse(surveyEntity)
    }

    override fun createSurveyResponse(request: CreateSurveyRequest, userId: Long?): SurveyResponse {
        userRepository.findByIdOrNull(userId) ?: throw com.wafflestudio.seminar.user.api.request.Seminar404("존재하지 않는 유저입니다.")
        if(request.springExp == null) throw Seminar400("spring 경험을 입력하세요.")
        if(request.rdbExp == null) throw Seminar400("rdb 경험을 입력하세요.")
        if(request.programmingExp == null) throw Seminar400("programming 경험을 입력하세요.")
        if(request.osName == null) throw Seminar400("os를 입력해주세요.")
        
        val surveyEntity = 
            SurveyResponseEntity(
                operatingSystem = osRepository.findByOsName(request.osName) ?: throw Seminar404("해당 OS를 찾을 수 없어요."),
                springExp = request.springExp,
                rdbExp = request.rdbExp,
                programmingExp = request.programmingExp,
                major = request.major,
                grade = request.grade,
                timestamp = LocalDateTime.now(),
                backendReason = request.backendReason,
                waffleReason = request.waffleReason,
                somethingToSay = request.somethingToSay,
                user_id = userRepository.findByIdOrNull(userId)
            )
        surveyResponseRepository.save(surveyEntity)
        return SurveyResponse(surveyEntity)
    }

    private fun OperatingSystem(entity: OperatingSystemEntity) = entity.run {
        OperatingSystem(id, osName, price, desc)
    }

    private fun UserEntity(entity: UserEntity?) = entity?.run {
        User(nickname, email, password, id)
    }
    private fun SurveyResponse(entity: SurveyResponseEntity) = entity.run {
        SurveyResponse(
            id = id,
            operatingSystem = OperatingSystem(operatingSystem),
            springExp = springExp,
            rdbExp = rdbExp,
            programmingExp = programmingExp,
            major = major,
            grade = grade,
            timestamp = timestamp,
            backendReason = backendReason,
            waffleReason = waffleReason,
            somethingToSay = somethingToSay,
            user_id = UserEntity(user_id)
        )
    }
}