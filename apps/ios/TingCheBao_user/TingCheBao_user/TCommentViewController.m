//
//  TCommentViewController.m
//  TingCheBao_user
//
//  Created by apple on 14-9-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TCommentViewController.h"
#import "TCommentCell.h"
#import "CVAPIRequestModel.h"
#import "DTAttributedTextView.h"
#import "TAPIUtility.h"
#import "TLoginViewController.h"

#define button_height 40
#define padding 13

#define tagButton_color RGBCOLOR(82, 88, 102)
#define tagButton_sel_color RGBCOLOR(10, 10, 10)

@interface TCommentViewController ()<UITableViewDelegate, UITableViewDataSource, UITextViewDelegate>

@property(nonatomic, retain) UITableView* tableView;
@property(nonatomic, retain) UIView* bottomView;
@property(nonatomic, retain) UIView* tagsView;
@property(nonatomic, retain) UIButton* arrowView;

@property(nonatomic, retain) UIView* lineView;

@property(nonatomic, retain) UIView* chatBarView;
@property(nonatomic, retain) UITextView* commentTextView;
@property(nonatomic, retain) UIButton* sendButton;

@property(nonatomic, retain) NSMutableArray* items;
@property(nonatomic, retain) NSArray* options;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) CVAPIRequest* sendRequest;

@end

@implementation TCommentViewController

- (id)init {
    if (self = [super init]) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        _tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _tableView.allowsSelection = NO;
        
        //bottome
        _bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.height - 171, self.view.width, 171)];
        _bottomView.backgroundColor = RGBCOLOR(53, 60, 76);
        
        //tagsViwe
        _tagsView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 130)];
        _tagsView.clipsToBounds = YES;
        
        _arrowView = [UIButton buttonWithType:UIButtonTypeCustom];
        [_arrowView setImage:[UIImage imageNamed:@"ic_drawer_open.png"] forState:UIControlStateNormal];
        _arrowView.frame = CGRectMake(0, 0, self.view.width, 30);
        [_arrowView setImageEdgeInsets:UIEdgeInsetsMake(10, (self.view.width - 30)/2, 10, (self.view.width - 30)/2)];
        _arrowView.tag = 0;
        [_arrowView addTarget:self action:@selector(showTagsView) forControlEvents:UIControlEventTouchUpInside];
        
        _options = @[@"数据挺准", @"环境很好", @"服务很好", @"数据不准", @"环境一般", @"服务一般"];
        CGFloat button_width = (self.view.width - 4*padding) / 3;
        for (NSString* option in _options) {
            int i = [_options indexOfObject:option];
            UIButton* button = [UIButton buttonWithType:UIButtonTypeCustom];
            [button setTitle:option forState:UIControlStateNormal];
            [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
            button.titleLabel.font = [UIFont systemFontOfSize:15];
            button.tag = i+1;
            [button setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(82, 88, 102)] forState:UIControlStateNormal];
            button.frame = CGRectMake(i%3 * button_width + (i%3+1)*padding, _arrowView.bottom + i/3*(button_height+padding), button_width, button_height);
            [button addTarget:self action:@selector(tagButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
            [_tagsView addSubview:button];
        }
        [_tagsView addSubview:_arrowView];
        
        //charBarView
        _chatBarView = [[UIView alloc] initWithFrame:CGRectMake(0, _tagsView.bottom, self.view.width, 41)];
        
        _lineView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, 1)];
        _lineView.backgroundColor = [UIColor whiteColor];
        
        _commentTextView = [[UITextView alloc] initWithFrame:CGRectMake(padding, _lineView.bottom + 5, self.view.width - 100, 30)];
        _commentTextView.returnKeyType = UIReturnKeySend;
        _commentTextView.delegate = self;
        
        _sendButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_sendButton setTitle:@"发送" forState:UIControlStateNormal];
        [_sendButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_sendButton setBackgroundImage:[TAPIUtility imageWithColor:RGBCOLOR(82, 88, 102)] forState:UIControlStateNormal];
        _sendButton.frame = CGRectMake(_commentTextView.right + 10, _commentTextView.top, 70, 30);
        [_sendButton addTarget:self action:@selector(sendButtonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [_chatBarView addSubview:_lineView];
        [_chatBarView addSubview:_commentTextView];
        [_chatBarView addSubview:_sendButton];
        
        [_bottomView addSubview:_tagsView];
        [_bottomView addSubview:_chatBarView];
        
        [self.view addSubview:_tableView];
        [self.view addSubview:_bottomView];
    }
    return self;
}
- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _items = [NSMutableArray array];
    self.titleView.text = @"评论详情";
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    //保证tableView 不被挡住
    _tableView.contentInset = UIEdgeInsetsMake(0, 0, _tableView.bottom  - _bottomView.top , 0);
    [self requestCommentInfo];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(showKeyBoard:) name:UIKeyboardWillChangeFrameNotification object:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_commentTextView endEditing:YES];
    
    [_request cancel];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark UITextViewDelegate

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    if ([text isEqualToString:@"\n"]) {
        [self requestSendComment];
        return NO;
    }
    return YES;
}

- (void)textViewDidChange:(UITextView *)textView {
    [self updateColorWithContent:@"数据挺准" content2:@"数据不准" tag:1];
    [self updateColorWithContent:@"环境很好" content2:@"环境一般" tag:2];
    [self updateColorWithContent:@"服务很好" content2:@"服务一般" tag:3];
}

#pragma mark notification

- (void)updateColorWithContent:(NSString*)content1 content2:(NSString*)content2 tag:(int)tag {
    UIButton* button = (UIButton*)[_tagsView viewWithTag:tag];
    UIButton* button2 = (UIButton*)[_tagsView viewWithTag:tag + 3];
    if ([_commentTextView.text rangeOfString:content1].length > 0 || [_commentTextView.text rangeOfString:content2].length > 0) {
        button.userInteractionEnabled = NO;
        button2.userInteractionEnabled = NO;
        if ([_commentTextView.text rangeOfString:content1].length > 0) {
            [button setBackgroundImage:[TAPIUtility imageWithColor:tagButton_sel_color] forState:UIControlStateNormal];
            [button2 setBackgroundImage:[TAPIUtility imageWithColor:tagButton_color] forState:UIControlStateNormal];
        } else {
            [button setBackgroundImage:[TAPIUtility imageWithColor:tagButton_color] forState:UIControlStateNormal];
            [button2 setBackgroundImage:[TAPIUtility imageWithColor:tagButton_sel_color] forState:UIControlStateNormal];
        }
    } else {
        button.userInteractionEnabled = YES;
        button2.userInteractionEnabled = YES;
        [button setBackgroundImage:[TAPIUtility imageWithColor:tagButton_color] forState:UIControlStateNormal];
        [button2 setBackgroundImage:[TAPIUtility imageWithColor:tagButton_color] forState:UIControlStateNormal];
    }
}

#pragma mark UIScrollViewDelegate

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    [_chatBarView endEditing:YES];
}

#pragma private

- (void)requestCommentInfo {
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do?action=getcomment&comid=%@", _parkId];
    _request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _request.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        [_items removeAllObjects];
        for (NSDictionary* object in result) {
            TCommentItem* item = [TCommentItem getItemWithDictionary:object];
            [_items addObject:item];
        }
        [_tableView reloadData];
    }];
}

- (void)requestSendComment {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone]) {
        TLoginViewController* vc = [[TLoginViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        return;
    }
    NSDictionary* parmas = @{@"action" : @"comment",
                             @"comid" : _parkId,
                             @"mobile" : [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],
                             @"comment" : _commentTextView.text};
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do%@", [CVAPIRequest GETParamString:parmas]];
    _sendRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _sendRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_sendRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        if ([[result objectForKey:@"info"] isEqualToString:@"1"]) {
            [TAPIUtility alertMessage:@"感谢您的评价" success:YES toViewController:self];
            _commentTextView.text = @"";
            //更新tags
            [self textViewDidChange:_commentTextView];
            
            [_commentTextView endEditing:YES];
            //重新请求comment,然后刷新页面
            [self requestCommentInfo];
        } else {
            [TAPIUtility alertMessage:@"对不起，评价失败～" success:NO toViewController:self];
        }
    }];

}

- (void)showTagsView {
    //打开或关闭tagView
    if (_arrowView.tag == 0) {
        [UIView animateWithDuration:0.2 animations:^{
            _bottomView.frame = CGRectMake(0, _bottomView.top + 100, self.view.width, _bottomView.height - 100);
            _tagsView.height -= 100;
            _chatBarView.top = _tagsView.bottom;
        } completion:^(BOOL finished) {
            [_arrowView setImage:[UIImage imageNamed:@"ic_drawer_closed.png"] forState:UIControlStateNormal];
            _arrowView.tag = 1;
            //保证tableView 不被挡住
            _tableView.contentInset = UIEdgeInsetsMake(0, 0, _tableView.bottom  - _bottomView.top , 0);
        }];
    } else {
        [UIView animateWithDuration:0.2 animations:^{
            _bottomView.frame = CGRectMake(0, _bottomView.top - 100, self.view.width, _bottomView.height + 100);
            _tagsView.height += 100;
            _chatBarView.top = _tagsView.bottom;
        } completion:^(BOOL finished) {
            [_arrowView setImage:[UIImage imageNamed:@"ic_drawer_open.png"] forState:UIControlStateNormal];
            _arrowView.tag = 0;
            //保证tableView 不被挡住
            _tableView.contentInset = UIEdgeInsetsMake(0, 0, _tableView.bottom  - _bottomView.top , 0);
        }];
    }
}

- (void)tagButtonTouched:(UIButton*) button{
    [button setBackgroundImage:[TAPIUtility imageWithColor:tagButton_sel_color] forState:UIControlStateNormal];
    button.userInteractionEnabled = NO;
    _commentTextView.text = [NSString stringWithFormat:@"%@%@,", _commentTextView.text, button.titleLabel.text];
    UIButton* button2 = (UIButton*)[_tagsView viewWithTag:(button.tag < 4 ? button.tag + 3 : button.tag - 3)];
    button2.userInteractionEnabled = NO;
}


- (void)sendButtonTouched:(UIButton*)button {
    [self requestSendComment];
}

- (void)showKeyBoard:(NSNotification*)noti {
    CGRect frame = [[noti.userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    frame = [self.view convertRect:frame fromView:self.view.window];
    
    NSTimeInterval duration = [[noti.userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    UIViewAnimationCurve curve = (UIViewAnimationCurve)[noti.userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey];
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationCurve:curve];
    [UIView setAnimationDuration:duration];
    [UIView setAnimationBeginsFromCurrentState:YES];
    _bottomView.bottom = frame.origin.y;
    _tableView.contentInset = UIEdgeInsetsMake(0, 0, _tableView.bottom  - _bottomView.top , 0);
    [UIView commitAnimations];
}

#pragma mark UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [_items count];
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString* identifier = @"commentCell";
    TCommentCell* cell = [_tableView dequeueReusableCellWithIdentifier:identifier];
    if (!cell) {
        cell = [[TCommentCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:identifier];
    }
    cell.item = [_items objectAtIndex:indexPath.row];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    TCommentItem* item = [_items objectAtIndex:indexPath.row];
    DTAttributedTextView* textView = [[DTAttributedTextView alloc] init];
    
    NSMutableAttributedString* comment = [[NSMutableAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@", item.info] attributes:@{NSFontAttributeName : [UIFont systemFontOfSize:15]}];
    textView.attributedString = comment;
    
    CGFloat suggestedWidth = 200;
    if (isIphoneNormal == NO) {
        suggestedWidth = 300;
    }
    CGSize suggestedSize = [textView.attributedTextContentView suggestedFrameSizeToFitEntireStringConstraintedToWidth:suggestedWidth];
    return suggestedSize.height;
}

@end