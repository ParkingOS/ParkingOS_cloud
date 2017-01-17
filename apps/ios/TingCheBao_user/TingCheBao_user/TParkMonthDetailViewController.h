//
//  TParkMonthDetailViewController.h
//  TingCheBao_user
//
//  Created by apple on 14-9-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"
#import "TParkMonthDetailItem.h"
#import "TParkMonthItem.h"

@interface TParkMonthDetailViewController : TBaseViewController

@property(nonatomic, retain) TParkMonthItem* monthItem;
@property(nonatomic, retain) TParkMonthDetailItem* detailItem;

@end
