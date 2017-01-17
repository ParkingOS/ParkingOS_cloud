//
//  TViewController.m
//  Dog
//
//  Created by apple on 14-7-24.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TViewController.h"
#import "UIView+CVUIViewAdditions.h"
#import "THomeViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TAccountItem.h"
#import "TSession.h"


static TViewController* _instance = nil;

@interface TViewController ()<UIGestureRecognizerDelegate>

@property(nonatomic, retain) UIView* centerView;
@property(nonatomic, retain) UIView* overView;
@property(nonatomic, retain) UITapGestureRecognizer* tapGesture;
@property(nonatomic, retain) UIPanGestureRecognizer* leftViewPanGesture;

@property(nonatomic, assign) CGPoint startPoint;

@end

@implementation TViewController

+ (TViewController*)share {
    return _instance;
}

+ (void)setInstance:(TViewController*)instance {
    _instance = instance;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    _leftController= [[TLeftViewController alloc] init];
    _leftView = _leftController.view;
    _leftView.alpha = 0;
    
    _rightController= [[TRightViewController alloc] init];
    _rightView = _rightController.view;
    _rightView.alpha = 0;
    
    _overView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.height)];
    _overView.backgroundColor = [UIColor blackColor];
    _overView.alpha = 0;
    
    [self.view addSubview:_leftController.view];
    [self.view addSubview:_rightController.view];
    [self.view addSubview:_overView];
}

- (void)setCenterController:(UINavigationController *)centerController {
    if (centerController != _centerController) {
        if (_centerController) {
            //移除原来的
            [_centerView removeFromSuperview];
            [_centerController removeFromParentViewController];
        }
        //添加新的
        _centerController = centerController;
        [self addChildViewController:_centerController];
        [_centerController didMoveToParentViewController:self];
        _centerView = _centerController.view;
        _centerController.view.clipsToBounds = NO;
        
//        [self addShadow];
        
        [self.view addSubview:_centerController.view];
        
        // 点击手势 点击灰色部分 关闭左侧
        _tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        _tapGesture.delegate = self;
        
        //滑动手势 作用于leftView
        _leftViewPanGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handleMove:)];
        _leftViewPanGesture.delegate = self;
        
        //滑动手势 下面这几行代码好像没用
        UIPanGestureRecognizer* panGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handleMove:)];
        
        if ([_centerController isKindOfClass:[UINavigationController class]]) {
            UIViewController* topVC = (UINavigationController*)_centerController.topViewController;
            if (![topVC isKindOfClass:[THomeViewController class]]) {
                [topVC.view addGestureRecognizer:panGesture];
            }
        }
    }
}

#pragma mark UIGestureRecognizerDelegate

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch {
    CGPoint point = [touch locationInView:self.view];
    if (gestureRecognizer == _tapGesture) {
        if (CGRectContainsPoint(_centerView.frame, point)) {
            return YES;
        } else {
            return NO;
        }
    } else if (gestureRecognizer == _leftViewPanGesture) {
        if (CGRectContainsPoint(CGRectMake(0, 0, self.view.width, self.view.height), point)) {
            return YES;
        } else {
            return NO;
        }
    }
    return NO;
}

#pragma  mark private

//- (void) addShadow {
//    CALayer*layer = _leftController.view.layer;
//    layer.shadowColor = [UIColor whiteColor].CGColor;
//    layer.shadowOpacity = 0.75f;
//    layer.shadowRadius = 10.f;
//    layer.shadowPath = [UIBezierPath bezierPathWithRect:layer.bounds].CGPath;
//}

- (void)handleMove:(UIPanGestureRecognizer*)gesture {
    CGPoint translationPoint = [gesture translationInView:self.view];
    CGPoint point = [gesture locationInView:self.view];
//    NSLog(@"%@", NSStringFromCGPoint(point));
    if (gesture.state == UIGestureRecognizerStateBegan) {
        _startPoint = _centerView.frame.origin;
    } else if (gesture.state == UIGestureRecognizerStateChanged) {
        _centerView.left = _startPoint.x + translationPoint.x;
        if (_startPoint.x != -1 *left_menu_max_width) {
            if (_centerView.left >= left_menu_max_width)
                _centerView.left = left_menu_max_width;
            if (_centerView.left < 0) {
                _centerView.left = 0;
                _leftView.alpha = 0;
            } else {
                _leftView.alpha = 1;
            }
            
            //覆盖层跟随
            _overView.left = _centerView.left;
            
        } else {
            if (_centerView.right <= self.view.width - left_menu_max_width)
                _centerView.right = left_menu_max_width;
            if (_centerView.right > self.view.width) {
                _centerView.right = self.view.width;
                _rightView.alpha = 0;
            } else {
                _rightView.alpha = 1;
            }
        }
        
    } else if (gesture.state == UIGestureRecognizerStateEnded) {
        if (_startPoint.x != -1 *left_menu_max_width) {
            BOOL showLeft = NO;
            if (translationPoint.x < 0) {
                if (translationPoint.x <= -60) {
                    showLeft = NO;
                } else {
                    showLeft = YES;
                }
            } else if (translationPoint.x > 0) {
                if (translationPoint.x >= 60) {
                    showLeft = YES;
                } else {
                    showLeft = NO;
                }
            }
            
            [UIView animateWithDuration:0.2 animations:^{
                [self updateState:@"left" show:showLeft];
                
                if (showLeft)
                    [self requestCarNumber];
            }];
            
        } else {
            BOOL showRight = NO;
            if (translationPoint.x > 0) {
                if (translationPoint.x >= 60) {
                    showRight = NO;
                } else {
                    showRight = YES;
                }
            }
            
            [self updateState:@"right" show:showRight];
        }
    }
}

- (void)handleTapGesture:(UIGestureRecognizer*)gesture {
    if (_leftView.alpha == 1)
        [self showOrHideLeftMenu];
    else if (_rightView.alpha == 1)
        [self showOrHideRightMenu];
}

#pragma mark public

- (void)showOrHideLeftMenu {
    if (_leftView.alpha == 0) {
        [UIView animateWithDuration:0.2 animations:^{
            [self updateState:@"left" show:YES];
            
            [self requestCarNumber];
        }];
    } else {
        [UIView animateWithDuration:0.2 animations:^{
            [self updateState:@"left" show:NO];
        }];
    }
}

- (void)showOrHideRightMenu {
    if (_rightView.alpha == 0) {
        [UIView animateWithDuration:0.2 animations:^{
            [self updateState:@"right" show:YES];
        }];
    } else {
        [UIView animateWithDuration:0.2 animations:^{
            [self updateState:@"right" show:NO];
        }];
    }
}
         
- (void)updateState:(NSString*)viewName show:(BOOL)show{
    if (show) {
        if ([viewName isEqualToString:@"left"]) {
            _centerView.left = left_menu_max_width;
            _leftView.alpha = 1;
            _rightView.alpha = 0;
        } else if ([viewName isEqualToString:@"right"]) {
            _centerView.left = left_menu_max_width * -1;
            _rightView.alpha = 1;
            _leftView.alpha = 0;
        }
        _centerView.userInteractionEnabled = NO;
        [self.view addGestureRecognizer:_tapGesture];
        [self.view addGestureRecognizer:_leftViewPanGesture];
    } else {
        _centerView.left = 0;
        if ([viewName isEqualToString:@"left"]) {
            _leftView.alpha = 0;
        } else if ([viewName isEqualToString:@"right"]) {
            _rightView.alpha = 0;
        }
        _centerView.userInteractionEnabled = YES;
        [self.view removeGestureRecognizer:_tapGesture];
        [self.view removeGestureRecognizer:_leftViewPanGesture];
    }
    
    //添加覆盖层
    _overView.left = _centerView.left;
    _overView.alpha = show ? 0.7 : 0;
    [self.view bringSubviewToFront:_overView];
}

- (void)requestCarNumber {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        //不显示车牌号等信息
        [_leftController updateLoginState:NO];
        return;
    }
    //显示车牌号等信息
    [_leftController updateLoginState:YES];
    
//#warning test
//    [_leftController startRequestCarNumber:NO isAuth:@"1"];
//    return;
    
    //每次都网络请求
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=getcarnumbs&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    CVAPIRequest* request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [_leftController startRequestCarNumber:YES isAuth:nil];
    [model sendRequest:request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        //[{"car_number":"京QLL578","is_auth":"1"},{"car_number":"京QLL577","is_auth":"1"}]
        //is_auht:0未认证，1已认证 2认证中 -1审核不通过 -2异常
        NSMutableArray* carNumbers = [NSMutableArray array];
        NSString* is_auth = nil;
        
        for (NSDictionary* dic in result) {
            [carNumbers addObject:[dic objectForKey:@"car_number"]];
            if ([[dic objectForKey:@"is_default"] isEqualToString:@"1"]) {
                is_auth = [dic objectForKey:@"is_auth"];
                break;
            }
        }
        //保存本地车牌号
        [[TSession shared] setCarNumbers:carNumbers];
        
        [_leftController startRequestCarNumber:NO isAuth:is_auth];
    }];
}

@end
