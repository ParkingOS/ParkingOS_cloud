//
//  TParkTicketPackageViewController.m
//  TingCheBao_user
//
//  Created by yangshaojin on 15-3-9.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//
#define kColor(r,g,b) [UIColor colorWithRed:r/255.0 green:g/255.0 blue:b/255.0 alpha:1]

#import "TShareItem.h"
#import "NSString+CVURLEncoding.h"
#import "TTicketHelpController.h"
#import "TParkTicketPackageViewController.h"
#import "UIImageView+WebCache.h"
#import "TWeixin.h"

@interface TParkTicketPackageViewController ()

@property (nonatomic, readwrite) UIScrollView *scrollview;
@property (nonatomic, readwrite) UILabel *barLabel;
@property (nonatomic, readwrite) UIButton *barLeftButton;
@property (nonatomic, readwrite) UIButton *barRightButton;

@property (nonatomic, readwrite) UIView *numberView;
@property (nonatomic, readwrite) UIView *priceView;

@property (nonatomic, readwrite) UILabel *numberLabel;
@property (nonatomic, readwrite) UILabel *priceLabel;
@property (nonatomic, readwrite) UIImageView *pingImageView;

@property (nonatomic, readwrite) UILabel *smallNumberLabel;
@property (nonatomic, readwrite) UILabel *smallPriceLabel;

@property (nonatomic, readwrite) UITextView *textView;
@property (nonatomic, readwrite) UILabel *moneyLabel;
@property (nonatomic, readwrite) UIButton *shareButton;
@property (nonatomic, readwrite) UILabel *normalLabel;

@property (nonatomic, readwrite) CVAPIRequest* currentOrderReqeust;
@property (nonatomic, readwrite) CVAPIRequestModel* currentOrderModel;
@property (nonatomic, readwrite) TShareItem *item;

@end

@implementation TParkTicketPackageViewController

- (instancetype)init
{
    if (self = [super init]) {
        [[UINavigationBar appearance] setBarTintColor:kColor(188.0, 18.0, 31.0)];
//        if ([[UIDevice currentDevice].systemVersion floatValue] >= 7.0) {
//            self.view.height -= 64;
//            self.edgesForExtendedLayout = UIRectEdgeNone;
//        } else if ([[UIDevice currentDevice].systemVersion floatValue] >= 6.0) {
//            self.view.height -= 44;
//        }
//        
        [self.view addSubview:self.scrollview];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(willShowKeyboard:) name:UIKeyboardWillChangeFrameNotification object:nil];
        
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.titleView.text = @"停车券礼包";
    self.titleView.textColor = [UIColor whiteColor];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    UIBarButtonItem *left = [[UIBarButtonItem alloc] initWithCustomView:self.barLeftButton];
    self.navigationItem.leftBarButtonItem = left;
    UIBarButtonItem *right = [[UIBarButtonItem alloc] initWithCustomView:self.barRightButton];
    self.navigationItem.rightBarButtonItem = right;
    
    [self requstData];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_currentOrderModel cancel];
    [_currentOrderReqeust cancel];
    [[UINavigationBar appearance] setBarTintColor:RGBCOLOR(254, 254, 254)];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillChangeFrameNotification object:nil];
}

- (void)requstData
{
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=obparms&mobile=%@&bid=%@",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone], _boundId];
    _currentOrderReqeust = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    _currentOrderModel = [[CVAPIRequestModel alloc] init];
    _currentOrderModel.delegate = self;
    [_currentOrderModel sendRequest:_currentOrderReqeust completion:^(NSDictionary *result, NSError *error) {
        if (!error&&result) {
            _item = [TShareItem getItemFromDic:result];
            NSString *smallNumberText = [NSString stringWithFormat:@"%@ 张",[result objectForKey:@"bnum"]];
            NSMutableAttributedString *numberString = [[NSMutableAttributedString alloc] initWithString:smallNumberText];
            [numberString addAttribute:NSFontAttributeName
                                 value:[UIFont boldSystemFontOfSize:24]
                                 range:NSMakeRange(0, smallNumberText.length - 2)];
            [numberString addAttribute:NSForegroundColorAttributeName
                                 value:kColor(188.0, 18.0, 31.0)
                                 range:NSMakeRange(0, smallNumberText.length - 2)];
            self.smallNumberLabel.attributedText = numberString;
            NSString *smallPriceText = [NSString stringWithFormat:@"%@ 元",[result objectForKey:@"total"]];
            NSMutableAttributedString *priceString = [[NSMutableAttributedString alloc] initWithString:smallPriceText];
            [priceString addAttribute:NSFontAttributeName
                                 value:[UIFont boldSystemFontOfSize:24]
                                 range:NSMakeRange(0, smallPriceText.length - 2)];
            [priceString addAttribute:NSForegroundColorAttributeName
                                 value:kColor(188.0, 18.0, 31.0)
                                 range:NSMakeRange(0, smallPriceText.length - 2)];
            self.smallPriceLabel.attributedText = priceString;
            self.textView.text = [result objectForKey:@"description"];
            self.moneyLabel.text = [NSString stringWithFormat:@"￥%@",[result objectForKey:@"total"]];
        }
    }];
}

- (UILabel *)barLabel
{
    if (!_barLabel) {
        _barLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 210, 40)];
        _barLabel.backgroundColor = [UIColor clearColor];
        _barLabel.textAlignment = NSTextAlignmentCenter;
        _barLabel.text = @"停车券礼包";
        _barLabel.font = [UIFont systemFontOfSize:20];
        _barLabel.textColor = [UIColor whiteColor];
    }
    return _barLabel;
}

- (UIButton *)barLeftButton
{
    if (!_barLeftButton) {
        _barLeftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_barLeftButton setBackgroundColor:[UIColor clearColor]];
        [_barLeftButton setTitle:@"关闭" forState:UIControlStateNormal];
        [_barLeftButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_barLeftButton addTarget:self action:@selector(barLeftButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        _barLeftButton.frame = CGRectMake(0, 0, 40, 40);
        _barLeftButton.titleLabel.font = [UIFont systemFontOfSize:18];
    }
    return _barLeftButton;
}

- (void)barLeftButtonClick:(UIButton *)sender
{
    [self.navigationController dismissViewControllerAnimated:YES completion:nil];
}

- (UIButton *)barRightButton
{
    if (!_barRightButton) {
        _barRightButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_barRightButton setBackgroundColor:[UIColor clearColor]];
        [_barRightButton setBackgroundImage:[UIImage imageNamed:@"help.png"] forState:UIControlStateNormal];
        [_barRightButton addTarget:self action:@selector(barRightButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        _barRightButton.frame = CGRectMake(0, 0, 25, 25);
    }
    return _barRightButton;
}

- (void)barRightButtonClick:(UIButton *)sender
{
    TTicketHelpController* helper = [[TTicketHelpController alloc] initWithName:@"停车券帮助" url:[TAPIUtility getNetworkWithUrl:@"ticket.jsp"]];
    helper.redNavi = YES;
    [self.navigationController pushViewController:helper animated:YES];
}

- (UIScrollView *)scrollview
{
    if (!_scrollview) {
        _scrollview = [[UIScrollView alloc] initWithFrame:self.view.frame];
        _scrollview.contentSize = CGSizeMake(self.view.width, 504);
        _scrollview.backgroundColor = kColor(245.0, 245.0, 245.0);
        
        [_scrollview addSubview:self.numberView];
        [_scrollview addSubview:self.priceView];
        
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapTouchDown)];
        [_scrollview addGestureRecognizer:tap];
        [_scrollview addSubview:self.textView];
        [_scrollview addSubview:self.moneyLabel];
        [_scrollview addSubview:self.shareButton];
        [_scrollview addSubview:self.normalLabel];
    }
    return _scrollview;
}

- (void)tapTouchDown
{
    [self.textView resignFirstResponder];
}

- (UITextView *)textView
{
    if (!_textView) {
        _textView = [[UITextView alloc] initWithFrame:CGRectMake(10, 170, self.view.width - 20, 90)];
        _textView.delegate = self;
        _textView.backgroundColor = [UIColor whiteColor];
        _textView.text = @"祝您新年一路发发发!";
        _textView.textColor = kColor(155.0, 155.0, 155.0);
        _textView.font = [UIFont boldSystemFontOfSize:20];
        _textView.returnKeyType = UIReturnKeyDone;
        
        _textView.layer.masksToBounds = YES;
        _textView.layer.cornerRadius  = 5;
    }
    return _textView;
}

- (UIView *)numberView
{
    if (!_numberView) {
        _numberView = [[UIView alloc] initWithFrame:CGRectMake(0, 30, self.view.width, 50)];
        _numberView.backgroundColor = [UIColor whiteColor];
        
        [_numberView addSubview:self.numberLabel];
        [_numberView addSubview:self.smallNumberLabel];
    }
    return _numberView;
}

- (UILabel *)numberLabel
{
    if (!_numberLabel) {
        _numberLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.view.width/2, 50)];
        _numberLabel.backgroundColor = [UIColor clearColor];
        _numberLabel.textAlignment = NSTextAlignmentLeft;
        _numberLabel.text = @"停车券数量";
        _numberLabel.font = [UIFont boldSystemFontOfSize:20];
        _numberLabel.textColor = kColor(122.0, 122.0, 122.0);
    }
    return _numberLabel;
}

- (UILabel *)smallNumberLabel
{
    if (!_smallNumberLabel) {
        _smallNumberLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width/2, 0, self.view.width/2-10, 50)];
        _smallNumberLabel.backgroundColor = [UIColor clearColor];
        _smallNumberLabel.textAlignment = NSTextAlignmentRight;
        _smallNumberLabel.text = @"张";
        _smallNumberLabel.font = [UIFont boldSystemFontOfSize:20];
        _smallNumberLabel.textColor = kColor(122.0, 122.0, 122.0);
    }
    return _smallNumberLabel;
}

- (UIView *)priceView
{
    if (!_priceView) {
        _priceView = [[UIView alloc] initWithFrame:CGRectMake(0, 100, self.view.width, 50)];
        _priceView.backgroundColor = [UIColor whiteColor];
        
        [_priceView addSubview:self.priceLabel];
        [_priceView addSubview:self.pingImageView];
        [_priceView addSubview:self.smallPriceLabel];
    }
    return _priceView;
}

- (UILabel *)priceLabel
{
    if (!_priceLabel) {
        _priceLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.view.width/2, 50)];
        _priceLabel.backgroundColor = [UIColor clearColor];
        _priceLabel.textAlignment = NSTextAlignmentLeft;
        _priceLabel.text = @"总金额";
        _priceLabel.font = [UIFont boldSystemFontOfSize:20];
        _priceLabel.textColor = kColor(122.0, 122.0, 122.0);
    }
    return _priceLabel;
}

- (UILabel *)smallPriceLabel
{
    if (!_smallPriceLabel) {
        _smallPriceLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width/2, 0, self.view.width/2-10, 50)];
        _smallPriceLabel.backgroundColor = [UIColor clearColor];
        _smallPriceLabel.textAlignment = NSTextAlignmentRight;
        _smallPriceLabel.text = @"元";
        _smallPriceLabel.font = [UIFont boldSystemFontOfSize:20];
        _smallPriceLabel.textColor = kColor(122.0, 122.0, 122.0);
    }
    return _smallPriceLabel;
}

- (UIImageView *)pingImageView
{
    if (!_pingImageView) {
        _pingImageView = [[UIImageView alloc] initWithFrame:CGRectMake(75, 12, 26, 26)];
        _pingImageView.image = [UIImage imageNamed:@"dialog_ping.png"];
    }
    return _pingImageView;
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text{
    if ([text isEqualToString:@"\n"]){
        [textView resignFirstResponder];
        return NO;
    }
    return YES;
}

- (UILabel *)moneyLabel
{
    if (!_moneyLabel) {
        _moneyLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 280, self.view.width, 80)];
        _moneyLabel.backgroundColor = [UIColor clearColor];
        _moneyLabel.textAlignment = NSTextAlignmentCenter;
        _moneyLabel.text = @"￥0.0";
        _moneyLabel.font = [UIFont boldSystemFontOfSize:50];
        _moneyLabel.textColor = [UIColor blackColor];
    }
    return _moneyLabel;
}

- (UIButton *)shareButton
{
    if (!_shareButton) {
        _shareButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_shareButton setBackgroundColor:kColor(188.0, 18.0, 31.0)];
        [_shareButton setTitle:@"发给好友" forState:UIControlStateNormal];
        [_shareButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_shareButton addTarget:self action:@selector(shareButtonClick:) forControlEvents:UIControlEventTouchUpInside];
        _shareButton.frame = CGRectMake(10, 370, self.view.width-20, 50);
        _shareButton.titleLabel.font = [UIFont boldSystemFontOfSize:20];
        
        _shareButton.layer.masksToBounds = YES;
        _shareButton.layer.cornerRadius  = 5;
    }
    return _shareButton;
}

- (void)shareButtonClick:(UIButton *)sender
{
     _item.descri = [NSString stringWithFormat:@"%@",self.textView.text];
    _item.url = [NSString stringWithFormat:@"%@&words=%@",_item.url,[[_item.descri stringByUrlEncoding] stringByUrlEncoding]];
    [self.navigationController dismissViewControllerAnimated:YES completion:^{
        [self shareToWeixin:_item];
    }];
}

- (void)shareToWeixin:(TShareItem*)item {
    [[SDWebImageDownloader sharedDownloader] downloadImageWithURL:[NSURL URLWithString:[TAPIUtility getNetworkWithUrl:item.imgurl]] options:0 progress:nil completed:^(UIImage *image, NSData *data, NSError *error, BOOL finished) {
        if (image) {
            
            WXWebpageObject* webObj = [WXWebpageObject object];
            webObj.webpageUrl = [TAPIUtility getNetworkWithUrl:[NSString stringWithFormat:@"%@&id=%@", item.url, _boundId]];
            NSLog(@"weburl----%@", webObj.webpageUrl);
            
            WXMediaMessage* message = [WXMediaMessage message];
            message.title = item.title;
            message.description = item.descri;
            message.mediaObject = webObj;
            UIImage* thumbImg = [TAPIUtility ajustImage:image size:CGSizeMake(30, 30)];
            message.thumbData = UIImageJPEGRepresentation(thumbImg, 1);
            
            SendMessageToWXReq* req = [[SendMessageToWXReq alloc] init];
            req.scene = WXSceneSession;
            req.bText = NO;
            req.message = message;
            [WXApi sendReq:req];
        } else {
            NSLog(@"+++++++NO image");
        }
    }];
}

- (UILabel *)normalLabel
{
    if (!_normalLabel) {
        _normalLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 460, 320, 30)];
        _normalLabel.backgroundColor = [UIColor clearColor];
        _normalLabel.textAlignment = NSTextAlignmentCenter;
        _normalLabel.text = @"对方可领取的红包金额为0.01～200元";
        _normalLabel.font = [UIFont boldSystemFontOfSize:15];
        _normalLabel.textColor = kColor(155.0, 155.0, 155.0);
    }
    return _normalLabel;
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
    CGRect rect = self.textView.frame;
    CGFloat height = rect.origin.y + rect.size.height - finalKeyboardFrame.origin.y;
    
    if (height < 0)
        height = 0;
    _scrollview.top = -height;
    
    [UIView commitAnimations];
    
}

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
