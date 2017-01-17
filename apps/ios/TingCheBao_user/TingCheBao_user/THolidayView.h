//
//  THolidayView.h
//  TingCheBao_user
//
//  Created by apple on 14/12/25.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol THolidayViewDelegate <NSObject>

- (void)holidayCloseTouched;
- (void)holidayShareTouched;

@end
@interface THolidayView : UIView

@property(nonatomic, unsafe_unretained) id<THolidayViewDelegate>delegate;
@property(nonatomic, retain) UIButton* holidayButton;
@property(nonatomic, retain) NSString* url;

@end
