//
//  CenterViewController.m
//  Dog
//
//  Created by apple on 14-7-24.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "CenterViewController.h"
#import "UIView+CVUIViewAdditions.h"

@interface CenterViewController ()

@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UIScrollView* showView;
@property(nonatomic, retain) UIPageControl* pageControl;
@property(nonatomic, retain) UIView* chooseView;
@property(nonatomic, retain) UIButton* button;
@property(nonatomic, retain) UIImageView* imageView;

@end

@implementation CenterViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = [UIColor yellowColor];
    
    _imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"fish.jpg"]];
    _imageView.frame = self.view.frame;
    [self.view addSubview:_imageView];
    
    _button = [UIButton buttonWithType:UIButtonTypeCustom];
    [_button setTitle:@"click me" forState:UIControlStateNormal];
    [_button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [_button setBackgroundImage:[UIImage imageNamed:@"it_common_blue_big.png"] forState:UIControlStateNormal];
    [_button setBackgroundImage:[UIImage imageNamed:@"it_common_green_big_pressed.png"] forState:UIControlStateHighlighted];
    [_button setBackgroundImage:[UIImage imageNamed:@"it_common_green_big_pressed.png"] forState:UIControlStateSelected];
    _button.frame = CGRectMake(100, 100, 100, 40);
    
    
    [self.view addSubview:_button];
    
    UIImage *btnImage = [[UIImage imageNamed:@"it_choose_chatbar.png"] stretchableImageWithLeftCapWidth:1 topCapHeight:1];
    UIImage *btnHighlightedImage = [[UIImage imageNamed:@"it_choose_chatbar_pressed.png"] stretchableImageWithLeftCapWidth:4 topCapHeight:16];
    [_button setBackgroundImage:btnImage forState:UIControlStateNormal];
    [_button setBackgroundImage:btnHighlightedImage forState:UIControlStateHighlighted];
    _button.frame = CGRectMake(100, 100, 130, 40);
    [self.view addSubview:_button];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)handle :(UIPanGestureRecognizer*)gesture {
    CGPoint translatePoint = [gesture translationInView:gesture.view];
    gesture.view.center = [gesture locationInView:self.view];
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
