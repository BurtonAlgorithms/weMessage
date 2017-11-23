#
# This ProGuard configuration file will process the weServer Application.
# Usage:
#     java -jar proguard.jar @rules.pro
#

# Input jars, output, and library jars

-libraryjars <java.home>/lib/rt.jar
-libraryjars ../build/dependencies.jar

-injars ../build/obfuscate/weServer.jar
-outjars ../build/weServer.jar

# Metadata for the Release Jar
-target 1.7

# Save the obfuscation mapping to a file, so you can de-obfuscate any stack
# traces later on. Keep a fixed source file attribute and all line number
# tables to get line numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-printmapping out.map
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Preserve all annotations, signatures, exceptions, and inner classes.

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

# You can print out the seeds that are matching the keep options below.

#-printseeds out.seeds

# Preserve all public applications.

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# Preserve all native method names and the names of their classes.

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your application doesn't use serialization.
# If your code contains serializable classes that have to be backward 
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Disable shrinking as well as warnings for certain packages.

-dontshrink
-dontwarn javax.**


##----------------------  Commons  ----------------------

-keepnames class scott.wemessage.commons.connection.**
-keep,allowobfuscation class scott.wemessage.commons.** { *; }

-keep class scott.wemessage.commons.connection.Heartbeat { *; }
-keep class scott.wemessage.commons.connection.security.EncryptedFile { *; }

##--------------------------------------------------------


##----------------------  Server  ------------------------

-keepnames class scott.wemessage.server.utils.ScriptError
-keepnames class scott.wemessage.server.configuration.json.**

-keep,allowobfuscation class scott.wemessage.server.utils.ScriptError { *; }
-keep,allowobfuscation class scott.wemessage.server.configuration.json.** { *; }

##--------------------------------------------------------