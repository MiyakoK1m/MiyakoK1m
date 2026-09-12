# Apache POI reflectively loads implementations; keep everything to be safe for release builds.
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class schemaorg_apache_xmlbeans.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn javax.xml.stream.**
-dontwarn org.w3c.dom.**
