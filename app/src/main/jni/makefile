
CC := g++
CFLAGS := -g $(shell pkg-config opencv --cflags)
LDFLAGS :=$(shell pkg-config opencv --libs)
# LDFLAGS := -lopencv_core -lopencv_highgui -lopencv_imgproc

SOURCES := android_compat.cpp image_processing.cpp helpers.cpp simulator.cpp

all: android_compat image_processing helpers simulator
	$(CC) $(CFLAGS) simulator.o android_compat.o image_processing.o helpers.o $(LDFLAGS) -o simulator

simulator: simulator.cpp
	$(CC) $(LDFLAGS) $(CFLAGS) -c simulator.cpp

android_compat: android_compat.cpp
	$(CC) $(LDFLAGS) $(CFLAGS) -c android_compat.cpp

image_processing: image_processing.cpp
	$(CC) $(LDFLAGS) $(CFLAGS) -c image_processing.cpp

helpers: helpers.cpp
	$(CC) $(LDFLAGS) $(CFLAGS) -c helpers.cpp

clean:
	rm -rf android_compat.o image_processing.o helpers.o simulator.o simulator