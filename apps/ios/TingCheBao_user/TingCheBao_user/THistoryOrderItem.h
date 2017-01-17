//
//  THistoryOrderItem.h
//  TingCheBao_user
//
//  Created by apple on 14/10/19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface THistoryOrderItem : NSObject

//{"total":"1.5","parkname":"测试中关村停车场3","orderid":"161050","date":"2014-10-18"}
@property(nonatomic, retain) NSString* total;
@property(nonatomic, retain) NSString* parkname;
@property(nonatomic, retain) NSString* orderid;
@property(nonatomic, retain) NSString* date;

+ (THistoryOrderItem*)getItemFromDictionary:(NSDictionary*)dic;

@end
