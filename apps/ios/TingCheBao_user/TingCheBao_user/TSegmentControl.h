//
//  TSegmentControl.h
//  TingCheBao_user
//
//  Created by apple on 15/5/11.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIView+CVUIViewAdditions.h"

@interface TSegmentControl : UIView

@property(nonatomic, retain) NSArray* items;

@property(nonatomic, readonly) NSInteger selectedIndex;

- (id)initWithItems:(NSArray*)items;
- (void)addTarget:(id)target action:(SEL)action;

@end
