// AMessageServer.aidl
package com.peng.ipc_server;
import com.peng.ipc_server.AMessageInfo;
import com.peng.ipc_server.AUpdataListener;

// Declare any non-default types here with import statements

interface AMessageServer {

    void sendMessage(inout AMessageInfo message);

    void registerListener(AUpdataListener listener);

    void unRegisterListener(AUpdataListener listener);
}
