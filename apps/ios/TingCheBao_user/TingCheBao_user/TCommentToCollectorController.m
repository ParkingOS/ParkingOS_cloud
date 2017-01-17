//
//  TPostCommentViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/5/12.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TCommentToCollectorController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "UIKeyboardViewController.h"
#import "TCurrentOrderViewController.h"

#define padding 10

@interface TCommentToCollectorController ()<UITextViewDelegate, MBProgressHUDDelegate, UIScrollViewDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UITextView* commentTextView;
@property(nonatomic, retain) UILabel* limitLabel;
@property(nonatomic, retain) UIButton* commentButton;

@property(nonatomic, retain) CVAPIRequest* sendRequest;
@property(nonatomic, retain) UIKeyboardViewController* keyboardViewController;

@end

@implementation TCommentToCollectorController

- (id)init {
    if (self = [super init]) {
        _scrollView = [[UIScrollView alloc] initWithFrame:self.view.frame];
        _scrollView.contentSize = CGSizeMake(self.view.width, self.view.height + 0.5);
        _scrollView.delegate = self;
        
        _commentTextView = [[UITextView alloc] initWithFrame:CGRectMake(padding, padding, self.view.width - 2*padding, 120)];
        _commentTextView.delegate = self;
        _commentTextView.font = [UIFont systemFontOfSize:17];
        _commentTextView.returnKeyType = UIReturnKeyDone;
        
        _limitLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width - padding - 200, _commentTextView.bottom + 10, 200, 20)];
        _limitLabel.textColor = [UIColor grayColor];
        _limitLabel.text = @"还能输入200个字";
        _limitLabel.textAlignment = NSTextAlignmentRight;
        
        _commentButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_commentButton setTitle:@"发表评论" forState:UIControlStateNormal];
        [_commentButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        [_commentButton setBackgroundImage:[TAPIUtility imageWithColor:green_color] forState:UIControlStateNormal];
        _commentButton.layer.cornerRadius = 5;
        _commentButton.clipsToBounds = 5;
        _commentButton.frame = CGRectMake(padding, self.view.height - padding - 45, self.view.width - 2*padding, 45);
        [_commentButton addTarget:self action:@selector(buttonTouched:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.scrollView addSubview:_commentTextView];
        [self.scrollView addSubview:_limitLabel];
        [self.scrollView addSubview:_commentButton];
        
        [self.view addSubview:_scrollView];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"评论收费员";
    self.view.backgroundColor = RGBCOLOR(237, 237, 237);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
//    _keyboardViewController = [[UIKeyboardViewController alloc] initWithControllerDelegate:self];
//    [_keyboardViewController addToolbarToKeyboard];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    [_sendRequest cancel];
}

#pragma mark UITextViewDelegate

- (void)textViewDidChange:(UITextView *)textView {
    if (textView.text.length >= 200) {
        textView.text = [textView.text substringWithRange:NSMakeRange(0, 200)];
        [TAPIUtility alertMessage:@"达到200个最大字数"];
    }
    _limitLabel.text = [NSString stringWithFormat:@"还能输入%ld个字", 200 - textView.text.length];
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    if ([text isEqualToString:@"\n"]) {
        [textView endEditing:YES];
        return NO;
    }
    return YES;
}

#pragma mark UIScrollViewDelegate

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    if (scrollView == _scrollView) {
        [_scrollView endEditing:YES];
    }
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    if ([_commentTextView.text isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"评论内容不能为空!"];
        return;
    }
    
    NSDictionary* parmas = @{@"action" : @"pusercomment",
                             @"orderid" : _orderid,
                             @"mobile" : [[NSUserDefaults standardUserDefaults] objectForKey:save_phone],
                             @"comment" : _commentTextView.text};
    NSString* apiPath = [NSString stringWithFormat:@"carowner.do%@", [CVAPIRequest GETParamString:parmas]];
    _sendRequest = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    _sendRequest.hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self.model sendRequest:_sendRequest completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        if ([[result objectForKey:@"result"] isEqualToString:@"1"]) {
            [TAPIUtility alertMessage:@"感谢您的评价" success:YES toViewController:self];
            _commentTextView.text = @"";
            _commentButton.enabled = NO;
            [_commentButton setTitle:@"已评论" forState:UIControlStateNormal];
            
        } else {
            [TAPIUtility alertMessage:[result objectForKey:@"errmsg"] success:NO toViewController:nil];
        }
    }];
}

- (void)hudWasHidden:(MBProgressHUD *)hud {
    if (_completer) {
        _completer();
    }
    if (!_needPushOrderPage) {
        [self.navigationController popViewControllerAnimated:YES];
    } else {
        UINavigationController* nv = self.navigationController;
        [nv popToRootViewControllerAnimated:NO];
        
        TCurrentOrderViewController* vc = [[TCurrentOrderViewController alloc] init];
        vc.historyOrderid = _orderid;
        
        [nv pushViewController:vc animated:YES];
    }
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
