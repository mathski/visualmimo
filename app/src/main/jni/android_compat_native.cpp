#include <android/log.h>
#define DEBUG_TAG "NDK_VMIMO"
extern "C" {
  void debug_log_print(char *message) {
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "%s\n", message);
  }
}

