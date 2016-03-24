// Need to return two values from processFrames.

#ifndef JNI_NDK_RESULT_H
#define JNI_NDK_RESULT_H

struct NDK_RESULT {
    int index;
    int mismatches;
    // message is passed by reference already
};

#endif //JNI_NDK_RESULT_H
