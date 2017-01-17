//
//  TParkMonthDetailTopView.h
//  TingCheBao_user
//
//  Created by apple on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TParkMonthItem.h"
@interface TParkMonthDetailTopView : UIView

@property(nonatomic, retain) UIImageView* infoImgView;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* numberLabel;
@property(nonatomic, retain) UILabel* monthLabel;

@property(nonatomic, retain) TParkMonthItem* item;

@end
