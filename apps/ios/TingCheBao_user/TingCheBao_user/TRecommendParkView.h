//
//  TRecommendParkView.h
//  TingCheBao_user
//
//  Created by apple on 14/10/23.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "TRecommendItem.h"

//这个类暂时废弃
//这个类暂时废弃
//这个类暂时废弃
typedef enum {
    RecommendModeOnlyShowBottom = 0,
    RecommendModeShowAll,
    RecommendModeNoRecommendInfo
} RecommendMode;

#define recommend_line_color [UIColor lightGrayColor]

@protocol TRecommendParkViewDelegate<NSObject>

- (void)monthButtonTouched;
- (void)bookButtonTouched;
- (void)recommendDetailButtonTouched;

@end
@interface TRecommendParkView : UIView

@property(nonatomic, unsafe_unretained) id<TRecommendParkViewDelegate>delegate;
@property(nonatomic, assign) RecommendMode mode;
@property(nonatomic, retain) TRecommendItem* item;

@end
