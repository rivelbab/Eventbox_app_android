package com.eventbox.app.android.utils.errors

import com.eventbox.app.android.utils.errors.HttpErrors.BAD_REQUEST
import com.eventbox.app.android.utils.errors.HttpErrors.CONFLICT
import com.eventbox.app.android.utils.errors.HttpErrors.FORBIDDEN
import com.eventbox.app.android.utils.errors.HttpErrors.METHOD_NOT_ALLOWED
import com.eventbox.app.android.utils.errors.HttpErrors.NOT_FOUND
import com.eventbox.app.android.utils.errors.HttpErrors.REQUEST_TIMEOUT
import com.eventbox.app.android.utils.errors.HttpErrors.UNAUTHORIZED
import com.eventbox.app.android.utils.errors.HttpErrors.UNPROCESSABLE_ENTITY
import com.eventbox.app.android.utils.StringUtils.isEmpty
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber

const val ERRORS = "errors"
const val SOURCE = "source"
const val POINTER = "pointer"
const val DETAIL = "detail"
const val TITLE = "title"
const val CODE = "code"

const val POINTER_LENGTH = 3

object ErrorUtils {

    fun getMessage(throwable: Throwable): Error {
        var error = Error()
        if (throwable is HttpException) {
            when (throwable.code()) {
                BAD_REQUEST,
                    UNAUTHORIZED,
                    FORBIDDEN,
                    NOT_FOUND,
                    METHOD_NOT_ALLOWED,
                    REQUEST_TIMEOUT -> error =
                    getErrorTitleAndDetails(
                        throwable
                    )
                UNPROCESSABLE_ENTITY,
                    CONFLICT -> error =
                    getErrorDetails(
                        throwable
                    )
                else -> error.detail = throwable.message
            }
            return error
        }

        if (isEmpty(error.detail))
            error.detail = throwable.message
        return error
    }

    fun getErrorDetails(throwable: Throwable): Error {
        val error = Error()
        if (throwable is HttpException) {
            val responseBody = throwable.response()?.errorBody()

            try {
                val jsonObject = JSONObject(responseBody?.string())
                val jsonArray = JSONObject(jsonObject.getJSONArray(ERRORS).get(0).toString())
                val errorSource = JSONObject(jsonArray.get(SOURCE).toString())

                try {
                    val pointedField =
                        getPointedField(
                            errorSource.getString(POINTER)
                        )
                    if (pointedField == null) {
                        error.detail = jsonArray.get(DETAIL).toString()
                    } else {
                        error.pointer = pointedField
                        error.detail = jsonArray.get(DETAIL).toString().replace(".", "")
                        error.code = errorSource.get(CODE).toString()
                    }
                } catch (e: Exception) {
                    error.detail = jsonArray.get(DETAIL).toString()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return error
    }

    fun getErrorTitleAndDetails(throwable: Throwable): Error {
        val error = Error()
        if (throwable is HttpException) {
            val responseBody = throwable.response()?.errorBody()

            try {
                val jsonObject = JSONObject(responseBody?.string())
                val jsonArray = JSONObject(jsonObject.getJSONArray(ERRORS).get(0).toString())
                val errorSource = JSONObject(jsonArray.get(SOURCE).toString())

                try {
                    val pointedField =
                        getPointedField(
                            errorSource.getString(POINTER)
                        )

                    if (pointedField == null) {
                        error.detail = jsonArray.get(DETAIL).toString()
                    } else {
                        error.pointer = pointedField
                        error.detail = jsonArray.get(DETAIL).toString().replace(".", "")
                        error.code = errorSource.get(CODE).toString()
                    }
                    error.title = jsonArray.get(TITLE).toString()
                } catch (e: Exception) {
                    error.title = jsonArray.get(TITLE).toString()
                    error.detail = jsonArray.get(DETAIL).toString()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return error
    }

    fun getPointedField(pointerString: String?): String? {
        return if (pointerString == null || isEmpty(pointerString))
            null
        else {
            val path = pointerString.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (path.size > POINTER_LENGTH)
                path[path.size - 1]
            else
                null
        }
    }
}
