//
//  TParkMonthDetailBottomCell.h
//  TingCheBao_user
//
//  Created by apple on 14-10-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TParkMonthDetailItem.h"
#import "TParkMonthItem.h"

@protocol TParkMonthDetailBottomCellDelegate <NSObject>

- (void)cellCommentTouched;

@end
@interface TParkMonthDetailBottomCell : UITableViewCell

@property(nonatomic, retain) TParkMonthDetailItem* item;
@property(nonatomic, retain) TParkMonthItem* monthItem;
@property(nonatomic, unsafe_unretained) id<TParkMonthDetailBottomCellDelegate>delegate;

- (void)setItem:(TParkMonthDetailItem *)detailItem monthItem:(TParkMonthItem*)monthItem;

@end
