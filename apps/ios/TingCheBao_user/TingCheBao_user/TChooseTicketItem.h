//
//  TChooseTicketItem.h
//  TingCheBao_user
//
//  Created by apple on 15/4/27.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TChooseTicketItem : NSObject

@property(nonatomic, retain) NSString* ticketId;//
@property(nonatomic, retain) NSString* limit_day;//到期时间
@property(nonatomic, retain) NSString* money;//券值
@property(nonatomic, retain) NSString* cname;//专用车场名字
@property(nonatomic, retain) NSString* type;// 0 通用  1专用车场
@property(nonatomic, retain) NSString* iscanuse;// 0不可用  1可用
@property(nonatomic, retain) NSString* desc;// 描述
@property(nonatomic, retain) NSString* limit;// 最大抵扣额
@property(nonatomic, retain) NSString* isbuy;// 是否是购买的停车券

@property(nonatomic, retain) NSString* moneyWhenBig2;//当券的值大于最大抵扣值，券的全额值

+ (TChooseTicketItem*)getItemFromDic:(NSDictionary*)dic;

@end
