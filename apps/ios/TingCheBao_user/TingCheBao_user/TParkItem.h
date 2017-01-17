//
//  TParkItem.h
//  TingCheBao_user
//
//  Created by apple on 14-8-21.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TParkItem : NSObject<NSCoding>

@property(nonatomic, retain) NSString* parkId;//车场id
@property(nonatomic, retain) NSString* name;//车场名字
@property(nonatomic, retain) NSString* addr;//地址
@property(nonatomic, retain) NSString* phone;//手机号
@property(nonatomic, retain) NSString* lng;//纬度
@property(nonatomic, retain) NSString* lat;//经度
@property(nonatomic, retain) NSString* free;//空闲车位数
@property(nonatomic, retain) NSString* total;//总车位
@property(nonatomic, retain) NSString* epay;//是否支持手机支付
@property(nonatomic, retain) NSString* monthlypay;//是否支持月卡
@property(nonatomic, retain) NSString* price;//当前价格（元/每小时，负数表示免费，正数表示有价格，0或“”表示没有价格信息）
@property(nonatomic, retain) NSString* suggested;//是否推荐  1:是
@property(nonatomic, retain) NSArray* photo_url;//车场照片的url地址集合

+(id)getItemFromDictionary:(NSDictionary*)dic;

@end
