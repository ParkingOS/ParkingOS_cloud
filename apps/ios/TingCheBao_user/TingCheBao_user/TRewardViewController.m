//
//  TRechargeViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRewardViewController.h"
#import "TAPIUtility.h"
#import "TRechargeWaysViewController.h"
#import "CVAPIRequestModel.h"

#define padding 20
#define image_width 15


@interface TRewardViewController ()<UIScrollViewDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UILabel* rechargeLabel;
@property(nonatomic, retain) UITextField* rechargeTextField;
//@property(nonatomic, retain) UILabel* unit;
@property(nonatomic, retain) UIImageView* selectedImage;
@property(nonatomic, retain) UIButton* rechargeButton;

@property(nonatomic, retain) NSArray* options;
@property(nonatomic, retain) NSMutableArray* optionsButtons;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) NSString* mostTicketReward;

@end

@implementation TRewardViewController

- (id)init {
    if (self = [super init]) {
        _options = @[@"1", @"2", @"3", @"4"];
        _optionsButtons = [NSMutableArray array];
        _mostTicketReward = @"2";
        
        _scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _scrollView.contentSize = CGSizeMake(self.view.width, self.view.height);
        _scrollView.delegate = self;
        
        _rechargeLabel  = [[UILabel alloc] initWithFrame:CGRectMake(padding, padding, self.view.width, 30)];
        _rechargeLabel.backgroundColor = [UIColor clearColor];
        _rechargeLabel.text = [NSString stringWithFormat:@"对该收费员停车券最多抵%@元打赏", _mostTicketReward];
        
        _rechargeTextField = [[UITextField alloc] initWithFrame:CGRectMake(padding, _rechargeLabel.bottom + 10, self.view.width - 2*padding, 40)];
        _rechargeTextField.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
        _rechargeTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _rechargeTextField.placeholder = @"打赏金额(元)";
        _rechargeTextField.borderStyle = UITextBorderStyleRoundedRect;
        _rechargeTextField.keyboardType = UIKeyboardTypeDecimalPad;
        
        //        _unit  = [[UILabel alloc] initWithFrame:CGRectMake(_rechargeTextField.right, _rechargeTextField.top, 40, 40)];
        //        _unit.backgroundColor = [UIColor clearColor];
        //        _unit.text = @"元";
        
        //创建4个选择按钮
        [self createOptionsButton];
        
        _selectedImage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rechage_selected.png"]];
        _selectedImage.frame = CGRectZero;
        
        _rechargeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_rechargeButton setTitle:@"去打赏" forState:UIControlStateNormal];
        [_rechargeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_rechargeButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _rechargeButton.frame = CGRectMake(padding, _rechargeTextField.bottom + 60, self.view.width - 2*padding, 40);
        _rechargeButton.layer.cornerRadius = 5;
        _rechargeButton.clipsToBounds = YES;
        [_rechargeButton addTarget:self action:@selector(rechargeButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [_scrollView addSubview:_rechargeLabel];
        [_scrollView addSubview:_rechargeTextField];
        [_scrollView addSubview:_selectedImage];
        //        [_scrollView addSubview:_unit];
        [_scrollView addSubview:_rechargeButton];
        
        [self.view addSubview:_scrollView];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = light_white_color;
    self.titleView.text = @"打赏";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    //请求打折数
    [self requestInfo];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [_rechargeTextField becomeFirstResponder];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(textViewDidChange:) name:UITextFieldTextDidChangeNotification object:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_rechargeTextField endEditing:YES];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [_request cancel];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear: animated];
    [_rechargeTextField endEditing:YES];
}


#pragma request
//请求网络
- (void)requestInfo {
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=getrewardquota&pid=%@", _collectorId];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    self.model.hideNetworkView = YES;
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        NSString* ticket = [result objectForKey:@"info"];
        //清除末尾的0
        ticket = [TAPIUtility clearDoubleZero:[ticket doubleValue] fractionCount:2];
        _mostTicketReward = ticket;
        _rechargeLabel.text = [NSString stringWithFormat:@"对该收费员停车券最多抵%@元打赏", _mostTicketReward];
    }];
}

#pragma mark private

- (void)createOptionsButton {
    CGFloat pad = 20;
    CGFloat button_width = (self.view.width - 2*padding - 3*pad)/4;
    for (int i = 0; i < 4; i++) {
        UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button setTitle:[_options objectAtIndex:i] forState:UIControlStateNormal];
        [button setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        [button setBackgroundImage:[TAPIUtility imageWithColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        button.tag = 100 + i;
        //        button.layer.borderColor = [UIColor grayColor].CGColor;
        button.layer.borderColor = RGBCOLOR(215, 215, 215).CGColor;
        button.layer.borderWidth = 1;
        button.layer.cornerRadius = 5;
        button.clipsToBounds = YES;
        [button addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        button.frame = CGRectMake(padding + (button_width + pad) * i, _rechargeTextField.bottom + 10, button_width, 40);
        
        [_optionsButtons addObject:button];
        
        [_scrollView addSubview:button];
    }
}

- (void)buttonTouched:(UIButton*)button {
    if (button != nil) {
        //4个都没选中
        _rechargeTextField.text = [_options objectAtIndex:button.tag - 100];
        _selectedImage.frame = CGRectMake(button.right - image_width + 3, button.bottom - image_width + 3, image_width, image_width);
    } else {
        _selectedImage.frame = CGRectZero;
    }
    
    for (UIButton* object in _optionsButtons) {
        if (button == object) {
            [object setTitleColor:green_color forState:UIControlStateNormal];
            object.layer.borderColor = green_color.CGColor;
        } else {
            [object setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
            object.layer.borderColor = RGBCOLOR(215, 215, 215).CGColor;
        }
    }
}

- (void)textViewDidChange:(NSNotification*)noti {
    if (noti.object == _rechargeTextField) {
        if ([_options containsObject:_rechargeTextField.text]) {
            UIButton* butotn = [_optionsButtons objectAtIndex:[_options indexOfObject:_rechargeTextField.text]];
            [self buttonTouched:butotn];
        } else {
            [self buttonTouched:nil];
        }
    }
}
- (void)rechargeButtonTouched:(UIButton*)button {
    if ([_rechargeTextField.text isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"请输入打赏金额" success:NO toViewController:self];
    } else if (![TAPIUtility isValidOfMoneyNumber:_rechargeTextField.text]) {
        [TAPIUtility alertMessage:@"金额格式错误" success:NO toViewController:self];
    }else {
        [_rechargeTextField endEditing:YES];
        
        TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
        vc.collectorId = _collectorId;
        vc.name = @"打赏收费员";
        vc.price = [self fullFormatPrice:_rechargeTextField.text];
        vc.rechargeMode = RechargeMode_collector;
        vc.isReward = YES;
        vc.orderId = _orderId;
        [self.navigationController pushViewController:vc animated:YES];
    }
    
}

- (NSString*)fullFormatPrice:(NSString*) price{
    NSRange range = [price rangeOfString:@"."];
    if (range.length > 0) {
        if (range.location == price.length - 2) {
            return [price stringByAppendingString:@"0"];
        }
    } else {
        return [price stringByAppendingString:@".00"];
    }
    return price;
}

@end
