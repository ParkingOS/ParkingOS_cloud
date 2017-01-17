//
//  TRecommendViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/12/18.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRecommendViewController.h"
#import "TAPIUtility.h"
//#import "QREncoder.h"
#import <MessageUI/MessageUI.h>
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TRecommendHistoryController.h"
#import "TRecommendProfileView.h"
#import "QRCodeGenerator.h"

@interface TRecommendViewController()<MFMessageComposeViewControllerDelegate>


@property(nonatomic, retain) UIView* normalView;
@property(nonatomic, retain) UIView* unNormalView;
//normal
@property(nonatomic, retain) UILabel* promptLabel;
@property(nonatomic, retain) UIImageView* numberImgView;
@property(nonatomic, retain) UILabel* scanLabel;
@property(nonatomic, retain) UIButton* profileButton;
@property(nonatomic, retain) UILabel* noMeetLabel;
@property(nonatomic, retain) UIButton* messageButton;
//unnormal
@property(nonatomic, retain) UIImageView* locationImgView;
@property(nonatomic, retain) UILabel* locationLabel;
//popView
@property(nonatomic, retain) TRecommendProfileView* popView;

//定位
@property(nonatomic, retain) NSString* link;
//短信
@property(nonatomic, retain) MFMessageComposeViewController* messageComposeViewController;

@property(nonatomic, assign) BOOL isFirstLoad;

@property(nonatomic, retain) CVAPIRequest* request;

@end
@implementation TRecommendViewController

- (id)init {
    if (self = [super init]) {
        _normalView = [[UIView alloc] initWithFrame:self.view.frame];
        
        _promptLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 50, self.view.width, 50)];
        if (isIphone6 || isIphone6Plus)
            _promptLabel.top = 120;
        
        NSMutableAttributedString* attri = [[NSMutableAttributedString alloc] initWithString:@"推荐一个收费员，成功后\n奖励您30元"];
        [attri addAttributes:@{NSFontAttributeName : [UIFont systemFontOfSize:17], NSForegroundColorAttributeName : green_color} range:NSMakeRange(0, attri.string.length)];
        [attri addAttributes:@{NSFontAttributeName : [UIFont boldSystemFontOfSize:22], NSForegroundColorAttributeName : green_color} range:NSMakeRange(4, 3)];
        
        _promptLabel.attributedText = attri;
        _promptLabel.numberOfLines = 2;
//        _promptLabel.textColor = green_color;//加上这行，则attribute失效
        _promptLabel.textAlignment = NSTextAlignmentCenter;
        
        _numberImgView = [[UIImageView alloc] initWithFrame:CGRectMake((self.view.width - 170)/2, _promptLabel.bottom + 5, 170, 170)];
        _numberImgView.backgroundColor = [UIColor clearColor];
        [_numberImgView layer].magnificationFilter = kCAFilterNearest;
        
        _scanLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _numberImgView.bottom, self.view.width, 30)];
        _scanLabel.text = @"让收费员扫一扫注册";
        _scanLabel.textAlignment = NSTextAlignmentCenter;
        _scanLabel.font = [UIFont systemFontOfSize:17];
        
        _profileButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_profileButton setTitle:@"收费员有啥奖励?" forState:UIControlStateNormal];
        [_profileButton setTitleColor:[UIColor orangeColor] forState:UIControlStateNormal];
        [_profileButton setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
        [_profileButton setContentHorizontalAlignment:UIControlContentHorizontalAlignmentRight];
        _profileButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_profileButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _profileButton.frame = CGRectMake(self.view.width - 138, _scanLabel.bottom, 135, 30);
        
        _noMeetLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, self.view.height - 90, self.view.width, 30)];
        _noMeetLabel.text = @"收费员不在身边?";
        _noMeetLabel.textAlignment = NSTextAlignmentLeft;
        _noMeetLabel.font = [UIFont systemFontOfSize:13];
        
        _messageButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_messageButton setTitle:@"短信推荐" forState:UIControlStateNormal];
        [_messageButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_messageButton setTitleColor:[UIColor whiteColor] forState:UIControlStateHighlighted];
        [_messageButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        [_messageButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _messageButton.frame = CGRectMake(10, _noMeetLabel.bottom, self.view.width - 2*10, 40);
        _messageButton.layer.borderColor = [UIColor lightGrayColor].CGColor;
        _messageButton.layer.borderWidth = 1;
        _messageButton.layer.cornerRadius = 5;
        
        [_normalView addSubview:_promptLabel];
        [_normalView addSubview:_numberImgView];
        [_normalView addSubview:_scanLabel];
        [_normalView addSubview:_profileButton];
        [_normalView addSubview:_noMeetLabel];
        [_normalView addSubview:_messageButton];
        
        _unNormalView = [[UIView alloc] initWithFrame:self.view.frame];
        
        _locationImgView = [[UIImageView alloc] initWithFrame:CGRectMake((self.view.width - 80)/2, self.view.height/2 - 90, 80, 90)];
        _locationImgView.backgroundColor = [UIColor clearColor];
        _locationImgView.image = [UIImage imageNamed:@"need_location.png"];
        
        _locationLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, self.view.height/2 + 10, self.view.width, 50)];
        _locationLabel.text = @"无法定位，为了提高推荐信息准确性\n请在\"设置-隐私-定位服务\"\n中允许停车宝使用定位服务";
        _locationLabel.numberOfLines = 3;
        _locationLabel.textColor = gray_color;
        _locationLabel.textAlignment = NSTextAlignmentCenter;
        _locationLabel.font = [UIFont systemFontOfSize:13];
        
        [_unNormalView addSubview:_locationImgView];
        [_unNormalView addSubview:_locationLabel];
        
        _popView = [[TRecommendProfileView alloc] initWithFrame:self.view.frame];
        _popView.hidden = YES;
        
        [self.view addSubview:_normalView];
        [self.view addSubview:_unNormalView];
        [self.view addSubview:_popView];
        
        _normalView.hidden = _unNormalView.hidden = YES;
        
       
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(249, 249, 249);
    _isFirstLoad = YES;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @" 推荐收费员";
    UIButton* recommendButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [recommendButton setTitle:@"推荐记录" forState:UIControlStateNormal];
    recommendButton.titleLabel.font = [UIFont systemFontOfSize:13];
    [recommendButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    recommendButton.frame = CGRectMake(0, 0, 60, 30);
    [recommendButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:recommendButton];
    
    if (_isFirstLoad)
            [self requestLinkInfo];
    _isFirstLoad = NO;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

#pragma mark request

- (void)requestLinkInfo {
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=regcarmsg&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        _link = [result objectForKey:@"info"];
        if (_link) {
//            _numberImgView.image = [QREncoder encode:_link size:i correctionLevel:QRCorrectionLevelHigh];
            _numberImgView.image = [QRCodeGenerator qrImageForString:_link imageSize:200];
            _normalView.hidden = NO;
        }
    }];
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    if (button == _messageButton) {
        _messageComposeViewController = [[MFMessageComposeViewController alloc] init];
        _messageComposeViewController.messageComposeDelegate = self;
        _messageComposeViewController.recipients = @[@"输入号码"];
        _messageComposeViewController.body = [NSString stringWithFormat:@"停车宝收费，注册送10元，每单奖两元！注册地址:%@", _link];
        [self presentViewController:_messageComposeViewController animated:YES completion:nil];
    } else if (button == _profileButton) {
        [_popView show:YES];
    }else {
        TRecommendHistoryController* vc = [[TRecommendHistoryController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
    }
}

#pragma mark MFMessageComposeViewControllerDelegate

- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
    [_messageComposeViewController dismissViewControllerAnimated:YES completion:nil];
    if (result == MessageComposeResultSent) {
        [TAPIUtility alertMessage:@"已发送成功" success:YES toViewController:self];
    }
}

@end
