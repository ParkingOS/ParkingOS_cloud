//
//  TMyMonthItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TMyMonthItem : NSObject

@property(nonatomic, retain) NSString* parkname;//车场名字
@property(nonatomic, retain) NSString* price;//价格
@property(nonatomic, retain) NSString* limitdate;//日期
@property(nonatomic, retain) NSString* name;//月卡名字
@property(nonatomic, retain) NSString* limittime;//每天时间
@property(nonatomic, retain) NSString* limitday;//剩余天数

+(TMyMonthItem*) getItemFromeDictionary:(NSDictionary*)dic;

//[{"resume":"null","parkname":"团购月卡","price":"0.05","limitdate":"2014-08-14 至 2014-09-14","name":"测试川浙会停车场","limittime":"0:00 - 24:00","limitday":"0"},
@end
