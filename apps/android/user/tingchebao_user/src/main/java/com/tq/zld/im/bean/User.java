/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tq.zld.im.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.easemob.chat.EMContact;

public class User extends EMContact implements Parcelable {
	private int unreadMsgCount = 0;
	private String header;
	private String avatar;
	private String reason;
	private String plate;
	
	public User(){}
	
	public User(String username){
	    this.username = username;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public int getUnreadMsgCount() {
		return unreadMsgCount;
	}

	public void unreadMsgCountIncrease(){
		this.unreadMsgCount++;
	}

	public void setUnreadMsgCount(int unreadMsgCount) {
		this.unreadMsgCount = unreadMsgCount;
	}
	
	public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getPlate() {
		return plate;
	}

	public void setPlate(String plate) {
		this.plate = plate;
	}

	@Override
	public int hashCode() {
		return 17 * getUsername().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof User)) {
			return false;
		}
		return getUsername().equals(((User) o).getUsername());
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(this.username);
		parcel.writeString(this.nick);
		parcel.writeString(this.avatar);
		parcel.writeString(this.plate);
		parcel.writeString(this.reason);
	}

	protected User(Parcel parcel){
		this.username = parcel.readString();
		this.nick = parcel.readString();
		this.avatar = parcel.readString();
		this.plate = parcel.readString();
		this.reason = parcel.readString();
	}

	@Override
	public String toString() {
		return "User{" +
				"name=" + this.username +
				",nick=" + this.nick +
				",avatar='" + avatar + '\'' +
				", reason='" + reason + '\'' +
				", plate='" + plate + '\'' +
				'}';
	}
}
