//
//  TParkMonthDetailSectionView.h
//  TingCheBao_user
//
//  Created by apple on 14-10-10.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TParkMonthItem.h"

@protocol TParkMonthDetailSectionViewDelegate <NSObject>

- (void)sectionBuyButtonTouched;

@end
@interface TParkMonthDetailSectionView : UIView

@property(nonatomic, retain) TParkMonthItem* item;
@property(nonatomic, unsafe_unretained)id<TParkMonthDetailSectionViewDelegate>delegate;

@end
