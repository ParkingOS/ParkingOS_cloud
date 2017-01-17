//
//  TPayCollectorViewController.m
//  TingCheBao_user
//
//  Created by apple on 14/12/12.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TPayCollectorViewController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TCollectorItem.h"
#import "TRechargeWaysViewController.h"
#import "TCollectorDetailViewController.h"
#import "UIKeyboardViewController.h"

@interface TPayCollectorViewController ()

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UIImageView* photoImgView;
@property(nonatomic, retain) UILabel* colloctorNameLabel;
@property(nonatomic, retain) UILabel* parkNameLabel;
@property(nonatomic, retain) UIButton* detailButton;

@property(nonatomic, retain) UIView* whiteView;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UITextField* moneyTextField;
@property(nonatomic, retain) UIButton* payButton;

@property(nonatomic, retain) UIKeyboardViewController* keyboardViewController;

@end

@implementation TPayCollectorViewController

- (id)init{
    if (self = [super init]) {
        _scrollView = [[UIScrollView alloc] initWithFrame:self.view.frame];
        _scrollView.contentSize = self.view.frame.size;
        
        _photoImgView = [[UIImageView alloc] initWithFrame:CGRectMake((self.view.width - 80)/2, 20, 80, 80)];
        _photoImgView.image = [UIImage imageNamed:@"collector.png"];
        
        CGFloat detailWidth = 60;
        CGFloat payButtonWidth = 60;
        
        _colloctorNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, _photoImgView.bottom + 5, self.view.width - detailWidth - 2*10, 30)];
        _colloctorNameLabel.text = @"";
        _colloctorNameLabel.textAlignment = NSTextAlignmentLeft;
        _colloctorNameLabel.font = [UIFont systemFontOfSize:17];
        
        _parkNameLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, _colloctorNameLabel.bottom, _colloctorNameLabel.width, 30)];
        _parkNameLabel.text = @"";
        _parkNameLabel.font = [UIFont systemFontOfSize:12];
        _parkNameLabel.textAlignment = NSTextAlignmentLeft;
        
        _detailButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _detailButton.frame = CGRectMake(self.view.width - 10 - detailWidth, _colloctorNameLabel.top, detailWidth, 40);
        UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, detailWidth - 6, 40)];
        label.text = @"查看详情";
        label.textColor = green_color;
        label.font = [UIFont systemFontOfSize:13];
        UIImageView* imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"arrow_right_green.png"]];
        imgView.frame = CGRectMake(label.right, 14, 6, 12);
        [_detailButton addSubview:label];
        [_detailButton addSubview:imgView];
        [_detailButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        _whiteView = [[UIView alloc] initWithFrame:CGRectMake(0, _parkNameLabel.bottom + 20, self.view.width, 50)];
        _whiteView.backgroundColor = [UIColor whiteColor];
        
        _moneyLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 10, 70, 30)];
        _moneyLabel.text = @"金额(元):";
        _moneyLabel.font = [UIFont systemFontOfSize:17];
        
        _moneyTextField = [[UITextField alloc] initWithFrame:CGRectMake(_moneyLabel.right + 4, _moneyLabel.top, self.view.width - 70 - 4  - payButtonWidth - 2*10, 30)];
        _moneyTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _moneyTextField.keyboardType = UIKeyboardTypeDecimalPad;
        
        _payButton = [UIButton buttonWithType:UIButtonTypeCustom];
        _payButton.titleLabel.font = [UIFont systemFontOfSize:13];
        [_payButton setTitle:@"去付款" forState:UIControlStateNormal];
        [_payButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_payButton setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
        [_payButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _payButton.layer.cornerRadius = 5;
        _payButton.clipsToBounds = YES;
        _payButton.frame = CGRectMake(_moneyTextField.right, ((_whiteView.height) - 30)/2, payButtonWidth, 30);
        [_payButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [_whiteView addSubview:_moneyLabel];
        [_whiteView addSubview:_moneyTextField];
        [_whiteView addSubview:_payButton];
        
        [_scrollView addSubview:_photoImgView];
        [_scrollView addSubview:_colloctorNameLabel];
        [_scrollView addSubview:_parkNameLabel];
        [_scrollView addSubview:_detailButton];
        [_scrollView addSubview:_whiteView];
        
        [self.view addSubview:_scrollView];
        
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(236, 236, 236);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = @"向收费员付款";
    [self initInfo];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    //键盘弹出的视图
    _keyboardViewController = [[UIKeyboardViewController alloc] initWithControllerDelegate:self];
    [_keyboardViewController addToolbarToKeyboard];
    [_moneyTextField becomeFirstResponder];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_moneyTextField endEditing:YES];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [_moneyTextField endEditing:YES];
}

- (void)initInfo {
    _colloctorNameLabel.text = [NSString stringWithFormat:@"%@(%@)", _collectorName, _collectorId];
    _parkNameLabel.text = _parkName;
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _payButton) {
        //去支付
        if (![TAPIUtility isValidOfMoneyNumber:_moneyTextField.text]) {
            [TAPIUtility alertMessage:@"金额格式不正确" success:NO toViewController:self];
            return;
        }
       
        TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
        vc.collectorId = _collectorId;
        vc.name = [NSString stringWithFormat:@"%@:%@", _collectorName, _collectorId];
        vc.price = _moneyTextField.text;
        vc.rechargeMode = RechargeMode_collector;
        
        [self.navigationController pushViewController:vc animated:YES];
        
    } else if (button == _detailButton) {
        //收费员详情
        TCollectorDetailViewController* vc = [[TCollectorDetailViewController alloc] init];
        vc.collectorName = _collectorName;
        vc.colletorId = _collectorId;
        vc.parkName = _parkName;
        [self.navigationController pushViewController:vc animated:YES];
        
    }
}


@end
