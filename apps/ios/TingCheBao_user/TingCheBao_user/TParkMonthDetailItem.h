//
//  TParkMonthDetailItem.h
//  TingCheBao_user
//
//  Created by apple on 14-10-9.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TParkMonthDetailItem : NSObject

@property(nonatomic, retain) NSString* resume;
@property(nonatomic, retain) NSString* limitday;
@property(nonatomic, retain) NSString* isbuy;
@property(nonatomic, retain) NSString* company_name;
@property(nonatomic, retain) NSString* address;
@property(nonatomic, retain) NSString* mobile;
@property(nonatomic, retain) NSString* parkId;
@property(nonatomic, retain) NSString* praiseNum;
@property(nonatomic, retain) NSString* disparageNum;
@property(nonatomic, retain) NSString* longitude;
@property(nonatomic, retain) NSString* latitude;
@property(nonatomic, retain) NSString* commentnum;

//{"resume":"Phone4s,安卓4.3版本用户购买后即可畅行无阻","limitday":"1444447063","isbuy":"0","parkinfo":{"company_name":"川浙会停车场","address":"北京市海淀区创业路18号","mobile":"","id":"10","praiseNum":"3","disparageNum":"1","longitude":"116.320037","latitude":"40.042315","commentnum":"9"}}
+(TParkMonthDetailItem*)getItemFromDictionary:(NSDictionary*)dic;
    
@end
