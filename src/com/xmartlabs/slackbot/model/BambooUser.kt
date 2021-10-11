package com.xmartlabs.slackbot.model

import com.xmartlabs.slackbot.data.sources.BambooHrCustomFieldsInfo
import com.xmartlabs.slackbot.data.sources.serializer.TogglBambooDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

data class BambooUser(
    val id: String,
    val customFields: BambooHrUserCustomFields,
    val displayName: String,
    val workEmail: String,
    val workingHours: Int,
    val hireDate: LocalDate,
)

@Serializable
data class BambooHrDirectoryUser(
    val id: String,
    val displayName: String,
    val workEmail: String?,
)

@Serializable
data class BambooHrUserCustomFields(
    @SerialName(BambooHrCustomFieldsInfo.ADDRESS_FIELD_ID)
    val address: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.DATE_OF_BIRTH_FIELD_ID)
    @Serializable(with = TogglBambooDateSerializer::class)
    val birthday: LocalDate? = null,
    @SerialName(BambooHrCustomFieldsInfo.CI_FIELD_ID)
    val ci: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.DEPARTMENT_FIELD_FIELD_ID)
    val department: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.DRINK_PREFERENCES_FIELD_ID)
    val drinkPreferences: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.EMERGENCY_CONTACT_FIELD_ID)
    val emergencyContact: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.GENDER_FIELD_ID)
    val gender: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.GITHUB_FIELD_ID)
    val githubId: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.HIRE_DATE_FIELD_ID)
    @Serializable(with = TogglBambooDateSerializer::class)
    val hireDate: LocalDate = LocalDate.MIN,
    @SerialName(BambooHrCustomFieldsInfo.IS_PHOTO_UPLOADED_FIELD_NAME)
    val isPhotoUploaded: Boolean? = null,
    @SerialName(BambooHrCustomFieldsInfo.MARITAL_STATUS_FIELD_ID)
    val maritalStatus: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.MOBILE_PHONE_FIELD_ID)
    val mobilePhone: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.SHIRT_SIZE_FIELD_ID)
    val shirtSize: String? = null,
    @SerialName(BambooHrCustomFieldsInfo.WORKING_HOURS_FIELD_ID)
    val workingHours: Int? = null,
)
