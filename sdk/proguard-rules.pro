# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.idme.auth.**$$serializer { *; }
-keepclassmembers class com.idme.auth.** { *** Companion; }
-keepclasseswithmembers class com.idme.auth.** { kotlinx.serialization.KSerializer serializer(...); }
