package kollus.test.media.hybrid

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Bridge(val value: String)
