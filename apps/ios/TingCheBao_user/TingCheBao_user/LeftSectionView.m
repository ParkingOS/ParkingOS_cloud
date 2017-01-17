//
//  LeftSectionView.m
//  Dog
//
//  Created by apple on 14-7-24.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "LeftSectionView.h"

@interface LeftSectionView()

@property(nonatomic, retain) UILabel* label;
@property(nonatomic, retain) UIImageView* addView;
@property(nonatomic, retain) UIImageView* settingView;
@property(nonatomic, retain) UIImageView* trashView;

@end

@implementation LeftSectionView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        _label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 120, 40)];
        _label.text = @"";
        _label.textColor = [UIColor whiteColor];
        _label.font = [UIFont systemFontOfSize:12];
        
        _addView = [[UIImageView alloc] initWithFrame:CGRectMake(140, 10, 20, 20)];
        _addView.backgroundColor = [UIColor yellowColor];
        
        _settingView = [[UIImageView alloc] initWithFrame:CGRectMake(170, 10, 20, 20)];
        _settingView.backgroundColor = [UIColor yellowColor];
        
        _trashView = [[UIImageView alloc] initWithFrame:CGRectMake(170, 10, 20, 20)];
        _trashView.backgroundColor = [UIColor yellowColor];
        
        self.backgroundColor = [UIColor blackColor];
        [self addSubview:_label];
        [self addSubview:_addView];
        [self addSubview:_settingView];
        [self addSubview:_trashView];
    }
    return self;
}

- (void)setText:(NSString*)text type:(LeftSectionType)type {
    _label.text = text;
    if (type == SETTING_TYPE) {
        _addView.hidden = _settingView.hidden = NO;
        _trashView.hidden = YES;
    } else {
        _addView.hidden = _settingView.hidden = YES;
        _trashView.hidden = NO;
    }
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
