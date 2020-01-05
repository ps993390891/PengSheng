// AServerManager.aidl
package com.peng.ipc_server;

// Declare any non-default types here with import statements

interface AServerManager {
    IBinder getServer(String name);
}
