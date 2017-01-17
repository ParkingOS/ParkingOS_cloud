//
//  TFullImageView.m
//  TingCheBao_user
//
//  Created by apple on 15/4/24.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TFullImageView.h"

@interface TFullImageView()<UIScrollViewDelegate>

@property(nonatomic, retain) UIWindow* imgWindow;
@property(nonatomic, retain) UIScrollView* imgScrollView;
@property(nonatomic, retain) UIImageView* fullImgView;

@end
@implementation TFullImageView

- (id)init {
    if (self = [super init]) {
        self.userInteractionEnabled = YES;
        self.contentMode = UIViewContentModeScaleAspectFit;
        [self addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapImgViewGesture:)]];
    }
    return self;
}

- (id)initWithImage:(UIImage *)image {
    if (self = [super initWithImage:image]) {
        self.userInteractionEnabled = YES;
        self.contentMode = UIViewContentModeScaleAspectFit;
        [self addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapImgViewGesture:)]];
    }
    return self;
}

- (void)handleTapImgViewGesture:(UITapGestureRecognizer*)gesture {
    if (gesture.view == self) {
        [self showImageView:self];
    } else if (gesture.view == _fullImgView) {
        [self hideImageView:self];
    }
}

//显示全屏图片
- (void)showImageView :(UIImageView*)orgImgView{
    _imgWindow = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    _imgWindow.backgroundColor = [UIColor blackColor];
    _imgWindow.alpha = 0;
    
    _imgScrollView = [[UIScrollView alloc] initWithFrame:_imgWindow.frame];
    _imgScrollView.contentSize = _imgScrollView.frame.size;
    _imgScrollView.backgroundColor = [UIColor clearColor];
    _imgScrollView.delegate = self;
    _imgScrollView.maximumZoomScale = 4.0;
    _imgScrollView.minimumZoomScale = 1.0;
    
    _fullImgView = [[UIImageView alloc] initWithImage:orgImgView.image];
    _fullImgView.backgroundColor = [UIColor blackColor];
    _fullImgView.contentMode = UIViewContentModeScaleAspectFit;
    _fullImgView.frame = [self.superview convertRect:orgImgView.frame toView:_imgWindow];
    [_fullImgView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapImgViewGesture:)]];
    _fullImgView.userInteractionEnabled = YES;
    
    [_imgScrollView addSubview:_fullImgView];
    [_imgWindow addSubview:_imgScrollView];
    [_imgWindow makeKeyAndVisible];
    [UIView animateWithDuration:0.3 animations:^{
        _fullImgView.frame = _imgScrollView.frame;
        _imgWindow.alpha = 1;
    }];
}

//隐藏全屏图片
- (void)hideImageView :(UIImageView*)orgImgView{
    [UIView animateWithDuration:0.3 animations:^{
        _fullImgView.frame = [self.superview convertRect:orgImgView.frame toView:_imgWindow];
        _imgWindow.alpha = 0;
    } completion:^(BOOL finished) {
        [_fullImgView removeFromSuperview];
        [_imgScrollView removeFromSuperview];
        _fullImgView = nil;
        _imgScrollView = nil;
        
        //清除 window
        NSMutableArray* windows = [NSMutableArray arrayWithArray:[UIApplication sharedApplication].windows];
        [windows removeObject:_imgWindow];
        //遍历window,选出正常的window
        [windows enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(UIWindow* obj, NSUInteger idx, BOOL *stop) {
            if ([obj isKindOfClass:[UIWindow class]] && obj.windowLevel == UIWindowLevelNormal) {
                [obj makeKeyAndVisible];
                *stop = YES;
            }
        }];
    }];
}

#pragma mark UIScrollViewDelegate

- (UIView*)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return _fullImgView;
}

@end
