package com.peng.ipc_server;

import android.os.Parcel;
import android.os.Parcelable;

public class AMessageInfo implements Parcelable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
    }

    public void readFromParcel(Parcel reply){
        name = reply.readString();
    }

    public AMessageInfo() {
    }

    protected AMessageInfo(Parcel in) {
        this.name = in.readString();
    }

    public static final Parcelable.Creator<AMessageInfo> CREATOR = new Parcelable.Creator<AMessageInfo>() {
        @Override
        public AMessageInfo createFromParcel(Parcel source) {
            return new AMessageInfo(source);
        }

        @Override
        public AMessageInfo[] newArray(int size) {
            return new AMessageInfo[size];
        }
    };
}
