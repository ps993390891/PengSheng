package com.peng.ipc_server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class IpcService extends Service {

    private final AServerManager.Stub aServerManager = new AServerManager.Stub() {
        @Override
        public IBinder getServer(String name) throws RemoteException {
            if(name.equals(AMessageServer.class.getSimpleName())){
                return aMessageServer;
            }else if(name.equals(AConnectionServer.class.getSimpleName())){
                return aConnectionServer;
            }
            return null;
        }
    };

    private final AMessageServer.Stub aMessageServer = new AMessageServer.Stub() {
        @Override
        public void sendMessage(AMessageInfo message) throws RemoteException {

        }

        @Override
        public void registerListener(AUpdataListener listener) throws RemoteException {

        }

        @Override
        public void unRegisterListener(AUpdataListener listener) throws RemoteException {

        }
    };

    private final AConnectionServer.Stub aConnectionServer = new AConnectionServer.Stub() {
        @Override
        public void connectServer() throws RemoteException {

        }

        @Override
        public void disconnectServer() throws RemoteException {

        }

        @Override
        public boolean isConnectServer() throws RemoteException {
            return false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return aServerManager;
    }
}
