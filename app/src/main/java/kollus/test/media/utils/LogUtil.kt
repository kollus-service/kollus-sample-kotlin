package kollus.test.media.utils

import android.util.Log
import kollus.test.media.settings.AppConfig.Companion
import kollus.test.media.settings.AppConfig.Companion.BUILD_CONFIG_DEBUG

class LogUtil {
    private val mLogPreFix = "[kollus.test]"

    private enum class LogPrintType {
        VERBOSE {
            override fun printLog(tag: String, msg: String, tr: Throwable?) {
                if (tr != null) {
                    Log.v(tag, msg, tr)
                } else {
                    Log.v(tag, msg)
                }
            }
        },
        DEBUG {
            override fun printLog(tag: String, msg: String, tr: Throwable?) {
                if (tr != null) {
                    Log.d(tag, msg, tr)
                } else {
                    Log.d(tag, msg)
                }
            }
        },
        INFO {
            override fun printLog(tag: String, msg: String, tr: Throwable?) {
                if (tr != null) {
                    Log.i(tag, msg, tr)
                } else {
                    Log.i(tag, msg)
                }
            }
        },
        WARN {
            override fun printLog(tag: String, msg: String, tr: Throwable?) {
                if (tr != null) {
                    Log.w(tag, msg, tr)
                } else {
                    Log.w(tag, msg)
                }
            }
        },
        ERROR {
            override fun printLog(tag: String, msg: String, tr: Throwable?) {
                if (tr != null) {
                    Log.e(tag, msg, tr)
                } else {
                    Log.e(tag, msg)
                }
            }
        };

        internal abstract fun printLog(tag: String, msg: String, tr: Throwable?)
    }

    // static method
    companion object {
        private fun logPrint(
            logPrintType: LogPrintType,
            tag: String,
            msg: String,
            tr: Throwable?,
            isAlwaysShow: Boolean
        ) {
            if (BUILD_CONFIG_DEBUG || isAlwaysShow) {
                val element = Throwable().stackTrace[2]
                val currClassName = element.className
                var currClassSimpleName = ""
                if (currClassName != null) {
                    val nextIndexOfLastDot = currClassName.lastIndexOf(".") + 1
                    if (nextIndexOfLastDot > 0 && nextIndexOfLastDot < currClassName.length) {
                        currClassSimpleName = currClassName.substring(nextIndexOfLastDot)
                    }
                }
                val buildLogMsg = /*mLogPreFix + */
                    "[" + currClassSimpleName + "] " + element.methodName + "()" + "[" + element.lineNumber + "]" + " >> " + msg
                logPrintType.printLog(tag, buildLogMsg, tr)
            }
        }

        fun v(tag: String, msg: String) {
            logPrint(LogPrintType.VERBOSE, tag, msg, null, false)
        }

        fun v(tag: String, msg: String, isAlwaysShow: Boolean) {
            logPrint(LogPrintType.VERBOSE, tag, msg, null, isAlwaysShow)
        }

        fun d(tag: String, msg: String, isAlwaysShow: Boolean) {
            logPrint(LogPrintType.DEBUG, tag, msg, null, isAlwaysShow)
        }

        fun d(tag: String, msg: String) {
            logPrint(LogPrintType.DEBUG, tag, msg, null, false)
        }

        fun i(tag: String, msg: String) {
            logPrint(LogPrintType.INFO, tag, msg, null, false)
        }

        fun i(tag: String, msg: String, isAlwaysShow: Boolean) {
            logPrint(LogPrintType.INFO, tag, msg, null, isAlwaysShow)
        }

        fun w(tag: String, msg: String) {
            logPrint(LogPrintType.WARN, tag, msg, null, false)
        }

        fun w(tag: String, msg: String, isAlwaysShow: Boolean) {
            logPrint(LogPrintType.WARN, tag, msg, null, isAlwaysShow)
        }

        fun e(tag: String, msg: String) {
            logPrint(LogPrintType.ERROR, tag, msg, null, false)
        }

        fun e(tag: String, msg: String, isAlwaysShow: Boolean) {
            logPrint(LogPrintType.ERROR, tag, msg, null, isAlwaysShow)
        }
    }
}