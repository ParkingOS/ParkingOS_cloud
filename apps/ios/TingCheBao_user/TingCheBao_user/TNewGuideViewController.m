//
//  TGuideViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/10/22.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TNewGuideViewController.h"
#import "TAPIUtility.h"
#import "TViewController.h"
#import "THomeViewController.h"
#import "TNavigationController.h"
#import "TTicketGameViewController.h"

@interface TNewGuideViewController ()<UIScrollViewDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UIButton* entryButton;
@property(nonatomic, retain) UIButton* playButton;
@property(nonatomic, retain) UIPageControl* pageControl;


@end

@implementation TNewGuideViewController

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
            imgView.image =  [UIImage imageNamed:[NSString stringWithFormat:@"new_guide%d.png", i+1]];
            imgView.frame = CGRectMake(self.view.width * i, 0, self.view.width, self.view.height);
            [_scrollView addSubview:imgView];
        }
        
        
        _entryButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_entryButton setTitle:@"进入应用" forState:UIControlStateNormal];
        [_entryButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _entryButton.titleLabel.font = [UIFont systemFontOfSize:20];
        [_entryButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_entryButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _entryButton.frame = CGRectMake(self.view.width * 3 + (self.view.width - 160)/2, self.view.height - (isIphone4s ? 170 :230), 160, 45);//iphone4s 140
        _entryButton.layer.cornerRadius = 5;
        _entryButton.clipsToBounds = YES;
        
        _playButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_playButton setTitle:@"先玩游戏" forState:UIControlStateNormal];
        [_playButton setTitleColor:green_color forState:UIControlStateNormal];
        _playButton.titleLabel.font = [UIFont systemFontOfSize:20];
        [_playButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        _playButton.layer.borderColor = green_color.CGColor;
        _playButton.layer.borderWidth = 1;
        _playButton.layer.cornerRadius = 5;
        _playButton.clipsToBounds = YES;
        [_playButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _playButton.frame = CGRectMake(self.view.width * 3 + (self.view.width - 160)/2, _entryButton.bottom + 20, 160, 45);
        _playButton.hidden = [[NSUserDefaults standardUserDefaults] objectForKey:save_phone] ? NO : YES;
        
        _pageControl = [[UIPageControl alloc] initWithFrame:CGRectMake((self.view.width - 200)/2, self.view.height - 50, 200, 20)];
        _pageControl.numberOfPages = 4;
        _pageControl.pageIndicatorTintColor = [UIColor whiteColor];
        _pageControl.currentPageIndicatorTintColor = green_color;
        
        [_scrollView addSubview:_entryButton];
        [_scrollView addSubview:_playButton];
        
        [self.view addSubview:_scrollView];
        [self.view addSubview:_pageControl];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

////// 滚动停止时，触发该函数
//- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
//}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    int num = (int)(_scrollView.contentOffset.x / self.view.width);
    _pageControl.currentPage = num;
}

- (void)buttonTouched:(UIButton*)button {
    THomeViewController* home = [THomeViewController share];
    TNavigationController* nv = [[TNavigationController alloc] initWithRootViewController:home];
    
    if ([button.titleLabel.text isEqualToString:@"先玩游戏"]) {
        
        NSString* url = [TAPIUtility getNetworkWithUrl:[NSString stringWithFormat:@"cargame.do?action=playgame&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]]];
        TTicketGameViewController* palyVc = [[TTicketGameViewController alloc] initWithName:@"游戏" url:url];
        
        //连续推进面页
        nv.shouldIgnorePushingViewControllers = NO;
        
        [nv pushViewController:palyVc animated:NO];
    }
    
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
