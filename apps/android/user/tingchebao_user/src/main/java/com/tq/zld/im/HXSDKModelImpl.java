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
package com.tq.zld.im;

import android.content.Context;

import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.DemoDBManager;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.im.lib.DefaultHXSDKModel;

import java.util.List;
import java.util.Map;

public class HXSDKModelImpl extends DefaultHXSDKModel {

    public HXSDKModelImpl(Context ctx) {
        super(ctx);
    }

    public boolean getUseHXRoster() {
        return true;
    }
    
    // demo will switch on debug mode
    public boolean isDebugMode(){
        return true;
    }
    
    public boolean saveContactList(List<User> contactList) {
        UserDao dao = new UserDao(context);
        dao.saveContactList(contactList);
        return true;
    }

    public Map<String, User> getContactList() {
        UserDao dao = new UserDao(context);
        return dao.getContactList();
    }
    
    public void saveContact(User user){
    	UserDao dao = new UserDao(context);
    	dao.saveContact(user);
    }

    public void closeDB() {
        DemoDBManager.getInstance().closeDB();
    }
    
    @Override
    public String getAppProcessName() {
        return context.getPackageName();
    }
}
