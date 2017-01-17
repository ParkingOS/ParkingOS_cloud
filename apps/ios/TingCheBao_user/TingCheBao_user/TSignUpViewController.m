//
//  TSignUpViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-9.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TSignUpViewController.h"
#import "CVAPIRequestModel.h"
#import "TAPIUtility.h"
#import "TTicketHelpController.h"
#import "THomeViewController.h"
#import "TViewController.h"
#import "UIButton+Utilities.h"
#import "UILabel+Utilities.h"

#define padding 20

@interface TSignUpViewController ()<MBProgressHUDDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UIView* topView;
@property(nonatomic, retain) UILabel* numberLabel;
@property(nonatomic, retain) UITextField* numberField;
@property(nonatomic, retain) UILabel* accessNumberLabel;
@property(nonatomic, retain) UITextField* accessNumberField;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIView* line3View;

@property(nonatomic, retain) UIButton* checkBoxButton;
@property(nonatomic, retain) UILabel* agreeLabel;
@property(nonatomic, retain) UIButton* policyButton;

@property(nonatomic, retain) UIButton* signUpButton;

@property(nonatomic, retain) CVAPIRequest* request;

@property(nonatomic, readwrite) UIButton       * noSignUpButton;
@property(nonatomic, readwrite) UIButton       * helpButton;
@property(nonatomic, readwrite) UIImageView    * helpImageView;
@property(nonatomic, readwrite) UIView         * owerNumberView;

@end

@implementation TSignUpViewController

- (id)init {
    if (self = [super init]) {
        _scrollView = [[UIScrollView alloc] initWithFrame:self.view.frame];
        _scrollView.contentSize = CGSizeMake(self.view.width, self.view.height);
        
        _topView = [[UIView alloc] initWithFrame:CGRectMake(0, 20, self.view.width, 83)];
        _topView.backgroundColor = [UIColor whiteColor];
        
        _line1View = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 1)];
        _line1View.backgroundColor = bg_line_color;
        
        _numberLabel = [[UILabel alloc] initWithFrame:CGRectMake(padding, _line1View.bottom, 60, 40)];
        _numberLabel.text = @"车牌号:";
        _numberLabel.textColor = gray_color;
        _numberLabel.font = [UIFont systemFontOfSize:14];
        
        _numberField = [[UITextField alloc] initWithFrame:CGRectMake(_numberLabel.right + padding/2, _numberLabel.top, self.view.width - 3/2* padding - _numberLabel.width - 115, 40)];
//        _numberField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _numberField.placeholder = @"填写您的车牌号";
        [_numberField setValue:[UIColor lightGrayColor] forKeyPath:@"_placeholderLabel.textColor"];
        _numberField.font = [UIFont systemFontOfSize:14];
        
        _line2View = [[UIView alloc] initWithFrame:CGRectMake(0, _numberField.bottom, self.view.width, 1)];
        _line2View.backgroundColor = bg_line_color;
        
        _accessNumberLabel = [[UILabel alloc] initWithFrame:CGRectMake(padding, _line2View.bottom, 60, 40)];
        _accessNumberLabel.text = @"邀请码:";
        _accessNumberLabel.textColor = gray_color;
        _accessNumberLabel.font = [UIFont systemFontOfSize:14];
        
        _accessNumberField = [[UITextField alloc] initWithFrame:CGRectMake(_accessNumberLabel.right + padding/2, _accessNumberLabel.top, self.view.width - 3/2* padding - _numberLabel.width, 40)];
        _accessNumberField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _accessNumberField.placeholder = @"可以不填写";
        _accessNumberField.keyboardType = UIKeyboardTypeNumberPad;
        [_accessNumberField setValue:[UIColor lightGrayColor] forKeyPath:@"_placeholderLabel.textColor"];
        _accessNumberField.font = [UIFont systemFontOfSize:14];
        
        _line3View = [[UIView alloc] initWithFrame:CGRectMake(0, _accessNumberField.bottom, self.view.width, 1)];
        _line3View.backgroundColor = bg_line_color;
        
        [_topView addSubview:_line1View];
        [_topView addSubview:_line2View];
        [_topView addSubview:_line3View];
        [_topView addSubview:_numberLabel];
        [_topView addSubview:_numberField];
        [_topView addSubview:_accessNumberLabel];
        [_topView addSubview:_accessNumberField];
        [_topView addSubview:self.noSignUpButton];

        
        _checkBoxButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_checkBoxButton setImage:[UIImage imageNamed:@"checkbox_ticked.png"] forState:UIControlStateNormal];
        _checkBoxButton.frame = CGRectMake(padding, _topView.bottom + 70, 30, 30);
        _checkBoxButton.selected = YES;
        [_checkBoxButton addTarget:self action:@selector(clickedCheckBoxButton) forControlEvents:UIControlEventTouchUpInside];
        
        _agreeLabel = [[UILabel alloc] initWithFrame:CGRectMake(_checkBoxButton.right + 4, _checkBoxButton.top, 81, 30)];
        _agreeLabel.backgroundColor = [UIColor clearColor];
        _agreeLabel.text = @"已阅读并同意";
        _agreeLabel.textColor = [UIColor blackColor];
        _agreeLabel.font = [UIFont systemFontOfSize:13];
        
        _policyButton = [UIButton buttonWithType:UIButtonTypeSystem];
        [_policyButton setTitle:@"使用条款和隐私政策" forState:UIControlStateNormal];
        [_policyButton setTitleColor:RGBCOLOR(77, 157, 246) forState:UIControlStateNormal];
        [_policyButton addTarget:self action:@selector(policyButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        _policyButton.frame = CGRectMake(_agreeLabel.right, _agreeLabel.top + 3, 140, 22);
        
        _signUpButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_signUpButton setTitle:@"完成注册" forState:UIControlStateNormal];
        [_signUpButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _signUpButton.layer.cornerRadius = 5;
        [_signUpButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _signUpButton.frame = CGRectMake(padding, _checkBoxButton.bottom + padding, self.view.width - 2*padding, 60);
        [_signUpButton addTarget:self action:@selector(signUpButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        [_scrollView addSubview:_topView];
        [_scrollView addSubview:_checkBoxButton];
        [_scrollView addSubview:_agreeLabel];
        [_scrollView addSubview:_policyButton];
        [_scrollView addSubview:_signUpButton];
        [_scrollView addSubview:self.helpButton];
        [_scrollView addSubview:self.helpImageView];
        
        [self.view addSubview:_scrollView];
        
    }
    return self;
}

- (UIButton *)noSignUpButton
{
    if (!_noSignUpButton) {
        _noSignUpButton = [UIButton titleButtonWithFrame:CGRectMake(self.view.width - 110, _numberField.top, 100, 40) title:@"车牌已被注册?点这里" textColor:RGBCOLOR(77, 157, 246) backgroundColor:[UIColor clearColor] target:self action:@selector(noSignUpButtonClick:)];
        _noSignUpButton.titleLabel.font = [UIFont systemFontOfSize:10];
        
        UIView *view = [[UIView alloc] initWithFrame:CGRectMake(2, 26, _noSignUpButton.width - 5, 0.5)];
        view.backgroundColor = RGBCOLOR(77, 157, 246);
        [_noSignUpButton addSubview:view];
    }
    return _noSignUpButton;
}

- (void)noSignUpButtonClick:(UIButton *)sender
{
    TTicketHelpController* vc = [[TTicketHelpController alloc] initWithName:@"帮助" url:@"http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=209112920&idx=1&sn=2d5faa08b2075e6d8471dc6ce0955caf#rd"];
    [self.navigationController pushViewController:vc animated:YES];
}

- (UIButton *)helpButton
{
    if (!_helpButton) {
        _helpButton = [UIButton titleButtonWithFrame:CGRectMake(self.view.width - 165, _topView.bottom + 5, 120, 30) title:@"请勿填写他人车牌" textColor:RGBCOLOR(170, 170, 170) backgroundColor:[UIColor clearColor] target:self action:@selector(helpButtonClick:)];
        _helpButton.titleLabel.font = [UIFont boldSystemFontOfSize:15];
    }
    return _helpButton;
}

- (void)helpButtonClick:(UIButton *)sender
{
    [[UIApplication sharedApplication].keyWindow addSubview:self.owerNumberView];
}

- (UIView *)owerNumberView
{
    if (!_owerNumberView) {
        _owerNumberView = [[UIView alloc] initWithFrame:[ UIScreen mainScreen ].bounds];
        _owerNumberView.backgroundColor = [[UIColor blackColor] colorWithAlphaComponent:0.5];
        
        UIView *titleView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width - 30, 210)];
        titleView.backgroundColor = [UIColor whiteColor];
        
        titleView.center = _owerNumberView.center;
        
        titleView.layer.masksToBounds = YES;
        titleView.layer.cornerRadius  = 8;
        titleView.layer.borderColor   = RGBCOLOR(210, 210, 210).CGColor;
        titleView.layer.borderWidth   = 0.5;
        
        UILabel *titleLabel = [UILabel labelWithTitle:@"请勿填写他人车牌" font:20 textColor:[UIColor blackColor] backgroundColor:[UIColor clearColor] Frame:CGRectMake(10, 15, 180, 40)];
        titleLabel.font = [UIFont boldSystemFontOfSize:18];
        titleLabel.textAlignment = NSTextAlignmentLeft;
        
        UIButton *openButton = [UIButton imageButtonWithFrame:CGRectMake(titleView.width - 45, 20, 30, 30) image:@"car_x.png" target:self action:@selector(openButtonClick:)];
        
        UILabel *detalabel = [UILabel labelWithTitle:@""
                                                font:20
                                           textColor:RGBCOLOR(120, 120, 120)
                                     backgroundColor:[UIColor clearColor]
                                               Frame:CGRectMake(10, titleLabel.bottom + 20, titleView.width - 20, 110)];
        detalabel.font = [UIFont boldSystemFontOfSize:15];
        detalabel.textAlignment = NSTextAlignmentLeft;
        
        NSString *labelText = @"当你填写车牌的真实车主上传行驶证通过认证时,您的所有停车券将被清空,您的手机账户,手机串号,被拉入用券黑名单一个月.";
        
        NSMutableAttributedString *attributedString = [[NSMutableAttributedString alloc] initWithString:labelText];
        NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
        
        [paragraphStyle setLineSpacing:6];//调整行间距
        
        [attributedString addAttribute:NSParagraphStyleAttributeName value:paragraphStyle range:NSMakeRange(0, [labelText length])];
        detalabel.attributedText = attributedString;

        [titleView addSubview:titleLabel];
        [titleView addSubview:openButton];
        [titleView addSubview:detalabel];
        [_owerNumberView addSubview:titleView];
    }
    return _owerNumberView;
}

- (void)openButtonClick:(UIButton *)sender
{
    [self.owerNumberView removeFromSuperview];
}

- (UIImageView *)helpImageView
{
    if (!_helpImageView) {
        _helpImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"car_i.png"]];
        _helpImageView.frame = CGRectMake(_helpButton.right + 10, _helpButton.top + 5, 20, 20);
    }
    return _helpImageView;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"注册车牌号";
    self.view.backgroundColor = bg_view_color;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    NSNotificationCenter* center = [NSNotificationCenter defaultCenter];
    [center addObserver:self selector:@selector(willShowKeyboard:) name:UIKeyboardWillChangeFrameNotification object:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [_request cancel];
}

- (void)clickedCheckBoxButton {
    if (_checkBoxButton.selected) {
        [_checkBoxButton setImage:[UIImage imageNamed:@"checkbox_not_ticked.png"] forState:UIControlStateNormal];
        _checkBoxButton.selected = NO;
        _signUpButton.userInteractionEnabled = NO;
        [_signUpButton setBackgroundImage:[TAPIUtility imageWithColor:[UIColor grayColor]] forState:UIControlStateNormal];
    } else {
        [_checkBoxButton setImage:[UIImage imageNamed:@"checkbox_ticked2.png"] forState:UIControlStateNormal];
        _checkBoxButton.selected = YES;
        _signUpButton.userInteractionEnabled = YES;
        [_signUpButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
    }
}

- (void)policyButtonTouched:(UIButton*)button {
    TTicketHelpController* vc = [[TTicketHelpController alloc] initWithName:@"使用条款和隐私政策" url:[TAPIUtility getNetworkWithUrl:@"parkservice.html"]];
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)signUpButtonTouched {
    if ([_numberField.text isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"车牌号不能为空" success:NO toViewController:nil];
        return;
    } else if (![TAPIUtility isValidOfCarNumber:_numberField.text]) {
        [TAPIUtility alertMessage:@"车牌号格式不正确" success:NO toViewController:nil];
        return;
    }
    [self.view endEditing:YES];
    
    NSMutableDictionary* dictionary = [NSMutableDictionary dictionary];
    [dictionary setObject:@"addcar" forKey:@"action"];
    [dictionary setObject:_numberField.text forKey:@"carnumber"];
    [dictionary setObject:_phoneNum forKey:@"mobile"];
    [dictionary setObject:_accessNumberField.text forKey:@"recom_code"];
    NSString* apiPath = [NSString stringWithFormat:@"carlogin.do?%@", [CVAPIRequest GETParamString:dictionary]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        int code = [[result objectForKey:@"info"] integerValue];
        if (code == 4) {
            [TAPIUtility alertMessage:@"注册成功，但推荐码不存在" success:YES toViewController:self];
            [[NSUserDefaults standardUserDefaults] setObject:_phoneNum forKey:save_phone];
            
            //上传device token
            [TAPIUtility sendDeviceToken];
            
            //查询车牌号
            [[TViewController share] requestCarNumber];
            
            //成功回调函数
            if (_completer) {
                _completer();
            }
        } else if (code == 3){
            [TAPIUtility alertMessage:@"注册成功" success:YES toViewController:self];
            [[NSUserDefaults standardUserDefaults] setObject:_phoneNum forKey:save_phone];
            //上传device token
            [TAPIUtility sendDeviceToken];
            
            //查询车牌号
            [[TViewController share] requestCarNumber];
            
            //成功回调函数
            if (_completer) {
                _completer();
            }
        } else if (code == -9) {
            [TAPIUtility alertMessage:@"车牌号已存在"];
        }else {
            [TAPIUtility alertMessage:@"注册失败"];
        }
    }];
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

#pragma mark MBProgressHUDDelegate

- (void)hudWasHidden:(MBProgressHUD *)hud {
    BOOL flag = NO;
    for (UIViewController* vc in self.navigationController.viewControllers) {
        if ([vc isKindOfClass:[THomeViewController class]]) {
            flag = YES;
            break;
        }
    }
    if (flag) {
        UIViewController* vc = [self.navigationController.viewControllers objectAtIndex:[self.navigationController.viewControllers count] - 3];
        [self.navigationController popToViewController:vc animated:YES];
    } else {
        [self.navigationController dismissViewControllerAnimated:YES completion:nil];
    }
    [[THomeViewController share] checkHoliday];
}
@end
