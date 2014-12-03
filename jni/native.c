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
void Java_com_android_visualmimo_MainActivity_frameSubtraction(JNIEnv *env, jobject obj,
		jbyteArray frame1, jbyteArray frame2,
		jint width, jint height,
		jfloatArray corners1, jfloatArray corners2)
{
	//f1 and f2 are the byte data of the two frames, l1 and l2 are the array lengths
	jbyte* f1 = (*env)->GetByteArrayElements(env, frame1, NULL);
	jbyte* f2 = (*env)->GetByteArrayElements(env, frame2, NULL);
	jsize l1 = (*env)->GetArrayLength(env, frame1);
//	jsize l2 = (*env)->GetArrayLength(env, frame2);

	//c1 and c2 are float[4][2]: four sets of two points.
	jfloat* corners1Temp =  (*env)->GetFloatArrayElements(env, corners1, NULL);
	jfloat* corners2Temp =  (*env)->GetFloatArrayElements(env, corners2, NULL);
	float* c1 = corners1Temp;
	float* c2 = corners2Temp;

	//TODO: use OpenCV magic here

	//subtraction demo
	int i;
	for (i = 0; i < l1; i++) {
		f1[i] = f1[i] - f2[i];
	}

	//last arg: 0 -> copy array back, JNI_ABBORT -> don't copy
	(*env)->ReleaseByteArrayElements(env, frame1, f1, 0);
	(*env)->ReleaseByteArrayElements(env, frame2, f2, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, corners1, corners1Temp, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, corners2, corners2Temp, JNI_ABORT);
}
