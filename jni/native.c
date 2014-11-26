#include <jni.h>
#include <string.h>
#include <android/log.h>

#define DEBUG_TAG "NDK_AndroidNDK1SampleActivity"

void Java_com_android_visualmimo_MainActivity_helloLog(JNIEnv * env, jobject this, jstring logThis)
{
    jboolean isCopy;
    const char * szLogThis = (*env)->GetStringUTFChars(env, logThis, &isCopy);

    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis);

    (*env)->ReleaseStringUTFChars(env, logThis, szLogThis);
}

/**
 * Subtracts frame2 from frame1, overwriting frame1
 */
void Java_com_android_visualmimo_MainActivity_frameSubtraction(JNIEnv *env, jobject obj, jbyteArray frame1, jbyteArray frame2)
{
	jbyte* f1 = (*env)->GetByteArrayElements(env, frame1, NULL);
	jsize l1 = (*env)->GetArrayLength(env, frame1);
	jbyte* f2 = (*env)->GetByteArrayElements(env, frame2, NULL);
//	jsize l2 = (*env)->GetArrayLength(env, frame2);

	//subtraction
	int i;
	for (i = 0; i < l1; i++) {
		f1[i] = f1[i] - f2[i];
	}

	(*env)->ReleaseByteArrayElements(env, frame1, f1, 0);
	(*env)->ReleaseByteArrayElements(env, frame2, f2, JNI_ABORT);
}
