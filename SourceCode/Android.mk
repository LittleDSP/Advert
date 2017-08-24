LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := KKAdvert
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED:= disabled

LOCAL_STATIC_JAVA_LIBRARIES := local_android-support-v4 \
								commons-codec \
								gson \
								local_okhttp \
								okio \
								sw-mstar-swmvpCurr \
								sw-servutil
								

include $(BUILD_PACKAGE)


include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := local_android-support-v4:libs/android-support-v4.jar \
										commons-codec:libs/commons-codec-1.10.jar \
										gson:libs/gson-2.8.0.jar \
										local_okhttp:libs/okhttp-3.4.2.jar \
										okio:libs/okio-1.11.0.jar \
										sw-mstar-swmvpCurr:libs/sw-mstar-swmvpCurr-20170303.jar \
										sw-servutil:libs/sw-servutil-20170306.jar
										
include $(BUILD_MULTI_PREBUILT)