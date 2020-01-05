// AConnectionServer.aidl
package com.peng.ipc_server;

// Declare any non-default types here with import statements
interface AConnectionServer {
    oneway void connectServer();
    void disconnectServer();
    boolean isConnectServer();
}
