//
//  TParkMonthCell.h
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TParkMonthItem.h"

@class TParkMonthCell;
@protocol TParkMonthCellDelegate <NSObject>

- (void)buyButtonTouched:(TParkMonthItem*)item cell:(TParkMonthCell*)cell;

@end

@interface TParkMonthCell : UITableViewCell

@property(nonatomic, retain) TParkMonthItem* item;
@property(nonatomic, unsafe_unretained) id<TParkMonthCellDelegate> delegate;

@end


