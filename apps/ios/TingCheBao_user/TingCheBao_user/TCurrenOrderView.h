//
//  TCurrenOrderView.h
//  TingCheBao_user
//
//  Created by apple on 14/12/9.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TBlurView.h"
#import "TCurrentOrderItem.h"

//这个类暂时废弃
//这个类暂时废弃
//这个类暂时废弃
typedef void(^CurrentScanBlock)();
typedef void(^CurrentCloseBlock)();

@interface TCurrenOrderView : UIView

@property(nonatomic, retain) UIActivityIndicatorView* activityView;
@property(nonatomic, retain) TBlurView* blurView;
@property(nonatomic, retain) TCurrentOrderItem* item;

@property(nonatomic, copy) CurrentCloseBlock closeBlock;
@property(nonatomic, copy) CurrentScanBlock scanBlock;

@property(nonatomic, assign) CGRect litleFrame;

- (id)initWithFrame:(CGRect)frame bgImage:(UIImage*)bgImage;

- (void)show;

@end
