@file:OptIn(ExperimentalContracts::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package me.hy.base.ext.view

import android.view.View
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.contracts.ExperimentalContracts

/**
 * <pre>
 *     author: dhl
 *     date  : 2021/3/29
 *     desc  :
 * </pre>
 */

// 感谢 FlowBinding
@kotlin.internal.InlineOnly
inline fun <E> SendChannel<E>.safeOffer(value: E) = !isClosedForSend && try {
    trySend(value).isSuccess
} catch (e: CancellationException) {
    false
}

@CheckResult
@kotlin.internal.InlineOnly
inline fun View.clickFlow(): Flow<View> {
    return callbackFlow {
        setOnClickListener {
            safeOffer(it)
        }
        awaitClose { setOnClickListener(null) }
    }
}

/**
 * Example：
 *
 * view.click(lifecycleScope) {
 *     showShortToast("公众号：ByteCode")
 * }
 */
@kotlin.internal.InlineOnly
inline fun View.click(lifecycle: LifecycleCoroutineScope? = null, noinline onClick: (view: View) -> Unit) {
    try {
        clickFlow().onEach {
            onClick(this)
        }.launchIn(lifecycle ?: this.findViewTreeLifecycleOwner()?.lifecycle?.coroutineScope!!)
    }catch (e:Exception) {
        LogUtils.e("view获取lifecycleOwner为null,可能已detach")
        e.printStackTrace()
    }
}

/**
 * 延迟第一次点击事件
 *
 * Example：
 *
 * view.clickDelayed(lifecycleScope) {
 *     showShortToast("公众号：ByteCode")
 * }
 */
@kotlin.internal.InlineOnly
inline fun View.clickDelayed(
    lifecycle: LifecycleCoroutineScope,
    delayMillis: Long = 500,
    noinline onClick: (view: View) -> Unit
) {
    clickFlow().onEach {
        delay(delayMillis)
        onClick(this)
    }.launchIn(lifecycle)
}


var lastMillis: Long = 0
/**
 * 防止多次点击
 * @param onRepeatClick:重复的无效点击
 * Example：
 *
 * view.clickTrigger(lifecycleScope) {
 *     showShortToast("公众号：ByteCode")
 * }
 */
@kotlin.internal.InlineOnly
inline fun View.clickTrigger(
    lifecycle: LifecycleCoroutineScope,
    intervalMillis: Long = 500,
    noinline onRepeatClick:(view : View) -> Unit = {},
    noinline onClick: (view: View) -> Unit
) {
    clickFlow().onEach {
        val currtMillis = System.currentTimeMillis()
        if (currtMillis - lastMillis < intervalMillis) {
            onRepeatClick(this)
            return@onEach
        }
        lastMillis = currtMillis
        onClick(this)
    }.launchIn(lifecycle)
}


