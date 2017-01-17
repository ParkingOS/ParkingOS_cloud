//
//  TPopView.m
//  TingCheBao_user
//
//  Created by apple on 15/1/23.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TPopView.h"

@interface TPopView()


@end
@implementation TPopView

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _bgView = [[UIView alloc] initWithFrame:frame];
        _bgView.backgroundColor = [UIColor blackColor];
        _bgView.alpha = 0.7;
        UITapGestureRecognizer* tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
        [_bgView addGestureRecognizer:tap];
        
        _centerView = [[UIView alloc] initWithFrame:CGRectMake(30, isIphone4s ? 20 : 60, self.width - 30*2, 210)];
        _centerView.backgroundColor = [UIColor whiteColor];
        _centerView.clipsToBounds = YES;
        _centerView.layer.cornerRadius = 5;
        
        _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _closeButton.frame = CGRectMake(_centerView.right - 20, _centerView.top - 20, 40, 40);
        [_closeButton setImage:[UIImage imageNamed:@"close_gray.png"] forState:UIControlStateNormal];
        [_closeButton addTarget:self action:@selector(closeButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self addSubview:_bgView];
        [self addSubview:_centerView];
        [self addSubview:_closeButton];
    }
    return self;
}

- (void)handleTap:(UIGestureRecognizer*)gesture {
    [self show:NO];
}

- (void)show:(BOOL)show {
    if (show) {
        _myWindow = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
        _myWindow.backgroundColor = [UIColor clearColor];
        
        [_myWindow addSubview:self];
        [_myWindow makeKeyAndVisible];
    } else {
        //清除 window
        NSMutableArray* windows = [NSMutableArray arrayWithArray:[UIApplication sharedApplication].windows];
        [windows removeObject:_myWindow];
        //遍历window,选出正常的window
        [windows enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(UIWindow* obj, NSUInteger idx, BOOL *stop) {
            if ([obj isKindOfClass:[UIWindow class]] && obj.windowLevel == UIWindowLevelNormal) {
                [obj makeKeyAndVisible];
                *stop = YES;
            }
        }];
        _myWindow = nil;
    }
}

- (void)closeButtonTouched:(UIButton*)button {
    self.hidden = YES;
}

@end
