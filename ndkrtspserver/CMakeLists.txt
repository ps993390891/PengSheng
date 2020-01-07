cmake_minimum_required(VERSION 3.4.1)
# 这一句用来导出编译生成的so库
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})
#支持-std=gnu++11
#set(CMAKE_VERBOSE_MAKEFILE on)
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O1,O2,O3,Os,Ofast")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O1,O2,O3,Os,Ofast")
#LOCAL_CFLAGS += -Ofast
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Ofast,fopenmp,ffast-math,ffunction-sections,fdata-sections,Wl")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Ofast,fopenmp,ffast-math,ffunction-sections,fdata-sections,Wl")
include_directories(src/main/jni)
include_directories(src/main/jni/net)
include_directories(src/main/jni/xop)
include_directories(src/main/jni/md5)

file(GLOB C_FILES1 "src/main/jni/*.cpp")
file(GLOB C_FILES2 "src/main/jni/net/*.cpp")
file(GLOB C_FILES3 "src/main/jni/xop/*.cpp")
file(GLOB C_FILES4 "src/main/jni/md5/*.cpp")

add_library(
    # 自己的库名字
	rtsp_server_jni
    # 将自己的库编译成动态库
	SHARED
	# 自己的cpp源码所在的路径即jni源码的路径
	${C_FILES1} ${C_FILES4} ${C_FILES2} ${C_FILES3}
)

#从系统中查找依赖库
find_library(
		log-lib
		log
)

target_link_libraries(
		rtsp_server_jni
		${log-lib} )

