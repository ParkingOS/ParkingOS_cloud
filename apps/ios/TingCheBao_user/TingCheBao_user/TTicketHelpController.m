//
//  TTicketHelpController.m
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TTicketHelpController.h"

@interface TTicketHelpController()

@property(nonatomic, retain) NSString* name;
@property(nonatomic, retain) NSString* url;

@property(nonatomic, retain) CVAPIRequest* request;
@property(nonatomic, retain) MBProgressHUD* hud;

@end
@implementation TTicketHelpController

- (id)initWithName:(NSString*)name url:(NSString*)url {
    if (self = [super init]) {
        _name = name;
        _url = url;
        
        _webView = [[UIWebView alloc] initWithFrame:self.view.frame];
        NSURLRequest* request = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:_url]];
        _webView.delegate = self;
        [_webView loadRequest:request];
        [self.view addSubview:_webView];
    }
    return self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.titleView.text = _name;
    if (_redNavi) {
        UIButton* leftButton = [UIButton buttonWithType:UIButtonTypeCustom];
        leftButton.frame = CGRectMake(0, 0, 30, 30);
        [leftButton setImage:[UIImage imageNamed:@"left_arrow.png"] forState:UIControlStateNormal];
        [leftButton setImageEdgeInsets:UIEdgeInsetsMake(0, 0, 0, 0)];
        [leftButton addTarget:self action:@selector(clickedLeftItem:) forControlEvents:UIControlEventTouchUpInside];
        self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:leftButton];
        self.titleView.textColor = [UIColor whiteColor];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [_request cancel];
}

- (void)clickedLeftItem :(UIButton*)button {
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark UIWebViewDelegaet

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    [_hud removeFromSuperview];
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error {
    [_hud removeFromSuperview];
}

- (void)webViewDidStartLoad:(UIWebView *)webView {
    _hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
}
@end
