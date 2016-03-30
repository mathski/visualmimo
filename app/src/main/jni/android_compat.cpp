
#include <stdio.h>
extern "C" {
  void debug_log_print(char* message) {
    printf("%s\n", message);
  }
}
