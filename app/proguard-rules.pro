# ProGuard rules for Relay release builds.
# Keep model/engine classes that are accessed via reflection by native libs.
-keep class com.schilling3003.relay.BuildConfig { *; }
-keep class com.schilling3003.relay.engines.** { *; }
-keep class com.schilling3003.relay.domain.** { *; }
-keep class com.schilling3003.relay.storage.** { *; }
-keep class com.schilling3003.relay.ui.** { *; }
-keep class com.schilling3003.relay.viewmodel.** { *; }
-keep class com.google.ai.edge.litertlm.** { *; }
-keep class ai.moonshine.voice.** { *; }

# Keep Kotlinx Serialization models and generated serializers.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep @kotlinx.serialization.Serializable class com.schilling3003.relay.domain.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.schilling3003.relay.domain.** { *; }
-keepnames class com.schilling3003.relay.domain.** { *; }
