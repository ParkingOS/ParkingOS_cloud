//
//  TPriceView.h
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TPriceItem.h"

@interface TPriceView : UIView

@property(nonatomic, retain) TPriceItem* item;

- (void)setItem:(TPriceItem *)item isDay:(BOOL)isDay;

@end
