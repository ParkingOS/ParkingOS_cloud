//
//  TRechargeViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-13.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TRechargeViewController.h"
#import "TAPIUtility.h"
#import "TRechargeWaysViewController.h"
#import "UIKeyboardViewController.h"
#import "IQKeyboardManager.h"

#define padding 20
#define image_width 15


@interface TRechargeViewController ()<UIScrollViewDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UILabel* rechargeLabel;
@property(nonatomic, retain) UITextField* rechargeTextField;
//@property(nonatomic, retain) UILabel* unit;
@property(nonatomic, retain) UIImageView* selectedImage;
@property(nonatomic, retain) UILabel* redPackageLabel;
@property(nonatomic, retain) UIButton* rechargeButton;

@property(nonatomic, retain) NSArray* options;
@property(nonatomic, retain) NSMutableArray* optionsButtons;

@property(nonatomic, retain) UIKeyboardViewController* keyboardViewController;

@end

@implementation TRechargeViewController

- (id)init {
    if (self = [super init]) {
        _lackMoney = @"0";
        
        _options = @[@"30", @"50", @"100", @"200"];
        _optionsButtons = [NSMutableArray array];
        
        _scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _scrollView.contentSize = CGSizeMake(self.view.width, self.view.height);
        _scrollView.delegate = self;
        
        _rechargeLabel  = [[UILabel alloc] initWithFrame:CGRectMake(padding, padding, 200, 30)];
        _rechargeLabel.backgroundColor = [UIColor clearColor];
        _rechargeLabel.text = @"请输入充值金额";
        
        _rechargeTextField = [[UITextField alloc] initWithFrame:CGRectMake(padding, _rechargeLabel.bottom + 10, self.view.width - 2*padding, 40)];
        _rechargeTextField.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
        _rechargeTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _rechargeTextField.placeholder = @"充值金额";
        _rechargeTextField.borderStyle = UITextBorderStyleRoundedRect;
        _rechargeTextField.keyboardType = UIKeyboardTypeDecimalPad;
        
//        _unit  = [[UILabel alloc] initWithFrame:CGRectMake(_rechargeTextField.right, _rechargeTextField.top, 40, 40)];
//        _unit.backgroundColor = [UIColor clearColor];
//        _unit.text = @"元";
        
        //创建4个选择按钮
        [self createOptionsButton];
        
        _redPackageLabel  = [[UILabel alloc] initWithFrame:CGRectMake(0, _rechargeTextField.bottom + 70, self.view.width, 30)];
        _redPackageLabel.backgroundColor = [UIColor clearColor];
        NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:@"充一百，送充值礼包，认证用户专享" attributes:@{NSForegroundColorAttributeName : green_color, NSFontAttributeName : [UIFont systemFontOfSize:14]}];
        _redPackageLabel.attributedText = attr;
        _redPackageLabel.textAlignment = NSTextAlignmentCenter;
        
        _selectedImage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rechage_selected.png"]];
        _selectedImage.frame = CGRectZero;
        
        _rechargeButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_rechargeButton setTitle:@"去支付" forState:UIControlStateNormal];
        [_rechargeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_rechargeButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _rechargeButton.frame = CGRectMake(padding, _redPackageLabel.bottom + 20, self.view.width - 2*padding, 40);
        _rechargeButton.layer.cornerRadius = 5;
        _rechargeButton.clipsToBounds = YES;
        [_rechargeButton addTarget:self action:@selector(rechargeButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [_scrollView addSubview:_rechargeLabel];
        [_scrollView addSubview:_rechargeTextField];
        [_scrollView addSubview:_redPackageLabel];
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
    self.titleView.text = @"充值";
}

- (void)viewWillAppear:(BOOL)animated {
    
    //键盘弹出的视图
    _keyboardViewController = [[UIKeyboardViewController alloc] initWithControllerDelegate:self];
    [_keyboardViewController addToolbarToKeyboard];
    [super viewWillAppear:animated];
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
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear: animated];
    
    [_rechargeTextField endEditing:YES];
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
        _rechargeTextField.text = [_options objectAtIndex:button.tag - 100];
        _selectedImage.frame = CGRectMake(button.right - image_width + 3, button.bottom - image_width + 3, image_width, image_width);
    } else {
        //4个都没选中
        _selectedImage.frame = CGRectZero;
    }
    
    //画按钮边框
    for (UIButton* object in _optionsButtons) {
        if (button == object) {
            [object setTitleColor:green_color forState:UIControlStateNormal];
            object.layer.borderColor = green_color.CGColor;
        } else {
            [object setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
            object.layer.borderColor = RGBCOLOR(215, 215, 215).CGColor;
        }
    }
    
    //100元和非100元 UI显示不同
    [self updateRedpackageLabelUI];
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
        [TAPIUtility alertMessage:@"请输入充值金额" success:NO toViewController:nil];
    } else if (![TAPIUtility isValidOfMoneyNumber:_rechargeTextField.text]) {
        [TAPIUtility alertMessage:@"金额格式错误" success:NO toViewController:nil];
    } else if ([_rechargeTextField.text doubleValue] > 500) {
        [TAPIUtility alertMessage:@"单次充值不可超过500" success:NO toViewController:nil];
        _rechargeTextField.text = @"500";
    }else if ([_rechargeTextField.text doubleValue] < [_lackMoney doubleValue]) {
        NSString* money = [TAPIUtility clearDoubleZero:[_lackMoney doubleValue] fractionCount:2];
        [TAPIUtility alertMessage:[NSString stringWithFormat:@"充值不得低于信用欠费:%@元", money] success:NO toViewController:nil];
    }else {
        [_rechargeTextField endEditing:YES];
        
        TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
        vc.name = @"停车宝帐户充值";
        vc.price = [self fullFormatPrice:_rechargeTextField.text];
        vc.rechargeMode = RechargeMode_addMoney;
        [self.navigationController pushViewController:vc animated:YES];
    }
    
}

- (void)updateRedpackageLabelUI {
    if ([_rechargeTextField.text doubleValue] == 100) {
        NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:@"充一百，送充值礼包，认证用户专享" attributes:@{NSForegroundColorAttributeName : green_color, NSFontAttributeName : [UIFont systemFontOfSize:14]}];
        _redPackageLabel.attributedText = attr;
    } else {
        NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:@"充一百，送充值礼包，其他金额不送" attributes:@{NSForegroundColorAttributeName : green_color, NSFontAttributeName : [UIFont systemFontOfSize:14]}];
        [attr addAttributes:@{NSForegroundColorAttributeName : red_color} range:NSMakeRange(10, 6)];
        _redPackageLabel.attributedText = attr;
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
