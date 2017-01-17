//
//  TRechargeWaysViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-9-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"
#import "TChooseTicketItem.h"

typedef enum {
    RechargeMode_month = 0,//月卡
    RechargeMode_order,//订单
    RechargeMode_addMoney,//充值
    RechargeMode_collector,//收费员
    RechargeMode_buyTicket,//买券
} RechargeMode;

@interface TRechargeWaysViewController : TBaseViewController

@property(nonatomic, retain) NSString* productId;
@property(nonatomic, retain) NSString* name;
@property(nonatomic, retain) NSString* price;
@property(nonatomic, retain) NSString* orderId;
@property(nonatomic, retain) NSString* collectorId;
@property(nonatomic, retain) NSDate* startDate;
@property(nonatomic, retain) NSString* longTime;

@property(nonatomic, assign) RechargeMode rechargeMode;


@property(nonatomic, retain) TChooseTicketItem* ticketItem;

//是否打赏 如果是Yes,mode = collector
@property(nonatomic, assign) BOOL isReward;

//当mode== buyTicket
@property(nonatomic, retain)NSString* buyTicketMoney;
@property(nonatomic, retain)NSString* buyTicketNumber;

- (void)updateCommentState;

@end
