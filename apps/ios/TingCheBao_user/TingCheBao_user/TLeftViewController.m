//
//  TLeftViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-8-19.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TViewController.h"
#import "TLeftViewController.h"
#import "TLeftMenuCell.h"
#import "TSignUpViewController.h"

#import "THomeViewController.h"
#import "TSettingViewController.h"
#import "TAccountViewController.h"
#import "THistoryOrderViewController.h"
#import "TRecommendUserViewController.h"
#import "UMFeedback.h"
#import "TAPIUtility.h"
#import "TNavigationController.h"
#import "TLoginViewController.h"
#import "TPhoneListController.h"

@interface TLeftViewController ()<UITableViewDataSource, UITableViewDelegate, UIAlertViewDelegate>

@property(nonatomic, retain) UIButton* topView;
@property(nonatomic, retain) UIImageView* photoImageView;
@property(nonatomic, retain) UILabel* loginLabel;
@property(nonatomic, retain) UIImageView* carNumberStatusView;
@property(nonatomic, retain) UIActivityIndicatorView* numberActivityIndicator;
@property(nonatomic, retain) UILabel* phoneLabel;
@property(nonatomic, retain) UILabel* detailLabel;
@property(nonatomic, retain) UIImageView* rightArrowImageView;
@property(nonatomic, retain) UIView* lineView;

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) NSMutableArray* rows;
@property(nonatomic, retain) NSArray* images;

@property(nonatomic, retain) UIImageView* logoImageVIew;
@property(nonatomic, retain) UIButton* phoneButton;

@property(nonatomic, retain) UISegmentedControl* serverSegment;

@end

@implementation TLeftViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = RGBCOLOR(245, 246, 245);
    
    //-----topview
    _topView = [[UIButton alloc] initWithFrame:CGRectMake(0, 40, self.view.width, 80)];
    [_topView setBackgroundImage:[TAPIUtility imageWithColor:self.view.backgroundColor] forState:UIControlStateNormal];
    [_topView setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(208, 208, 208)] forState:UIControlStateHighlighted];
    [_topView addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
    
    _photoImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"left_user.png"]];
    _photoImageView.frame = CGRectMake(20, 0, 60, 60);
    
    _loginLabel = [[UILabel alloc] initWithFrame:CGRectMake(_photoImageView.right + 10, _photoImageView.top, 100, 60)];
    _loginLabel.text = @"点击登录";
    _loginLabel.textColor = [UIColor grayColor];
    
    _carNumberStatusView = [[UIImageView alloc] initWithFrame:CGRectMake(_photoImageView.right + 10, 7, 45, 20)];
    
    _numberActivityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    _numberActivityIndicator.center = _carNumberStatusView.center;
    
    _phoneLabel = [[UILabel alloc] initWithFrame:CGRectMake(_photoImageView.right + 10, _carNumberStatusView.bottom + 7, 90, 20)];
    _phoneLabel.textColor = [UIColor grayColor];
    _phoneLabel.font = [UIFont systemFontOfSize:14];
    _phoneLabel.text = @"";
    
    _detailLabel = [[UILabel alloc] initWithFrame:CGRectMake(_phoneLabel.right + 20, 20, 60, 20)];
    _detailLabel.text = @"余额、券";
    _detailLabel.textColor = RGBCOLOR(180, 180, 180);
    _detailLabel.font = [UIFont systemFontOfSize:14];
    
    _rightArrowImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"right_arrow_light_grey.png"]];
    _rightArrowImageView.frame = CGRectMake(_detailLabel.right, 23, 9, 15);
    
    _lineView = [[UIView alloc] initWithFrame:CGRectMake(_photoImageView.left, _topView.height - 1, _topView.width, 1)];
    _lineView.backgroundColor = RGBCOLOR(222, 222, 222);
    
    [_topView addSubview:_photoImageView];
    [_topView addSubview:_loginLabel];
    [_topView addSubview:_carNumberStatusView];
    [_topView addSubview:_numberActivityIndicator];
    [_topView addSubview:_phoneLabel];
    [_topView addSubview:_detailLabel];
    [_topView addSubview:_rightArrowImageView];
    [_topView addSubview:_lineView];
    
    //-----topview------end
    
    _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, _topView.bottom, self.view.width, self.view.height - 80)];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    _tableView.rowHeight = 52;
    _tableView.backgroundColor = RGBCOLOR(244, 244, 244);//343942
    _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    _tableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 0)];
    
//    _logoImageVIew = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"left_phone2.png"]];
//    _logoImageVIew.frame = CGRectMake(10, self.view.height - 33, 50, 20);
    
    _phoneButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_phoneButton setTitle:@"010-56450585" forState:UIControlStateNormal];
    [_phoneButton setTitleColor:RGBCOLOR(159, 159, 159) forState:UIControlStateNormal];
    _phoneButton.titleLabel.font = [UIFont systemFontOfSize:14];
    [_phoneButton setContentHorizontalAlignment:UIControlContentHorizontalAlignmentLeft];
    [_phoneButton setImage:[UIImage imageNamed:@"left_phone2.png"] forState:UIControlStateNormal];
    [_phoneButton setImageEdgeInsets:UIEdgeInsetsMake(5, 50, 5, 135)];
    [_phoneButton addTarget:self action:@selector(phoneButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
    _phoneButton.frame = CGRectMake(0, self.view.height - 35, 163, 25);
    
    _serverSegment = [[UISegmentedControl alloc] initWithItems:@[@"正式", @"老姚", @"老王", @"荣辉"]];
    [_serverSegment addTarget:self action:@selector(serverChanged) forControlEvents:UIControlEventValueChanged];
    NSArray* servers = @[@"s.tingchebao.com", @"192.168.199.240", @"192.168.199.239", @"192.168.199.251"];
    _serverSegment.selectedSegmentIndex = [servers indexOfObject:GL(save_server_url)];
    _serverSegment.frame = CGRectMake(30, self.view.height - 70, 200, 30);
    
    [self.view addSubview:_topView];
    [self.view addSubview:_tableView];
    [self.view addSubview:_logoImageVIew];
    [self.view addSubview:_phoneButton];
    
#pragma mark 如果是正式发布 注释到这三行代码某些地方
    [self.view addSubview:_serverSegment];
    _rows = [NSMutableArray arrayWithObjects:@"历史订单",/* @"我的收藏", @"我的消息",*/ @"我要推荐", @"我要反馈", @"设置"/*, @"手机号切换-->"*/, nil];
    _images = @[@"left_order.png"/*, @"left_favorite.png", @"left_message.png",*/, @"left_recommend.png", @"left_feedback.png", @"left_setting2.png"/*, @""*/];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_rows count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    TLeftMenuCell* cell = [tableView dequeueReusableCellWithIdentifier:@"LeftCell"];
    if (!cell)
        cell = [[TLeftMenuCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"LeftCell"];
    cell.imgView.image = [UIImage imageNamed:[_images objectAtIndex:indexPath.row]];
    cell.nameLabel.text = [_rows objectAtIndex:indexPath.row];
    cell.nameLabel.textColor = RGBCOLOR(159, 159, 159);
    cell.backgroundColor = RGBCOLOR(244, 244, 244);
    return cell;
}

//- (UIView*)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
//    UIView* view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 44)];
//    UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 84, 44)];
//    label.text = @"菜单";
//    label.textColor = RGBCOLOR(159, 159, 159);
//    label.textAlignment = NSTextAlignmentCenter;
////    UIView* lineView = [[UIView alloc] initWithFrame:CGRectMake(0, 43.5, self.view.width, 0.5)];
////    lineView.backgroundColor = [UIColor whiteColor];
//    [view addSubview:label];
////    [view addSubview:lineView];
//    return view;
//}
//
//- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
//    return 44;
//}

#pragma mark UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [_tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    //先登录
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        TLoginViewController* login = [[TLoginViewController alloc] init];
        UINavigationController* nv = [[UINavigationController alloc] initWithRootViewController:login];
        [[TViewController share] presentViewController:nv animated:YES completion:nil];
        return;
    }
    
    UIViewController* controller = nil;
    switch (indexPath.row) {
        case 0:
            controller = [[THistoryOrderViewController alloc] init];
            break;
        case 1:
            controller = [[TRecommendUserViewController alloc] init];
            break;
        case 2:
            controller = [UMFeedback feedbackViewController];
            controller.navigationItem.leftBarButtonItem = [self getLeftItem];
            break;
        case 3:
            controller = [[TSettingViewController alloc] init];
            break;
        case 4:
            controller = [[TPhoneListController alloc] init];
            break;
        default:
            return;
            break;
    }
    [[TViewController share].centerController pushViewController:controller animated:NO];
    [[TViewController share] showOrHideLeftMenu];
}

- (void)phoneButtonTouched:(id)sender {
    UIAlertView* alert = [[UIAlertView alloc] initWithTitle:nil message:@"010-56450585" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
    alert.delegate = self;
    [alert show];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex != alertView.cancelButtonIndex) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", _phoneButton.titleLabel.text]]];
    }
}

- (void)buttonTouched:(UIButton*)button {
    if (button == _topView) {
        if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
            TLoginViewController* login = [[TLoginViewController alloc] init];
            UINavigationController* nv = [[UINavigationController alloc] initWithRootViewController:login];
            [[TViewController share] presentViewController:nv animated:YES completion:nil];
        } else {
            TAccountViewController* vc = [[TAccountViewController alloc] init];
            [[TViewController share].centerController pushViewController:vc animated:NO];
            [[TViewController share] showOrHideLeftMenu];
        }
    }
}

//更换服务器地址
- (void)serverChanged {
    NSArray* servers = @[@"s.tingchebao.com", @"192.168.199.240", @"192.168.199.239", @"192.168.199.251"];
    SL([servers objectAtIndex:_serverSegment.selectedSegmentIndex], save_server_url);
}

#pragma mark public

- (void)selectIndex:(int)index {
    NSIndexPath* indexPath = [NSIndexPath indexPathForRow:index inSection:0];
    [_tableView selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionTop];
    [self tableView:_tableView didSelectRowAtIndexPath:indexPath];
}

- (void)selectHomePage {
    UINavigationController* nv = [[UINavigationController alloc] initWithRootViewController:[THomeViewController share]];
    [TViewController share].centerController = nv;
    [[TViewController share] showOrHideLeftMenu];
}

- (void)startRequestCarNumber:(BOOL)start isAuth:(NSString*)isAuth {
    if (start) {
        [_numberActivityIndicator startAnimating];
        _carNumberStatusView.image = nil;
    } else {
        [_numberActivityIndicator stopAnimating];
        NSString* name = [NSString stringWithFormat:@"carStatus_%@.png", isAuth];
        UIImage* image = [UIImage imageNamed:name];
        _carNumberStatusView.image = image;
        _carNumberStatusView.width = [isAuth isEqualToString:@"-1"] ? 63 : ([isAuth isEqualToString:@"-2"] ? 55 : 45);
    }
    _phoneLabel.text = [[NSUserDefaults standardUserDefaults] objectForKey:save_phone];
}

- (void)updateLoginState:(BOOL)login {
    _loginLabel.hidden = login;
    _carNumberStatusView.hidden = _phoneLabel.hidden = _detailLabel.hidden = _rightArrowImageView.hidden = !login;
}

#pragma mark tmp for comment

- (UIBarButtonItem*)getLeftItem {
    UIButton* leftButton = [UIButton buttonWithType:UIButtonTypeCustom];
    leftButton.frame = CGRectMake(0, 0, 30, 30);
    [leftButton setImage:[UIImage imageNamed:@"left_arrow_gray.png"] forState:UIControlStateNormal];
    [leftButton setImageEdgeInsets:UIEdgeInsetsMake(5, 0, 5, 10)];
    [leftButton addTarget:self action:@selector(clickedLeftItem:) forControlEvents:UIControlEventTouchUpInside];
    
    UIBarButtonItem* item = [[UIBarButtonItem alloc] initWithCustomView:leftButton];
    return item;
}

- (void)clickedLeftItem :(UIButton*)button {
    [[TViewController share].centerController popViewControllerAnimated:YES];
}

@end
