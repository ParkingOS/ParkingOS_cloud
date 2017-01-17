//
//  TRecommendItem.h
//  TingCheBao_user
//
//  Created by apple on 14-8-26.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface TRecommendItem : NSObject

@property(nonatomic, retain) NSString* dist;//范围 两种：500,1000
@property(nonatomic, retain) NSString* freeinfo;//空闲车位情况：0紧张，1较少，2充足
@property(nonatomic, retain) NSString* name;//名字（suggest)
@property(nonatomic, retain) NSArray* monthids;
@property(nonatomic, retain) NSArray* bookids;
@property(nonatomic, retain) NSString* lon;
@property(nonatomic, retain) NSString* lat;
@property(nonatomic, retain) NSString* parkId;//id
@property(nonatomic, retain) NSString* price;
@property(nonatomic, retain) NSString* freeNum;// snumber
@property(nonatomic, retain) NSString* epay;// 是否支持手机支付
@property(nonatomic, retain) NSString* monthlypay;// 是否支持包月
//自定义
@property(nonatomic, retain) NSString* locationName;//搜索出来 要去的位置
@property(nonatomic, assign) int      hour;
@property(nonatomic, assign) int      minite;


//{"freeinfo":"0","monthids":[3,1175],"bookids":[3,1175],"suggest":"上地嘉华停车场","snumber":"6","lon":"116.31363","lat":"40.041917","id":"1196","price":"5.0元/720分钟"}

+(TRecommendItem*)getItemFromDictionary:(NSDictionary*)dic;

@end
