//
//  TOfflineMapViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/12/4.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TOfflineMapViewController.h"
#import "TAPIUtility.h"
#import <BaiduMapAPI/BMKOfflineMap.h>

@interface TOfflineMapViewController ()<BMKOfflineMapDelegate>

@property(nonatomic, retain) UILabel* cityLabel;
@property(nonatomic, retain) UILabel* progressLabel;
@property(nonatomic, retain) UIButton* actionButton;
@property(nonatomic, retain) UIProgressView* progressView;

@property(nonatomic, retain) BMKOfflineMap* offlineMap;
@property(nonatomic, retain) BMKOLSearchRecord* record;
@property(nonatomic, retain) BMKOLUpdateElement* element;

@end

@implementation TOfflineMapViewController

- (id)init {
    if (self = [super init]) {
        _cityLabel = [[UILabel alloc] init];
        _cityLabel.text = @"北京市";
        _cityLabel.textColor = [UIColor blackColor];
        _cityLabel.font = [UIFont systemFontOfSize:14];
        
        _actionButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_actionButton setTitle:@"下载" forState:UIControlStateNormal];
        [_actionButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _actionButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_actionButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        [_actionButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _progressView = [UIProgressView new];
        
        _progressLabel = [[UILabel alloc] init];
        _progressLabel.text = @"已下载 %0";
        _progressLabel.font = [UIFont systemFontOfSize:14];
        _progressLabel.textAlignment = NSTextAlignmentRight;
        
        
        [self.view addSubview:_cityLabel];
        [self.view addSubview:_progressLabel];
        [self.view addSubview:_actionButton];
        [self.view addSubview:_progressView];
        
        [self initInfo];
    }
    return self;
}

- (void)initInfo {
    _offlineMap = [[BMKOfflineMap alloc] init];
    _offlineMap.delegate = self;
    _record = [[_offlineMap searchCity:@"北京市"] firstObject];
    _cityLabel.text = [NSString stringWithFormat:@"北京市(%.1fM)", ((CGFloat)_record.size)/(1024*1024)];
    BMKOLUpdateElement* element = [_offlineMap getUpdateInfo:_record.cityID];
    _progressView.progress = (CGFloat)element.ratio/100;
    if (element.ratio == 100) {
//        _actionButton.hidden = YES;
        [_actionButton setTitle:@"移除" forState:UIControlStateNormal];
        _progressView.progress = 1;
        _progressLabel.text = @"已下载";
        _progressLabel.textColor = green_color;
        
    } else if (element.ratio != 0) {
        [_actionButton setTitle:@"继续下载" forState:UIControlStateNormal];
        _progressLabel.text = [NSString stringWithFormat:@"已下载 %d%%", element.ratio];
    } else if (element.ratio == 0) {
        [_actionButton setTitle:@"下载" forState:UIControlStateNormal];
        _progressLabel.text = @"";
    }
}

- (void)viewWillLayoutSubviews {
    _cityLabel.frame = CGRectMake(10, 20, 100, 35);
    _actionButton.frame = CGRectMake(self.view.width - 10 - 80, _cityLabel.top, 80, _cityLabel.height);
    _progressView.frame = CGRectMake(10, _cityLabel.bottom + 20, self.view.width - 10*2, 30);
    _progressLabel.frame = CGRectMake(self.view.width - 10 - 120, _progressView.bottom + 10, 120, _cityLabel.height);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"离线地图";
}

- (void)viewDidLoad {
    [super viewDidLoad];
}

#pragma mark BMKOfflineMapDelegate

- (void)onGetOfflineMapState:(int)type withState:(int)state {
    //下载时，type = 0, state = 131北京
//    NSLog(@"%d-%d", type, state);
    _element = [_offlineMap getUpdateInfo:state];
//    NSLog(@"%d--ratio,-state--%d", _element.ratio, _element.status);
    if (_element.status == 1) {
        //正在下载
        _progressView.progress = (CGFloat)_element.ratio/100;
        _progressLabel.text = [NSString stringWithFormat:@"正在下载 %d%%", _element.ratio];
        _progressLabel.textColor = [UIColor blueColor];
    } else if (_element.status == 3) {
        //暂停
        _progressView.progress = (CGFloat)_element.ratio/100;
        _progressLabel.text = [NSString stringWithFormat:@"暂停下载 %d%%", _element.ratio];
        _progressLabel.textColor = red_color;
    } else if (_element.status == 4) {
        [_actionButton setTitle:@"移除" forState:UIControlStateNormal];
        
        _progressView.progress = (CGFloat)_element.ratio/100;
        _progressLabel.text = @"已下载";
        _progressLabel.textColor = green_color;
        [TAPIUtility alertMessage:@"下载成功" success:YES toViewController:self];
    }
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    NSString* title = button.titleLabel.text;
    if ([title isEqualToString:@"下载"] || [title isEqualToString:@"继续下载"]) {
        [_offlineMap start:_record.cityID];
        [_actionButton setTitle:@"暂停" forState:UIControlStateNormal];
    } else if ([title isEqualToString:@"暂停"]) {
        [_offlineMap pause:_record.cityID];
        [_actionButton setTitle:@"继续下载" forState:UIControlStateNormal];
    } else if ([title isEqualToString:@"移除"]) {
        [TAPIUtility alertMessage:@"移除成功" success:YES toViewController:self];
        [_offlineMap remove:_record.cityID];
        _progressView.progress = 0;
        [_actionButton setTitle:@"下载" forState:UIControlStateNormal];
        _progressLabel.text = @"";
    }
}

@end
