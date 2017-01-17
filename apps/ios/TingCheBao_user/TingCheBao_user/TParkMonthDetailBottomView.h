//
//  TParkMonthDetailBottomView.h
//  TingCheBao_user
//
//  Created by apple on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TParkMonthDetailItem.h"
#import "TParkMonthItem.h"

@interface TParkMonthDetailBottomView : UIView

@property(nonatomic, retain) TParkMonthDetailItem* item;
@property(nonatomic, retain) TParkMonthItem* monthItem;

- (void)setItem:(TParkMonthDetailItem *)detailItem monthItem:(TParkMonthItem*)monthItem;

@end
