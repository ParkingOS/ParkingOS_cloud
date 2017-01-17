//
//  TBuyTicketViewController.m
//  
//
//  Created by apple on 15/8/26.
//
//

#import "TBuyTicketViewController.h"
#import "IQKeyboardManager.h"
#import "TRechargeWaysViewController.h"
#import "TBuyTicketItem.h"
#import "CVAPIRequestModel.h"

#define padding 10

@interface TBuyTicketViewController()

@property(nonatomic, retain) UITextField* moneyTextField;
@property(nonatomic, retain) UITextField* numberTextField;
@property(nonatomic, retain) UILabel* ticketLabel;
@property(nonatomic, retain) UILabel* moneyLabel;
@property(nonatomic, retain) UIButton* buyTicketButton;

@property(nonatomic, retain) TBuyTicketItem* item;
@property(nonatomic, retain) NSString* price;

@property(nonatomic, retain) CVAPIRequest* request;

@end
@implementation TBuyTicketViewController

- (id)init {
    if (self = [super init]) {
        _moneyTextField = [[UITextField alloc] initWithFrame:CGRectMake(padding, 20, self.view.width - 2*padding, 40)];
        _moneyTextField.keyboardType = UIKeyboardTypeNumberPad;
        _moneyTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _moneyTextField.placeholder = @"最高20";
        _moneyTextField.textColor = green_color;
        _moneyTextField.borderStyle = UITextBorderStyleRoundedRect;
        _moneyTextField.textAlignment = NSTextAlignmentRight;
        UILabel* leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 55, 40)];
        leftLabel.text = @"  金额: ";
        _moneyTextField.leftView = leftLabel;
        _moneyTextField.leftViewMode = UITextFieldViewModeAlways;
        UILabel* rightLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 25, 40)];
        rightLabel.text = @"元";
        _moneyTextField.rightView = rightLabel;
        _moneyTextField.rightViewMode = UITextFieldViewModeAlways;
        
        
        _numberTextField = [[UITextField alloc] initWithFrame:CGRectMake(padding, _moneyTextField.bottom + 20, self.view.width - 2*padding, 40)];
        _numberTextField.keyboardType = UIKeyboardTypeNumberPad;
        _numberTextField.clearButtonMode = UITextFieldViewModeWhileEditing;
        _numberTextField.placeholder = @"0";
        _numberTextField.textColor = green_color;
        _numberTextField.borderStyle = UITextBorderStyleRoundedRect;
        _numberTextField.textAlignment = NSTextAlignmentRight;
        leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 50, 40)];
        leftLabel.text = @"  数量: ";
        _numberTextField.leftView = leftLabel;
        _numberTextField.leftViewMode = UITextFieldViewModeAlways;
        rightLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 25, 40)];
        rightLabel.text = @"张";
        _numberTextField.rightView = rightLabel;
        _numberTextField.rightViewMode = UITextFieldViewModeAlways;
        
        
        _ticketLabel = [[UILabel alloc] initWithFrame:CGRectMake(padding, _numberTextField.bottom + 20, self.view.width - 2*padding, 30)];
        _ticketLabel.textColor = [UIColor grayColor];
        _ticketLabel.text = @"末认证9折,认证后7折";
        _ticketLabel.font = [UIFont systemFontOfSize:17];
        _ticketLabel.textAlignment = NSTextAlignmentCenter;
        
        _moneyLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, _ticketLabel.bottom + 10, self.view.width, 60)];
        _moneyLabel.textAlignment = NSTextAlignmentCenter;
        NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:@"¥0   ¥6" attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:22]}];
        [attr addAttributes:@{NSForegroundColorAttributeName: green_color} range:NSMakeRange(0, 2)];
        [attr addAttributes:@{NSForegroundColorAttributeName: gray_color, NSStrikethroughStyleAttributeName : @(NSUnderlineStyleSingle)} range:NSMakeRange(5, 2)];
        [attr addAttributes:@{NSFontAttributeName: [UIFont systemFontOfSize:40]} range:NSMakeRange(1, 1)];
        _moneyLabel.attributedText = attr;
        
        _buyTicketButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_buyTicketButton setTitle:@"购买停车券" forState:UIControlStateNormal];
        [_buyTicketButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _buyTicketButton.layer.cornerRadius = 5;
        _buyTicketButton.clipsToBounds = YES;
        [_buyTicketButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _buyTicketButton.frame = CGRectMake(10, _moneyLabel.bottom + 20, self.view.width - 2*10, 40);
        [_buyTicketButton addTarget:self action:@selector(buyTicketButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        [self.view addSubview:_moneyTextField];
        [self.view addSubview:_numberTextField];
        [self.view addSubview:_ticketLabel];
        [self.view addSubview:_moneyLabel];
        [self.view addSubview:_buyTicketButton];
        
        for (UIView*view in self.view.subviews) {
            view.hidden = YES;
        }
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = [UIColor colorWithRed:236.0/255.0 green:236.0/255.0 blue:236.0/255.0 alpha:1];
    self.titleView.text = @"购买停车券";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self requestAccountInfo];
    
    [[IQKeyboardManager sharedManager] setEnable:YES];
    [[IQKeyboardManager sharedManager] setEnableAutoToolbar:YES];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(textViewDidChange:) name:UITextFieldTextDidChangeNotification object:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[IQKeyboardManager sharedManager] setEnable:NO];
    [[IQKeyboardManager sharedManager] setEnableAutoToolbar:NO];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [_request cancel];
}

#pragma mark request

- (void)requestAccountInfo {
    NSString* apiPath = [NSString stringWithFormat:@"carinter.do?action=prebuyticket&mobile=%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result || [result count] == 0)
            return;
        _item = [TBuyTicketItem getItemFromDic:result];
        
        //显示UI
        for (UIView*view in self.view.subviews) {
            view.hidden = NO;
        }
        
        //更新ticketLabel
        if ([_item.auth doubleValue] == 10) {
            _ticketLabel.hidden = YES;
            
        } else if ([_item.auth doubleValue] == [_item.notauth doubleValue]) {
            _ticketLabel.text = [NSString stringWithFormat:@"享受%@折优惠", _item.auth];
            
        } else if ([_item.isauth isEqualToString:@"1"]) {
            _ticketLabel.text = [NSString stringWithFormat:@"认证用户享受%@折优惠", _item.auth];
            
        } else if ([_item.isauth isEqualToString:@"0"]) {
            if ([_item.auth doubleValue] == 10) {
                _ticketLabel.text = [NSString stringWithFormat:@"无认证优惠，认证后%@折", _item.auth];
                
            } else {
                _ticketLabel.text = [NSString stringWithFormat:@"未认证%@折，认证后%@折", _item.notauth, _item.auth];
            }
        }
        
        //更新moneyLabel
        [self updateMoneyLabel];
        
    }];
 }

#pragma mark private

- (void)textViewDidChange:(NSNotification*) notif {
    if (notif.object == _moneyTextField || notif.object == _numberTextField) {
        //最终金额跟随输入变化
        [self updateMoneyLabel];
    }
}

- (void)updateMoneyLabel {
    NSString* price1 = [TAPIUtility clearDoubleZero:[_moneyTextField.text doubleValue] * [_numberTextField.text doubleValue] fractionCount:2];
    NSString* price2 = [TAPIUtility clearDoubleZero:[price1 doubleValue] * ([_item.isauth isEqualToString:@"1"] ? [_item.auth doubleValue] : [_item.notauth doubleValue])/10 fractionCount:2];
    
    NSMutableAttributedString* attr = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"¥%@   ¥%@", price2, price1] attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:22]}];
    [attr addAttributes:@{NSForegroundColorAttributeName: green_color} range:NSMakeRange(0, price2.length + 1)];
    [attr addAttributes:@{NSForegroundColorAttributeName: gray_color, NSStrikethroughStyleAttributeName : @(NSUnderlineStyleSingle)} range:NSMakeRange(price2.length + 4, price1.length + 1)];
    [attr addAttributes:@{NSFontAttributeName: [UIFont systemFontOfSize:40]} range:NSMakeRange(1, price2.length)];
    _moneyLabel.attributedText = attr;
    
    _price = price2;
}

- (void)buyTicketButtonTouched {
    if (![TAPIUtility isValidOfMoneyNumber:_moneyTextField.text] || ![TAPIUtility isValidOfMoneyNumber:_numberTextField.text]) {
        [TAPIUtility alertMessage:@"金额格式错误" success:NO toViewController:nil];
        return;
    } else if ([self isHasDecimals:_moneyTextField.text] || [self isHasDecimals:_numberTextField.text]) {
        [TAPIUtility alertMessage:@"停车券金额或数量 必须为整数" success:NO toViewController:nil];
        return;
    } else if ([_item.isauth isEqualToString:@"0"] && [_moneyTextField.text doubleValue] > 1.0) {
        [TAPIUtility alertMessage:@"未认证用户最大只能买1元券" success:NO toViewController:nil];
        _moneyTextField.text = @"1";
        [self updateMoneyLabel];
        return;
    } else if ([_moneyTextField.text doubleValue] > 20) {
        [TAPIUtility alertMessage:@"停车券金额最高为20元" success:NO toViewController:nil];
        _moneyTextField.text = @"20";
        [self updateMoneyLabel];
        return;
    } else if ([_numberTextField.text doubleValue] > 99) {
        [TAPIUtility alertMessage:@"一次最多购买99张停车券" success:NO toViewController:nil];
        _numberTextField.text = @"99";
        [self updateMoneyLabel];
        return;
    }
    
    TRechargeWaysViewController* vc = [[TRechargeWaysViewController alloc] init];
    vc.rechargeMode = RechargeMode_buyTicket;
    vc.name = [NSString stringWithFormat:@"购买%@张%@元停车券", _numberTextField.text, _moneyTextField.text];
    vc.price = _price;
    vc.buyTicketMoney = _moneyTextField.text;
    vc.buyTicketNumber = _numberTextField.text;
    [self.navigationController pushViewController:vc animated:YES];
}

- (BOOL)isHasDecimals:(NSString*)money {
    NSRange range = [money rangeOfString:@"."];
    if (range.length > 0) {
        NSString* suffix = [money substringFromIndex:range.location + range.length];
        if ([suffix doubleValue] == 0) {
            return NO;
        }
    } else {
        return NO;
    }
    
    return YES;
}

@end
