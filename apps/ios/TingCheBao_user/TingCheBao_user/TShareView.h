//
//  TShareView.h
//  TingCheBao_user
//
//  Created by apple on 14/12/25.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

//微信分享
#import <UIKit/UIKit.h>
#import "TShareItem.h"

@class TShareView;
@protocol TShareViewDelegate <NSObject>

- (void)shareViewTouched:(TShareView*)shareView index:(NSInteger)index;

@end
@interface TShareView : UIView

@property(nonatomic, unsafe_unretained) id<TShareViewDelegate>delegate;
@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) TShareItem* item;

@end
