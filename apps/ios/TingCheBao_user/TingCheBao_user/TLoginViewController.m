//
//  TLoginViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-4.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TLoginViewController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TSignUpViewController.h"
#import <MessageUI/MessageUI.h>
#import "THomeViewController.h"
#import "WQUserDataManager.h"
#import <Security/SecItem.h>
#import "TViewController.h"

#define padding 20

@interface TLoginViewController ()<CVAPIModelDelegate, UIWebViewDelegate, MFMessageComposeViewControllerDelegate, UIAlertViewDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UIImageView* phoneImgView;
@property(nonatomic, retain) UITextField* phoneField;

@property(nonatomic, retain) UIImageView* checkImgView;
@property(nonatomic, retain) UITextField* checkField;
@property(nonatomic, retain) UIView* lineView;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIButton* requestCheckButton;
@property(nonatomic, retain) UILabel* messageLabel;


@property(nonatomic, retain) UIButton* loginButton;

@property(nonatomic, retain) NSTimer* checkTimer;
@property(nonatomic, assign) int time;
@property(nonatomic, retain) CVAPIRequestModel* checkModel;
@property(nonatomic, retain) MFMessageComposeViewController* messageComposeViewController;
@property(nonatomic, retain) UIAlertView* messageAlert;
@property(nonatomic, retain) NSString* messageCode;
@property(nonatomic, retain) NSString* messageMobile;
@property(nonatomic, retain) NSTimer* timer;

@property(nonatomic, assign) int requestTimes;

@property(nonatomic, retain) CVAPIRequest* loginRequest;
@property(nonatomic, retain) CVAPIRequest* checkRequest;
@property(nonatomic, retain) CVAPIRequest* messageRequest;
@property(nonatomic, retain) CVAPIRequest* notifRequest;

@end

@implementation TLoginViewController

- (id)init {
    if (self = [super init]) {
        _scrollView = [[UIScrollView alloc] initWithFrame:self.view.frame];
        _scrollView.contentSize = CGSizeMake(self.view.width, self.view.height);
        
        //topView
        _topView = [[UIView alloc] initWithFrame:CGRectMake(0, 20, self.view.width, 83)];
        _topView.backgroundColor = [UIColor whiteColor];
        
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 1)];
        _lineView.backgroundColor = bg_line_color;
        
        _phoneImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"person"]];
        _phoneImgView.frame = CGRectMake(padding, _lineView.bottom + 10, 20, 20);
        
        _phoneField = [[UITextField alloc] init];
        if (isIphoneNormal)
            _phoneField.frame = CGRectMake(_phoneImgView.right + 10, _lineView.bottom, 150, 40);
        else
            _phoneField.frame = CGRectMake(_phoneImgView.right + 10, _lineView.bottom, 200, 40);
        _phoneField.keyboardType = UIKeyboardTypeNumberPad;
        _phoneField.placeholder = @"手机号";
        _phoneField.font = [UIFont systemFontOfSize:14];
        _phoneField.clearButtonMode = UITextFieldViewModeWhileEditing;
        [_phoneField setValue:[UIColor lightGrayColor] forKeyPath:@"_placeholderLabel.textColor"];
        
        _line1View = [[UIView alloc] initWithFrame:CGRectMake(padding, _phoneField.bottom, self.view.width - 5, 1)];
        _line1View.backgroundColor = bg_line_color;
        
        _checkImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"checkNumber"]];
        _checkImgView.frame = CGRectMake(padding, _line1View.bottom + 10, 20, 20);
        
        _checkField = [[UITextField alloc] init];
        if (isIphoneNormal)
            _checkField.frame = CGRectMake(_checkImgView.right + 10, _line1View.bottom, 150, 40);
        else
            _checkField.frame = CGRectMake(_checkImgView.right + 10, _line1View.bottom, 200, 40);
        _checkField.keyboardType = UIKeyboardTypeNumberPad;
        _checkField.placeholder = @"验证码";
        _checkField.font = [UIFont systemFontOfSize:14];
        _checkField.clearButtonMode = UITextFieldViewModeWhileEditing;
        [_checkField setValue:[UIColor lightGrayColor] forKeyPath:@"_placeholderLabel.textColor"];
        
        _requestCheckButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_requestCheckButton setTitle:@"获取" forState:UIControlStateNormal];
        [_requestCheckButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _requestCheckButton.titleLabel.font = [UIFont systemFontOfSize:14];
        [_requestCheckButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _requestCheckButton.frame = CGRectMake(self.view.width - 101, _checkField.top, 100, 40);
        [_requestCheckButton addTarget:self action:@selector(clickedRequestCheckButton) forControlEvents:UIControlEventTouchUpInside];
        
        _line2View = [[UIView alloc] initWithFrame:CGRectMake(0, _checkField.bottom, self.view.width, 1)];
        _line2View.backgroundColor = bg_line_color;
        
        [_topView addSubview:_phoneImgView];
        [_topView addSubview:_phoneField];
        [_topView addSubview:_lineView];
        [_topView addSubview:_checkImgView];
        [_topView addSubview:_checkField];
        [_topView addSubview:_requestCheckButton];
        [_topView addSubview:_line1View];
        [_topView addSubview:_line2View];
        
        _messageLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width - 230, _topView.bottom + 20, 220, 30)];
        _messageLabel.backgroundColor = [UIColor clearColor];
        _messageLabel.font = [UIFont systemFontOfSize:13];
        _messageLabel.textAlignment = NSTextAlignmentRight;
        NSAttributedString* message = [[NSAttributedString alloc] initWithString:@"未收到验证码?用短信登录!" attributes:@{NSUnderlineStyleAttributeName : @(NSUnderlineStyleSingle), NSForegroundColorAttributeName : orange_color}];
        _messageLabel.attributedText = message;
        UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        [_messageLabel addGestureRecognizer:tapGesture];
        _messageLabel.userInteractionEnabled = YES;
        
        _loginButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_loginButton setTitle:@"点我一键登录" forState:UIControlStateNormal];
        [_loginButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_loginButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _loginButton.frame = CGRectMake(padding, _messageLabel.bottom + 20, self.view.width - 2*padding, 50);
        [_loginButton addTarget:self action:@selector(clickedLoginButton) forControlEvents:UIControlEventTouchUpInside];
        
        [_scrollView addSubview:_topView];
        [_scrollView addSubview:_messageLabel];
        [_scrollView addSubview:_loginButton];
        
        
        [self.view addSubview:_scrollView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"一键登录";
    self.view.backgroundColor = bg_view_color;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    NSNotificationCenter* center = [NSNotificationCenter defaultCenter];
    [center addObserver:self selector:@selector(willShowKeyboard:) name:UIKeyboardWillChangeFrameNotification object:nil];
    
    //打开键盘
    [_phoneField becomeFirstResponder];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [_loginRequest cancel];
    [_checkRequest cancel];
    [_messageRequest cancel];
}

#pragma mark keyboard
- (void)willShowKeyboard:(NSNotification*)notification {
    NSDictionary *notificationInfo = [notification userInfo];
    
    // Get the end frame of the keyboard in screen coordinates.
    CGRect finalKeyboardFrame = [[notificationInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    
    // Convert the finalKeyboardFrame to view coordinates to take into account any rotation
    // factors applied to the window’s contents as a result of interface orientation changes.
    finalKeyboardFrame = [self.view convertRect:finalKeyboardFrame fromView:self.view.window];
    
    
    // Get the animation curve and duration
    UIViewAnimationCurve animationCurve = (UIViewAnimationCurve) [[notificationInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] integerValue];
    NSTimeInterval animationDuration = [[notificationInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    // Animate view size synchronously with the appearance of the keyboard.
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:animationDuration];
    [UIView setAnimationCurve:animationCurve];
    [UIView setAnimationBeginsFromCurrentState:YES];
    CGFloat height = _scrollView.bottom - finalKeyboardFrame.origin.y;
    if (height < 0)
        height = 0;
    _scrollView.contentInset = UIEdgeInsetsMake(0, 0, height, 0);
    [UIView commitAnimations];
    
}

#pragma private

//登录
- (void)clickedLoginButton {
    
    //验证手机号和验证码格式
    if ([_phoneField.text isEqualToString:@""] || [_checkField.text isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"手机号码或验证码不能为空" success:NO toViewController:self];
        return;
    } else if (![TAPIUtility isValidOfPhone:_phoneField.text]){
        [TAPIUtility alertMessage:@"手机号码格式不正确" success:NO toViewController:self];
        return;
    } else if ([_checkField.text length] != 4) {
        [TAPIUtility alertMessage:@"验证码格式不正确" success:NO toViewController:self];
        return;
    }
    
    NSString* apiPath = [NSString stringWithFormat:@"carlogin.do?action=validcode&code=%@&mobile=%@&imei=%@", _checkField.text, _phoneField.text,[self uuid]];
    _loginRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _loginRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_loginRequest completion:^(NSDictionary *result, NSError *error) {
        int code = [[result objectForKey:@"info"] integerValue];
        [self handleLoginResult:code];
    }];

}

-(NSString*) uuid {
    
    NSString *strUUID = [WQUserDataManager readPassWord];
    
    if (strUUID.length == 0)

    {
        CFUUIDRef uuidRef = CFUUIDCreate(kCFAllocatorDefault);

        strUUID = (__bridge NSString *)CFUUIDCreateString (kCFAllocatorDefault,uuidRef);

        [WQUserDataManager savePassWord:strUUID];

    }
    return strUUID;
}

//获取 验证码
- (void)clickedRequestCheckButton {
    //验证手机号
    if ([_phoneField.text isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"手机号码不能为空" success:NO toViewController:self];
        return;
    } else if (![TAPIUtility isValidOfPhone:_phoneField.text]){
        [TAPIUtility alertMessage:@"手机号码格式不正确" success:NO toViewController:self];
        return;
    }
    
    //check按钮不让点
    [_requestCheckButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor grayColor]] forState:UIControlStateNormal];
    _requestCheckButton.userInteractionEnabled = NO;
    
    NSString* apiPath = [NSString stringWithFormat:@"carlogin.do?action=login&mobile=%@", _phoneField.text];
    _checkRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _checkRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    _checkModel = [[CVAPIRequestModel alloc] init];
    _checkModel.delegate = self;
    [_checkModel sendRequest:_checkRequest completion:^(NSDictionary *result, NSError *error) {
        int code = [[result objectForKey:@"info"] integerValue];
        if (code == 0) {
            [TAPIUtility alertMessage:@"验证码发送成功" success:YES toViewController:self];
            
            [_checkField becomeFirstResponder];
            
            if (_checkTimer) {
                [_checkTimer invalidate];
            }
            //开启60秒定时器
            _checkTimer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(updateCheckTime:) userInfo:nil repeats:YES];
            _time = 60;
            [_requestCheckButton setTitle:[NSString stringWithFormat:@"重新发送(%d秒)", _time] forState:UIControlStateNormal];
            
        } else {
            //恢复check
            [_requestCheckButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
            _requestCheckButton.userInteractionEnabled = YES;
            
            [TAPIUtility alertMessage:@"验证码发送失败" success:NO toViewController:self];
        }
    }];
}

//定时器
- (void)updateCheckTime:(NSTimer*)timer {
    _time --;
    if (_time == 0) {
        //恢复check
        [_requestCheckButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _requestCheckButton.userInteractionEnabled = YES;
        
        [_requestCheckButton setTitle:@"重新发送" forState:UIControlStateNormal];
        //停止 定时器
        [_checkTimer invalidate];
    } else {
        [_requestCheckButton setTitle:[NSString stringWithFormat:@"重新发送(%d秒)", _time] forState:UIControlStateNormal];
    }
}

//短信验证
- (void)handleTapGesture:(UIGestureRecognizer*)gesture {
    //验证手机号
    if ([_phoneField.text isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"手机号码不能为空" success:NO toViewController:self];
        return;
    } else if (![TAPIUtility isValidOfPhone:_phoneField.text]){
        [TAPIUtility alertMessage:@"手机号码格式不正确" success:NO toViewController:self];
        return;
    }
    _messageLabel.textColor = [UIColor whiteColor];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.2),
                   dispatch_get_main_queue(), ^{
                        _messageLabel.textColor = orange_color;
                   });
    
    NSString* apiPath = [NSString stringWithFormat:@"carlogin.do?action=dologin&mobile=%@&imei=%@", _phoneField.text,[self uuid]];
    _messageRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _messageRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_messageRequest completion:^(NSDictionary *result, NSError *error) {
        NSString* mesg = [result objectForKey:@"mesg"];
        _messageCode = [result objectForKey:@"code"];
        _messageMobile = [result objectForKey:@"tomobile"];
        if ([mesg isEqualToString:@"0"]) {
            //成功 发送短信
            _messageAlert = [[UIAlertView alloc] initWithTitle:@"发送验证码" message:@"将要把验证码发送至服务器(请不要修改短信内容)" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"发送", nil];
            _messageAlert.delegate = self;
            [_messageAlert show];
        } else {
            [TAPIUtility alertMessage:@"获取验证码失败" success:NO toViewController:self];
        }
    }];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != alertView.cancelButtonIndex) {
        if (_messageCode && _messageMobile) {
            _messageComposeViewController = [[MFMessageComposeViewController alloc] init];
            _messageComposeViewController.messageComposeDelegate = self;
            _messageComposeViewController.recipients = @[_messageMobile];
            _messageComposeViewController.body = [NSString stringWithFormat:@"%@【停车宝】", _messageCode];
            [self presentViewController:_messageComposeViewController animated:YES completion:nil];
        }
    }
}

- (void)updateTime:(NSTimer*)timer {
    //最多执行5次
    _requestTimes ++;
    if (_requestTimes > 5) {
        [_timer invalidate];
//        [SVProgressHUD dismiss];
        [TAPIUtility alertMessage:@"加载超时，请重新操作" success:NO toViewController:self];
        return;
    }
    //每2秒 取一次消息
    NSString* apiPath = [NSString stringWithFormat:@"carmessage.do?mobile=%@&msgid=&action=checkcode", _phoneField.text];
    
    _notifRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath mserver:YES];
    _notifRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [model sendRequest:_notifRequest completion:^(NSDictionary *result, NSError *error) {
        if (result) {
            if ([result count] != 0) {
                [_timer invalidate];
                int code = [[[result objectForKey:@"info"] objectForKey:@"result"] intValue];
                [self handleLoginResult:code];
            }
        }
    }];
}

//登陆处理
- (void)handleLoginResult:(int)code {
    if (code == 1) {
        //登录成功
        [_scrollView endEditing:YES];
        [[NSUserDefaults standardUserDefaults] setObject:_phoneField.text forKey:save_phone];
        //上传device token
        [TAPIUtility sendDeviceToken];
        if ([self.navigationController.viewControllers count] == 1) {
            [self.navigationController dismissViewControllerAnimated:YES completion:nil];
        } else {
            [self.navigationController popViewControllerAnimated:YES];
        }
        [[THomeViewController share] checkHoliday];
        
        //查询车牌号
        [[TViewController share] requestCarNumber];
        
        if (_completer) {
            //登陆成功后的回调函数
            _completer();
        }
    } else if (code == 2) {
        [_scrollView endEditing:YES];
        //设为0.22秒，因为sv dismiss动画1.5秒,否则可极小的机率signup页面会卡死，但是我从来没重现过
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.22),
                       dispatch_get_main_queue(), ^{
                           //第一次注册，需要填车牌号
                           TSignUpViewController* vc = [[TSignUpViewController alloc] init];
                           vc.phoneNum = _phoneField.text;
                           vc.completer = _completer;
                           [self.navigationController pushViewController:vc animated:YES];
                       });
    } else {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, NSEC_PER_SEC * 0.22),
                       dispatch_get_main_queue(), ^{
                           [TAPIUtility alertMessage:@"验证错误" success:NO toViewController:self];
                       });
    }
}
#pragma mark CVAPIModelDelegate

- (void)modelDidFailWithError:(NSError *)error model:(CVAPIRequestModel *)model request:(CVAPIRequest *)request {
    if (model == _checkModel) {
        //如果网络失败，恢复check
        [_requestCheckButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _requestCheckButton.userInteractionEnabled = YES;
    }
}

#pragma mark MFMessageComposeViewControllerDelegate

- (void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result {
    [_messageComposeViewController dismissViewControllerAnimated:YES completion:nil];
    if (result == MessageComposeResultSent) {
        _timer = [NSTimer scheduledTimerWithTimeInterval:2.0f target:self selector:@selector(updateTime:) userInfo:nil repeats:YES];
        _requestTimes = 0;
    }
}

@end
