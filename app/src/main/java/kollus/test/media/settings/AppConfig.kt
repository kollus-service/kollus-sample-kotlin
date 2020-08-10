package kollus.test.media.settings

import kollus.test.media.BuildConfig

class AppConfig {
    companion object {
        // App Config
        var BUILD_CONFIG_DEBUG = BuildConfig.DEBUG
        var MODE_MAKE_JWT = true

        // Console Account config
        val SECURITY_KEY = "KOLLUS_SECURITY_KEY"
        val CUSTOM_KEY = "KOLLUS_CUSTOM_KEY"
    }
}