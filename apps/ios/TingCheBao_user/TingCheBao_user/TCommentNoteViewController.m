//
//  TPostCommentViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/5/12.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TCommentNoteViewController.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "UIKeyboardViewController.h"
#import "TCurrentOrderViewController.h"

#define padding 10

@interface TCommentNoteViewController ()<UITextViewDelegate, MBProgressHUDDelegate, UIScrollViewDelegate>

@property(nonatomic, retain) UIScrollView* scrollView;
@property(nonatomic, retain) UITextView* commentTextView;
@property(nonatomic, retain) UILabel* limitLabel;
@property(nonatomic, retain) UIButton* commentButton;

@end

@implementation TCommentNoteViewController

- (id)init {
    if (self = [super init]) {
        _scrollView = [[UIScrollView alloc] initWithFrame:self.view.frame];
        _scrollView.contentSize = CGSizeMake(self.view.width, self.view.height + 0.5);
        _scrollView.delegate = self;
        
        _commentTextView = [[UITextView alloc] initWithFrame:CGRectMake(padding, padding, self.view.width - 2*padding, 120)];
        _commentTextView.delegate = self;
        _commentTextView.text = GL(save_location_note);
        _commentTextView.returnKeyType = UIReturnKeyDone;
        
        _limitLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.view.width - padding - 100, _commentTextView.bottom, 100, 20)];
        _limitLabel.textColor = [UIColor grayColor];
        _limitLabel.text = @"限50字";
        _limitLabel.textAlignment = NSTextAlignmentRight;
        
        _commentButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_commentButton setTitle:@"确认" forState:UIControlStateNormal];
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
    self.titleView.text = @"添加备注";
    self.view.backgroundColor = RGBCOLOR(237, 237, 237);
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
}

#pragma mark UITextViewDelegate

- (void)textViewDidChange:(UITextView *)textView {
    if (textView.text.length >= 50) {
        textView.text = [textView.text substringWithRange:NSMakeRange(0, 50)];
        [TAPIUtility alertMessage:@"达到50个最大字数"];
    }
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
    [_scrollView endEditing:YES];
}

#pragma mark private

- (void)buttonTouched:(UIButton*)button {
    SL(_commentTextView.text, save_location_note);
    [self.navigationController popViewControllerAnimated:YES];
}

@end
