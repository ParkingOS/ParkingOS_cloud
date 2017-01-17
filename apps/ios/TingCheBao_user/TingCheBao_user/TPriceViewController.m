//
//  TPriceViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TPriceViewController.h"
#import "TPriceView.h"
#import "TPriceItem.h"
#import "CVAPIRequestModel.h"

#define bg_color RGBCOLOR(230, 230, 230)

@interface TPriceViewController ()

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) TPriceView* dayView;
@property(nonatomic, retain) TPriceView* nightView;
@property(nonatomic, retain) CVAPIRequest* request;

@end

@implementation TPriceViewController

- (id)init {
    if (self = [super init]) {
        _scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _scrollView.contentSize = CGSizeMake(self.view.width, 460);
        
        _dayView = [[TPriceView alloc] initWithFrame:CGRectMake(10, 20, self.view.width - 2*10, 210)];
        [_dayView setItem:nil isDay:YES];
        
        _nightView = [[TPriceView alloc] initWithFrame:CGRectMake(10, _dayView.bottom + 20, self.view.width - 2*10, 210)];
        [_nightView setItem:nil isDay:NO];
        
        
        [self.scrollView addSubview:_dayView];
        [self.scrollView addSubview:_nightView];
        
        [self.view addSubview:_scrollView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = bg_color;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"价格详情";
    [self requestPriceInfo];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

- (void)requestPriceInfo {
    NSString* apiPath = [NSString stringWithFormat:@"parkedit.do?action=queryprice&comid=%@", _parkId];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        _item = [TPriceItem getItemFromDictionary:result];
        [_dayView setItem:_item isDay:YES];
        [_nightView setItem:_item isDay:NO];
        if ([_item.isnight isEqualToString:@"1"])
            _nightView.hidden = YES;
        else
            _nightView.hidden = NO;
    }];
}

@end
