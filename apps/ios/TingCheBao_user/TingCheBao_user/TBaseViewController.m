//
//  TBaseViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-8-19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TBaseViewController.h"
#import "TViewController.h"
#import "TAPIUtility.h"
#import "MobClick.h"
#import "THomeViewController.h"
#import "TLocationInfoViewController.h"

@interface TBaseViewController ()<CVAPIModelDelegate, UIGestureRecognizerDelegate>


@end

@implementation TBaseViewController

- (id)init {
    if (self = [super init]) {
        if ([[UIDevice currentDevice].systemVersion floatValue] >= 7.0) {
            self.edgesForExtendedLayout = UIRectEdgeNone;
            self.view.height -= 64;
            _model = [[CVAPIRequestModel alloc] init];
            _model.delegate = self;
        }
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = [UIColor whiteColor];
   
    
    //TODO:居中有问题，我写死了
//    CGRect leftViewbounds = self.navigationItem.leftBarButtonItem.customView.bounds;
    //为了让titleView 居中      leftview 左右都有间隙，左边是5像素，右边是8像素，加2个像素的阀值 5 ＋ 8 ＋ 2 = 15
    _titleView = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 210, 40)];
//    _titleView = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320 - (leftViewbounds.size.width + 13) * 2, 40)];
    _titleView.textColor = [UIColor grayColor];
    _titleView.textAlignment = NSTextAlignmentCenter;
    _titleView.font = [UIFont boldSystemFontOfSize:18];
    self.navigationItem.titleView = _titleView;
   
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    self.navigationController.interactivePopGestureRecognizer.delegate = (id<UIGestureRecognizerDelegate>)self;
    
    self.navigationItem.leftBarButtonItem = self.leftItem;
    [MobClick beginLogPageView:[NSString stringWithFormat:@"%@", NSStringFromClass([self class])]];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [MobClick endLogPageView:[NSString stringWithFormat:@"%@", NSStringFromClass([self class])]];
    //在viewDidDisappeare 会出问题，它会延迟，新的页面出来了，才调用
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}

- (UIBarButtonItem*)leftItem {
    UIButton* leftButton = [UIButton buttonWithType:UIButtonTypeCustom];
    leftButton.frame = CGRectMake(0, 0, 30, 30);
    if ([self.navigationController.viewControllers count] > 1) {
        
        if ([self isKindOfClass:[TLocationInfoViewController class]]) {
            //停车位置页面 整体颜色是黑色 所以要变成白色
            [leftButton setImage:[UIImage imageNamed:@"left_arrow_white.png"] forState:UIControlStateNormal];
        } else {
            
            [leftButton setImage:[UIImage imageNamed:@"left_arrow_gray.png"] forState:UIControlStateNormal];
        }
        
        [leftButton setImageEdgeInsets:UIEdgeInsetsMake(5, 0, 5, 10)];
        leftButton.tag = 0;
    } else {
        if ([self isKindOfClass:[THomeViewController class]]) {
            [leftButton setBackgroundImage:[UIImage imageNamed:@"contact.png"] forState:UIControlStateNormal];
            leftButton.tag = 1;
        } else {
            leftButton.frame = CGRectMake(0, 0, 40, 30);
            [leftButton setTitle:@"取消" forState:UIControlStateNormal];
            [leftButton setTitleColor:green_color forState:UIControlStateNormal];
            leftButton.tag = 2;
        }
    }
    [leftButton addTarget:self action:@selector(clickedLeftItem:) forControlEvents:UIControlEventTouchUpInside];
    
    UIBarButtonItem* item = [[UIBarButtonItem alloc] initWithCustomView:leftButton];
    return item;
}

- (void)clickedLeftItem :(UIButton*)button {
    if (button.tag == 0) {
        [self.navigationController popViewControllerAnimated:YES];
    } else if (button.tag == 1){
        [[TViewController share] showOrHideLeftMenu];
    } else if (button.tag == 2) {
        [self.navigationController dismissViewControllerAnimated:YES completion:nil];
    }
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
    if (gestureRecognizer == self.navigationController.interactivePopGestureRecognizer) {
        if (self.navigationController.viewControllers.count == 1) {
            return NO;
        } else {
            return YES;
        }
    }
    return YES;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
