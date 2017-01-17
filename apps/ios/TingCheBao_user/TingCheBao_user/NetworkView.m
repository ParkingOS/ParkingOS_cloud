//
//  NetworkView.m
//  Test
//
//  Created by apple on 14-9-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "NetworkView.h"
#import "UIView+CVUIViewAdditions.h"

#define statusHeight 20

@interface NetworkView()

@property(nonatomic, retain) UIWindow* netwindow;
@property(nonatomic, retain) UIView* netView;
@property(nonatomic, retain) UIImageView* imgView;
@property(nonatomic, retain) UILabel* statusLabel;

@end
@implementation NetworkView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        self.backgroundColor = [UIColor clearColor];
        
    }
    return self;
}

+ (NetworkView*)sharedView {
    static dispatch_once_t once;
    static NetworkView *sharedView;
    dispatch_once(&once, ^ { sharedView = [[NetworkView alloc] initWithFrame:[[UIScreen mainScreen] bounds]]; });
    return sharedView;
}

+(void)showWithStatus:(NSString*)status {
    [[NetworkView sharedView] show:status];
}

- (void)show:(NSString*)status {
    //防止频繁刷新,如果已经显示,则不再刷新
    if (self.netView.top != -statusHeight) {
        return;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (!self.netView.superview) {
            [self addSubview:self.netView];
        }
        if (!self.imgView.superview) {
            [self.netView addSubview:self.imgView];
        }
        self.statusLabel.text = status;
        
        if (!self.statusLabel.superview) {
            [self.netView addSubview:self.statusLabel];
        }
        if (!self.superview) {
            [self.netwindow addSubview:self];
        }
        [self.netwindow makeKeyAndVisible];
        [UIView animateWithDuration:0.2 animations:^{
            self.netView.top = 0;
        } completion:^(BOOL finished) {
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 1.5),
                           dispatch_get_main_queue(), ^{
                               [UIView animateWithDuration:0.2 animations:^{
                                   self.netView.top = -statusHeight;
                               }];
                           });
        }];
    });
}

- (UIWindow*)netwindow {
    if (!_netwindow) {
        _netwindow = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
        _netwindow.backgroundColor = [UIColor clearColor];
        _netwindow.userInteractionEnabled = NO;
        _netwindow.windowLevel = UIWindowLevelAlert;
    }
    return _netwindow;
}

- (UIView*)netView {
    if (!_netView) {
        _netView = [[UIView alloc] initWithFrame:CGRectMake(0, -20, self.width, statusHeight)];
        _netView.backgroundColor = [UIColor blackColor];
        
    }
    return _netView;
}

- (UIImageView*)imgView {
    if (!_imgView) {
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"yellow_alert.png"]];
        _imgView.frame = CGRectMake(10, 1, 18, 18);
    }
    return _imgView;
}

- (UILabel*)statusLabel {
    if (!_statusLabel) {
        _statusLabel = [[UILabel alloc] initWithFrame:CGRectMake(40, 0, 200, statusHeight)];
        _statusLabel.backgroundColor = [UIColor clearColor];
        _statusLabel.textAlignment = NSTextAlignmentLeft;
        _statusLabel.font = [UIFont boldSystemFontOfSize:15];
        _statusLabel.textColor = [UIColor whiteColor];
    }
    return _statusLabel;
}

@end
