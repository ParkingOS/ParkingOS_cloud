//
//  TPriceItem.h
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TPriceItem : NSObject

//白天
@property(nonatomic, retain) NSString* price;//正常价格
@property(nonatomic, retain) NSString* unit;//价格单位
@property(nonatomic, retain) NSString* b_time;//白天开始时间
@property(nonatomic, retain) NSString* e_time;//白天结束时间
@property(nonatomic, retain) NSString* first_times;// 白天首优惠时长
@property(nonatomic, retain) NSString* fprice;// 白天首优惠价格
@property(nonatomic, retain) NSString* fpay_type;//白天超过免费时长后，免费时长是否计费？1-->免费；0-->收费,备注中用到
@property(nonatomic, retain) NSString* free_time;//单位－分钟 免费时长，在“备注“中用到
//晚上   n代表晚上
@property(nonatomic, retain) NSString* nprice;
@property(nonatomic, retain) NSString* nunit;
@property(nonatomic, retain) NSString* nfirst_times;
@property(nonatomic, retain) NSString* nfprice;
@property(nonatomic, retain) NSString* nfpay_type;
@property(nonatomic, retain) NSString* nfree_time;
//是否支持夜间
@property(nonatomic, retain) NSString* isnight;

//{“id":"524","price":"2.00","unit":"15","pay_type":"0","b_time":"7","e_time":"21","first_times":"60","fprice":"1.00","countless":"0","fpay_type":"0","free_time":"1","nid":"525","nprice":"0.01","nunit":"30","nfirst_times":"60","ncountless":"0","npay_type":"0","nfpay_type":"0","nfree_time":"0","nfprice":"0.02","isnight":"0"}

+(TPriceItem*)getItemFromDictionary:(NSDictionary*)dic;

@end
