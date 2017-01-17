//
//  TParkMonthItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TParkMonthItem : NSObject

@property(nonatomic, retain) NSString* productId;//是否购买
@property(nonatomic, retain) NSString* price0;//原价
@property(nonatomic, retain) NSString* isbuy;//是否购买
@property(nonatomic, retain) NSString* reserved;//是否固定车位：0 不固定 1 固定
@property(nonatomic, retain) NSString* price;//价格
@property(nonatomic, retain) NSString* name;//名称
@property(nonatomic, retain) NSString* number;//剩余数量
@property(nonatomic, retain) NSString* limitday;//有效期至
@property(nonatomic, retain) NSString* limittime;//有效时段
@property(nonatomic, retain) NSArray* photoUrl;//所有图片
@property(nonatomic, retain) NSString* type;//产品类型：全天包月（0），夜间包月（1），日间包月（2）

+ (TParkMonthItem*)getItemFromDictionary:(NSDictionary*)dic;

//id = 46;
//isbuy = 1;
//limitday = 1508169600;
//limittime = "8:00-8:00";
//name = 11111;
//number = 109;
//photoUrl =             (
//                        "parkpics/1197_1409730218.jpeg",
//                        "parkpics/1197_1409155103.jpeg"
//                        );
//price = "0.01";
//price0 = "11.00";
//reserved = 0;
//type = 0;

@end
