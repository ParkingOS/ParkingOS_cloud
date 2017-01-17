//
//  TChooseTicketsController.h
//  TingCheBao_user
//
//  Created by apple on 15/4/27.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

//支付时选择停车券页面
#import "TBaseViewController.h"

@interface TChooseTicketsController : TBaseViewController

@property(nonatomic, retain) NSString* money;
@property(nonatomic, retain) NSString* orderId;
@property(nonatomic, retain) NSString* collectorId;
@property(nonatomic, retain) NSString* ticketId;

@property(nonatomic, assign) BOOL isReward;

@end
