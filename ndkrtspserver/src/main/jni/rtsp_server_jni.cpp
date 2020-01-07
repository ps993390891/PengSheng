//
// Created by pengsheng on 20-1-2.
//

#include <jni.h>
#include "xop/RtspServer.h"
#include "net/NetInterface.h"
#include "net/Timer.h"
#include <thread>
#include <memory>
#include <iostream>
#include <string>
#include "Logger.h"

#ifndef PENGSHENG_RTSP_SERVER_JNI_H
#define PENGSHENG_RTSP_SERVER_JNI_H


#ifdef __cplusplus
extern "C" {
#endif


xop::RtspServer* mrtspServer;
xop::MediaSessionId msessionId;
int client_number = 0;
int audio_type = 0;
JavaVM *g_VM;
jobject jcallback;
jmethodID javaCallbackId;
jclass javaClass;

JNIEXPORT void JNICALL
Java_com_peng_jni_RtspServerJni_startRtspServer(JNIEnv *env, jobject thiz, jint prot, jint type,jobject listener) {
    std::string ip = xop::NetInterface::getLocalIPAddress();
    std::string rtspUrl;

    std::shared_ptr<xop::EventLoop> eventLoop(new xop::EventLoop());
    xop::RtspServer server(eventLoop.get(), "0.0.0.0", prot);

#ifdef AUTH_CONFIG
    server.setAuthConfig("-_-", "admin", "12345");
#endif
    char str_prot[10];
    sprintf(str_prot, "%d", prot);
    xop::MediaSession *session = xop::MediaSession::createNew("1");
    rtspUrl = "rtsp://" + ip + ":"+ str_prot +"/" + session->getRtspUrlSuffix();
    session->addMediaSource(xop::channel_0, xop::H264Source::createNew());
    audio_type = type;
    if(type==0){
        session->addMediaSource(xop::channel_1, xop::AACSource::createNew(8000,1,false));
    }else{
        session->addMediaSource(xop::channel_1, xop::G711ASource::createNew());
    }
    //JavaVM是虚拟机在JNI中的表示，等下再其他线程回调java层需要用到
    env->GetJavaVM(&g_VM);
    jcallback = env->NewGlobalRef(listener);
    javaClass = env->GetObjectClass(listener);
    //获取要回调的方法ID
    javaCallbackId = env->GetMethodID(javaClass,"onClientNumber", "(I)V");

    //session->startMulticast();  /* enable multicast */
    session->setNotifyCallback([](xop::MediaSessionId sessionId, uint32_t clients) {
        LOGE("--->Number of rtsp client %d: ", clients);
        client_number = clients;
        JNIEnv *m_env = nullptr;
        if(g_VM->AttachCurrentThread(&m_env, nullptr) == 0){
            m_env->CallVoidMethod(jcallback,javaCallbackId,client_number);
            g_VM->DetachCurrentThread();
        }

    });
    xop::MediaSessionId sessionId = server.addMeidaSession(session);
    mrtspServer = &server;
    msessionId = sessionId;
    LOGE("--->Play URL:%s", rtspUrl.c_str());

    while (1) {
        xop::Timer::sleep(200);
    }
}


JNIEXPORT void JNICALL
Java_com_peng_jni_RtspServerJni_sendVideo(JNIEnv *env, jobject thiz, jbyteArray data, jint size) {
    if(client_number == 0){
        return;
    }
    xop::AVFrame videoFrame = {0};
    videoFrame.type = 0;
    videoFrame.size = size;
    videoFrame.timestamp = xop::H264Source::getTimeStamp();
    videoFrame.buffer.reset(new uint8_t[videoFrame.size]);
    jbyte *c_array = env->GetByteArrayElements(data, JNI_FALSE);
    memcpy(videoFrame.buffer.get(), c_array, videoFrame.size);
    mrtspServer->pushFrame(msessionId, xop::channel_0, videoFrame);
    env->ReleaseByteArrayElements(data, c_array, JNI_FALSE);
}

char* ConvertJByteaArrayToChars(JNIEnv *env, jbyteArray bytearray)
{
    char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(bytearray, 0);
    int chars_len = env->GetArrayLength(bytearray);
    chars = new char[chars_len + 1];
    memset(chars,0,chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;
    env->ReleaseByteArrayElements(bytearray, bytes, 0);
    return chars;
}




JNIEXPORT void JNICALL
Java_com_peng_jni_RtspServerJni_sendAudio(JNIEnv *env, jobject thiz, jbyteArray data, jint size) {
    if(client_number == 0){
        return;
    }
    xop::AVFrame audioFrame = {0};
    audioFrame.type = xop::AUDIO_FRAME;
    audioFrame.size = size;
    if(audio_type == 0){
        audioFrame.timestamp = xop::AACSource::getTimeStamp(8000); // 时间戳
    } else{
        audioFrame.timestamp = xop::G711ASource::getTimeStamp(); // 时间戳
    }
    audioFrame.buffer.reset(new uint8_t[audioFrame.size]);
    jbyte *c_array = env->GetByteArrayElements(data, JNI_FALSE);
    memcpy(audioFrame.buffer.get(), c_array, audioFrame.size);
    mrtspServer->pushFrame(msessionId, xop::channel_1, audioFrame);
    env->ReleaseByteArrayElements(data, c_array, JNI_FALSE);
}

#ifdef __cplusplus
}
#endif
#endif