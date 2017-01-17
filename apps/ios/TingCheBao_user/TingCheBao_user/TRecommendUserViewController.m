//
//  TRecommendUserViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/1/29.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TRecommendUserViewController.h"
#import <MessageUI/MessageUI.h>
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"

#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/QQApi.h>
#import <TencentOpenAPI/QQApiInterface.h>
#import "WXApiObject.h"
#import "WXApi.h"
#import "QRCodeGenerator.h"

#define button_width 60

@interface TRecommendUserViewController ()<MFMessageComposeViewControllerDelegate>

@property(nonatomic, retain) UILabel* promptLabel;
@property(nonatomic, retain) UIImageView* numberImgView;
@property(nonatomic, retain) UILabel* scanLabel;

@property(nonatomic, retain) UILabel* shareLabel;

@property(nonatomic, retain) UIButton* qqButton;
@property(nonatomic, retain) UILabel* qqLabel;
@property(nonatomic, retain) UIButton* friendButton;
@property(nonatomic, retain) UILabel* friendLabel;
@property(nonatomic, retain) UIButton* circleButton;
@property(nonatomic, retain) UILabel* circleLabel;
@property(nonatomic, retain) UIButton* messageButton;
@property(nonatomic, retain) UILabel* messageLabel;

@property(nonatomic, retain) NSString* link;
//短信
@property(nonatomic, retain) MFMessageComposeViewController* messageComposeViewController;

@property(nonatomic, retain) CVAPIRequest* request;
@end

@implementation TRecommendUserViewController

- (id)init {
    if (self = [super init]) {
        _promptLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 50, self.view.width, 50)];
        if (isIphone6 || isIphone6Plus)
            _promptLabel.top = 120;
        _promptLabel.text = @"推荐小伙伴，来用停车宝";
        _promptLabel.textColor = green_color;
        _promptLabel.textAlignment = NSTextAlignmentCenter;
        
        _numberImgView = [[UIImageView alloc] initWithFrame:CGRectMake((self.view.width - 170)/2, _promptLabel.bottom + 5, 170, 170)];
        _numberImgView.backgroundColor = [UIColor clearColor];
        [_numberImgView layer].magnificationFilter = kCAFilterNearest;
        
        _scanLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _numberImgView.bottom, self.view.width, 30)];
        _scanLabel.text = @"让好朋友扫一扫注册";
        _scanLabel.textAlignment = NSTextAlignmentCenter;
        _scanLabel.font = [UIFont systemFontOfSize:17];
        
        
        float padding = (self.view.width - 4*button_width)/5;
        
        _shareLabel = [[UILabel alloc] initWithFrame:CGRectMake(padding, self.view.bottom - 150, self.view.width, 30)];
        _shareLabel.text = @"分享到";
        _shareLabel.font = [UIFont systemFontOfSize:15];
        
        _qqButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _qqButton.frame = CGRectMake(padding, _shareLabel.bottom + 10, button_width, button_width);
        [_qqButton setImage:[UIImage imageNamed:@"recommend_qq.png"] forState:UIControlStateNormal];
        [_qqButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _qqLabel = [[UILabel alloc] initWithFrame:CGRectMake(_qqButton.left, _qqButton.bottom + 10, button_width, 20)];
        _qqLabel.textColor = gray_color;
        _qqLabel.text = @"QQ";
        _qqLabel.font = [UIFont systemFontOfSize:13];
        _qqLabel.textAlignment = NSTextAlignmentCenter;
        
        _friendButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _friendButton.frame = CGRectMake(_qqButton.right + padding, _qqButton.top , button_width, button_width);
        [_friendButton setImage:[UIImage imageNamed:@"recommend_friend.png"] forState:UIControlStateNormal];
        [_friendButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _friendLabel = [[UILabel alloc] initWithFrame:CGRectMake(_friendButton.left, _friendButton.bottom + 10, button_width, 20)];
        _friendLabel.textColor = gray_color;
        _friendLabel.text = @"微信好友";
        _friendLabel.font = [UIFont systemFontOfSize:13];
        _friendLabel.textAlignment = NSTextAlignmentCenter;
        
        _circleButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _circleButton.frame = CGRectMake(_friendButton.right + padding, _friendButton.top, button_width, button_width);
        [_circleButton setImage:[UIImage imageNamed:@"recommend_circle.png"] forState:UIControlStateNormal];
        [_circleButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _circleLabel = [[UILabel alloc] initWithFrame:CGRectMake(_circleButton.left - 5, _circleButton.bottom + 10, button_width + 10, 20)];
        _circleLabel.textColor = gray_color;
        _circleLabel.text = @"微信朋友圈";
        _circleLabel.font = [UIFont systemFontOfSize:13];
        _circleLabel.textAlignment = NSTextAlignmentCenter;
        
        _messageButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _messageButton.frame = CGRectMake(_circleButton.right + padding, _circleButton.top, button_width, button_width);
        [_messageButton setImage:[UIImage imageNamed:@"recommend_msg.png"] forState:UIControlStateNormal];
        [_messageButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _messageLabel = [[UILabel alloc] initWithFrame:CGRectMake(_messageButton.left, _messageButton.bottom + 10, button_width, 20)];
        _messageLabel.textColor = gray_color;
        _messageLabel.text = @"短信";
        _messageLabel.font = [UIFont systemFontOfSize:13];
        _messageLabel.textAlignment = NSTextAlignmentCenter;
        
        [self.view addSubview:_promptLabel];
        [self.view addSubview:_numberImgView];
        [self.view addSubview:_scanLabel];
        [self.view addSubview:_shareLabel];
        [self.view addSubview:_qqButton];
        [self.view addSubview:_qqLabel];
        [self.view addSubview:_friendButton];
        [self.view addSubview:_friendLabel];
        [self.view addSubview:_circleButton];
        [self.view addSubview:_circleLabel];
        [self.view addSubview:_messageButton];
        [self.view addSubview:_messageLabel];
    }
    return self;
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"推荐好友";
    self.view.backgroundColor = bg_view_color;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self requestLinkInfo];
}

#pragma mark request

- (void)requestLinkInfo {
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=getrecomurl&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        _link = [result objectForKey:@"info"];
        if (_link) {
//            _numberImgView.image = [QREncoder encode:_link size:5 correctionLevel:QRCorrectionLevelHigh];
            _numberImgView.image = [QRCodeGenerator qrImageForString:_link imageSize:200];
        }
    }];
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    if (!_link || [_link isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"加载失败，请重新进入"];
        return;
    }
        
    if (button == _qqButton) {
        TencentOAuth* tencentOAuth = [[TencentOAuth alloc] initWithAppId:QQID
                                                        andDelegate:self];
        if ([QQApi isQQInstalled] && [QQApi isQQSupportApi]) {
            NSData* data = UIImageJPEGRepresentation([UIImage imageNamed:@"recommend_user_wx.png"], 1);
            QQApiNewsObject* img = [QQApiNewsObject objectWithURL:[NSURL URLWithString:_link] title:@"我在用停车宝，邀你一起来" description:@"这是给新用户的红包，快来领取吧" previewImageData:data];
            SendMessageToQQReq *req = [SendMessageToQQReq reqWithContent:img];
            QQApiSendResultCode sent = [QQApiInterface sendReq:req];
            if (sent != EQQAPISENDSUCESS) {
                [TAPIUtility alertMessage:@"分享失败"];
            }
        }else{
            [TAPIUtility alertMessage:@"请先下载QQ或升级到最新版本"];
        }
        return;
    } else if (button == _friendButton || button == _circleButton) {
        WXWebpageObject* webObj = [WXWebpageObject object];
        webObj.webpageUrl = _link;
        
        WXMediaMessage* message = [WXMediaMessage message];
        message.title = @"我在用停车宝，邀你一起来";
        message.description = @"这是给新用户的红包，快来领取吧";
        message.mediaObject = webObj;
        UIImage* thumbImg = [TAPIUtility ajustImage:[UIImage imageNamed:@"recommend_user_wx.png"] size:CGSizeMake(30, 30)];
//        UIImage* thumbImg = [UIImage imageNamed:@"recommend_user_wx.png"];
        message.thumbData = UIImageJPEGRepresentation(thumbImg, 1);
        
        SendMessageToWXReq* req = [[SendMessageToWXReq alloc] init];
        if (button == _friendButton)
            req.scene = WXSceneSession;//聊天界面
        else if (button == _circleButton)
            req.scene = WXSceneTimeline;
        req.bText = NO;
        req.message = message;
        [WXApi sendReq:req];
    } else if (button == _messageButton) {
        _messageComposeViewController = [[MFMessageComposeViewController alloc] init];
        _messageComposeViewController.messageComposeDelegate = self;
        _messageComposeViewController.recipients = @[@"输入号码"];
        _messageComposeViewController.body = [NSString stringWithFormat:@"我在用停车宝付车费，送新人红包邀你一起来 %@", _link];
        [self presentViewController:_messageComposeViewController animated:YES completion:nil];
    }
}

#pragma mark MFMessageComposeViewControllerDelegate

- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
    [_messageComposeViewController dismissViewControllerAnimated:YES completion:nil];
    if (result == MessageComposeResultSent) {
        [TAPIUtility alertMessage:@"已发送成功" success:YES toViewController:self];
    }
}
//- (UIButton*)createButtonWithImageName:(NSString*)imgName text:(NSString*)text {
//    UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
//    UIImageView* imgView = [UIImageView alloc] initWithImage:[UIImage imageNamed:imgName];
//    imgView.frame = CGRectMake(0, 0, <#CGFloat width#>, <#CGFloat height#>)
//    UILabel* label = [UILabel alloc] initWithFrame:CGRectMake(0, <#CGFloat y#>, <#CGFloat width#>, <#CGFloat height#>)
//}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
