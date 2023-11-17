package me.hy.jetpackmvvm.state
import androidx.lifecycle.MutableLiveData
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import me.hy.jetpackmvvm.network.AppException
import me.hy.jetpackmvvm.network.BaseResponse
import me.hy.jetpackmvvm.network.ExceptionHandle

/**
 * 作者　: hegaojian
 * 时间　: 2020/4/9
 * 描述　: 自定义结果集封装类
 */
sealed class ResultState<out T> {
    companion object {
        fun <T> onAppSuccess(data: T): ResultState<T> = Success(data)
        fun <T> onAppLoading(loadingMessage: String): ResultState<T> = Loading(loadingMessage)
        fun <T> onAppError(error: AppException): ResultState<T> = Error(error)
    }

    data class Loading(val loadingMessage: String) : ResultState<Nothing>()
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val error: AppException) : ResultState<Nothing>()
}

/**
 * 处理返回值
 * @param result 请求结果
 */
fun <T> MutableLiveData<ResultState<T>>.paresResult(result: BaseResponse<T>) {
    value = when {
        result.isSuccess() -> {
            ResultState.onAppSuccess(result.getResponseData())
        }
        else -> {

            ResultState.onAppError(AppException(result.getResponseCode(), result.getResponseMsg()))
        }
    }
}


/**
 * 处理返回值
 * @param result 请求结果
 */
fun <T> UnPeekLiveData<ResultState<T>>.paresResult(result: BaseResponse<T>) {
    value = when {
        result.isSuccess() -> {
            ResultState.onAppSuccess(result.getResponseData())
        }
        else -> {

            ResultState.onAppError(AppException(result.getResponseCode(), result.getResponseMsg()))
        }
    }
}

/**
 * 处理返回值
 *
 * @param result 请求结果
 */
fun <T> MutableLiveData<ResultState<T>>.paresResult(result: BaseResponse<T>,success:(T)->Unit) {
    value = when {
        result.isSuccess() -> {
            success.invoke(result.getResponseData())
            ResultState.onAppSuccess(result.getResponseData())
        }
        else -> {
            ResultState.onAppError(AppException(result.getResponseCode(), result.getResponseMsg()))
        }
    }
}

/**
 * 处理返回值
 *
 * @param result 请求结果
 */
fun <T> UnPeekLiveData<ResultState<T>>.paresResult(result: BaseResponse<T>, success:(T)->Unit) {
    value = when {
        result.isSuccess() -> {
            success.invoke(result.getResponseData())
            ResultState.onAppSuccess(result.getResponseData())
        }
        else -> {
            ResultState.onAppError(AppException(result.getResponseCode(), result.getResponseMsg()))
        }
    }
}

/**
 * 不处理返回值 直接返回请求结果
 * @param result 请求结果
 */
fun <T> MutableLiveData<ResultState<T>>.paresResult(result: T) {
    value = ResultState.onAppSuccess(result)
}

/**
 * 不处理返回值 直接返回请求结果
 * @param result 请求结果
 */
fun <T> UnPeekLiveData<ResultState<T>>.paresResult(result: T) {
    value = ResultState.onAppSuccess(result)
}


/**
 * 异常转换异常处理
 */
fun <T> MutableLiveData<ResultState<T>>.paresException(e: Throwable) {
    this.value = ResultState.onAppError(ExceptionHandle.handleException(e))
}


/**
 * 异常转换异常处理
 */
fun <T> UnPeekLiveData<ResultState<T>>.paresException(e: Throwable) {
    this.value = ResultState.onAppError(ExceptionHandle.handleException(e))
}

