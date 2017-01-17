//
//  TTicketItem.h
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TTicketItem : NSObject

@property(nonatomic, retain) NSString* beginday;
@property(nonatomic, retain) NSString* limitday;
@property(nonatomic, retain) NSString* state;//state:0未使用，1已使用
@property(nonatomic, retain) NSString* money;
@property(nonatomic, retain) NSString* exp;//exp:0已过期，1未过期
@property(nonatomic, retain) NSString* cname;//"已使用" 当时使用的车场
@property(nonatomic, retain) NSString* utime;//"已使用" 当时使用时间
@property(nonatomic, retain) NSString* umoney;//"已使用" 当时金额
@property(nonatomic, retain) NSString* type;//是否专用
@property(nonatomic, retain) NSString* desc;//描述
@property(nonatomic, retain) NSString* isbuy;//1:自己购买的停车券 0则反之


@property(nonatomic, retain) NSString* ticketId;//只有查可用的停车券  会用到

//未使用 未过期 ->当前 其它的情况 是历史
+ (TTicketItem*)getItemFromDic:(NSDictionary*)dic;

//历史券 按时间排序 
+ (NSArray*)orderByLimitday:(NSArray*) items;

@end
