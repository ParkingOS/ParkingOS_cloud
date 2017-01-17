//
//  TGuideViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/10/22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TGuideViewController.h"
#import "TAPIUtility.h"
#import "TViewController.h"
#import "THomeViewController.h"
#import "TNavigationController.h"

@interface TGuideViewController ()<UIScrollViewDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UIButton* entryButton;
@property(nonatomic, retain) UIPageControl* pageControl;
@property(nonatomic, retain) UIImageView* carImgView;


@end

@implementation TGuideViewController

- (id)init {
    if (self = [super init]) {
        CGRect frame = [UIScreen mainScreen].bounds;
        _scrollView = [[UIScrollView alloc] initWithFrame:frame];
        _scrollView.pagingEnabled = YES;
        _scrollView.contentSize = CGSizeMake(frame.size.width * 4, frame.size.height);
        _scrollView.showsHorizontalScrollIndicator = NO;
        _scrollView.bounces = NO;
        _scrollView.delegate = self;
        for (int i = 0; i < 4; i++) {
            UIImageView* imgView = [[UIImageView alloc] init];
            if (self.view.height == 480) {
                imgView.image =  [UIImage imageNamed:[NSString stringWithFormat:@"guide%d-480.png", i+1]];
            } else {
                imgView.image =  [UIImage imageNamed:[NSString stringWithFormat:@"guide%d-568.png", i+1]];
            }
            imgView.frame = CGRectMake(self.view.width * i, 0, self.view.width, self.view.height);
            [_scrollView addSubview:imgView];
        }
        
        
        _entryButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_entryButton setTitle:@"进入应用" forState:UIControlStateNormal];
        [_entryButton setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        [_entryButton setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(241, 193, 30)] forState:UIControlStateNormal];
        [_entryButton addTarget:self action:@selector(entryButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _carImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"car_entry.png"]];
        _carImgView.frame = CGRectMake(240, self.view.height - 70, 80, 40);
        if (self.view.height == 480) {
            //iphone 4s
            _carImgView.frame = CGRectMake((self.view.width - 80)/2, 391, 80, 40);
        } else if (self.view.height == 568) {
            //iphone 5s
            _carImgView.frame = CGRectMake(120, 451, 80, 40);
        } else if (self.view.height == 667) {
            //iphone 6
            _carImgView.frame = CGRectMake(152, 533, 80, 40);
        } else if (self.view.height == 736) {
            //iphone 6 plus
            _carImgView.frame = CGRectMake(172, 591, 80, 40);
        }
        
        _entryButton.frame = CGRectMake(self.view.width * 3 + (self.view.width - 120)/2, _carImgView.top - 90, 120, 50);
        
        _pageControl = [[UIPageControl alloc] initWithFrame:CGRectMake((self.view.width - 100)/2, self.view.height - 30, 100, 20)];
        _pageControl.numberOfPages = 4;
        
        [_scrollView addSubview:_entryButton];
        
        [self.view addSubview:_scrollView];
        [self.view addSubview:_carImgView];
        [self.view addSubview:_pageControl];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

//// 滚动停止时，触发该函数
- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    int num = (int)(_scrollView.contentOffset.x / self.view.width);
    _pageControl.currentPage = num;
}

- (void)entryButtonTouched:(UIButton*)button {
    THomeViewController* home = [THomeViewController share];
    TNavigationController* nv = [[TNavigationController alloc] initWithRootViewController:home];
    //    THistoryOrderViewController* home = [[THistoryOrderViewController alloc] init];
    //    UINavigationController* nv = [[UINavigationController alloc] initWithRootViewController:home];
    
    TViewController* vc = [[TViewController alloc] init];
    [TViewController setInstance:vc];
    vc.centerController = nv;
    
    [[UINavigationBar appearance] setBarTintColor:RGBCOLOR(254, 254, 254)];
    [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDefault;
    [vc setNeedsStatusBarAppearanceUpdate];
    
    UIWindow* window = [UIApplication sharedApplication].keyWindow;
    window.rootViewController = vc;
    [window makeKeyAndVisible];
}
@end
