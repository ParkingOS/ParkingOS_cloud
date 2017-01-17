//
//  TNearView.h
//  TingCheBao_user
//
//  Created by apple on 14-8-22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TRecommendItem.h"

@protocol TNearViewDelegate <NSObject>

- (void)monthButtonTouched;
- (void)bookButtonTouched;

@end
@interface TNearView : UIView

@property(nonatomic, unsafe_unretained) id<TNearViewDelegate>delegate;

- (void)setItem:(TRecommendItem *)item name:(NSString*)name;

@end
