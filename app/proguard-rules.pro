# ProGuard rules for Relay release builds.
# Keep model/engine classes that are accessed via reflection by native libs.
-keep class com.schilling3003.relay.engines.** { *; }
-keep class com.schilling3003.relay.domain.** { *; }
-keep class com.schilling3003.relay.storage.** { *; }
-keep class com.google.ai.edge.litertlm.** { *; }
-keep class ai.moonshine.voice.** { *; }

# Keep kotlinx serialization and coroutines internals.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class kotlinx.serialization.json.** { *; }
