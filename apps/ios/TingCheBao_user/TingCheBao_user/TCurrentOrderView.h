//
//  TCurrentOrderView.h
//  TingCheBao_user
//
//  Created by zhuhao on 15/4/8.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TPopView.h"
#import "TCurrentOrderItem.h"

typedef enum {
    currentOrderMode_loading,
    currentOrderMode_noInfo,
    currentOrderMode_noComplete,
    currentOrderMode_countTime,
    currentOrderMode_note,
    currentOrderMode_pay
} currentOrderMode;

typedef void(^CurrentScanBlock)();
typedef void(^CurrentCloseBlock)();
typedef void(^CurrentPhotoBlock)();
typedef void(^CurrentPayBlock)(TCurrentOrderItem*item);

@interface TCurrentOrderView : TPopView

@property(nonatomic, retain) TCurrentOrderItem* item;

@property(nonatomic, retain) UIActivityIndicatorView* loaddingView;
@property(nonatomic, copy) CurrentCloseBlock closeBlock;
@property(nonatomic, copy) CurrentScanBlock scanBlock;
@property(nonatomic, copy) CurrentPhotoBlock photoBlock;
@property(nonatomic, copy) CurrentPayBlock payBlock;

- (void)updateView:(currentOrderMode)mode;
- (void)captureImage:(UIImage*)image;

@end
