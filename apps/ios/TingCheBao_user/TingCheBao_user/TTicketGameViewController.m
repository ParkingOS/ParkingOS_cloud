//
//  TTicketGameViewController.m
//  TingCheBao_user
//
//  Created by apple on 15/5/30.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import "TTicketGameViewController.h"
#import "TShareView.h"
#import "UIImageView+WebCache.h"
#import "WXApi.h"
#import "WXApiObject.h"
#import "TAPIUtility.h"
#import "TBuyTicketViewController.h"

@interface TTicketGameViewController ()<TShareViewDelegate>

@end

@implementation TTicketGameViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    NSLog(@"request-%@", request);
    NSString* clickUrl = @"s.tingchebao.com/?desc=";
    if (clickUrl && [request.URL.absoluteString rangeOfString:clickUrl].length > 0) {
        
        //分享之后要返回到一个页面
        if (_goBackAfterShare) {
//            [self.webView goBack];
        }
        
        NSStringEncoding enc = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
        NSString* e = [request.URL.absoluteString stringByReplacingPercentEscapesUsingEncoding:enc];
        NSRange descRange = [e rangeOfString:@"desc="];
        NSRange titleRange = [e rangeOfString:@"&title="];
        NSRange imgurlRange = [e rangeOfString:@"&imgurl="];
        NSRange urlRange = [e rangeOfString:@"&url="];
        
        NSString* desc = [self subStringWithRange1:descRange range2:titleRange string:e];
        NSString* title = [self subStringWithRange1:titleRange range2:imgurlRange string:e];
        NSString* imgurl = [self subStringWithRange1:imgurlRange range2:urlRange string:e];
        NSString* url = [e substringFromIndex:urlRange.location + urlRange.length];
        
        TShareItem* item = [[TShareItem alloc] init];
        item.descri = desc;
        item.title = title;
        item.imgurl = imgurl;
        item.url = url;
        
        TShareView* shareView = [[TShareView alloc] initWithFrame:CGRectMake(0, 0, self.view.width, self.view.height)];
        shareView.delegate = self;
        shareView.item = item;
        [self.view addSubview:shareView];
        shareView.centerView.top = shareView.bottom;
        [UIView animateWithDuration:0.3 animations:^{
            shareView.centerView.top = shareView.height - 170;
        }];
        return NO;
    } else if ([request.URL.absoluteString rangeOfString:@"s.buyticket.com"].length > 0) {
        TBuyTicketViewController* vc = [[TBuyTicketViewController alloc] init];
        [self.navigationController pushViewController:vc animated:YES];
        return NO;
    }
    
    return  YES;
}

#pragma mark TShareViewDelegate

- (void)shareViewTouched:(TShareView *)shareView index:(NSInteger)index {
    
    TShareItem* item = shareView.item;
    [UIView animateWithDuration:0.3 animations:^{
        shareView.centerView.top = shareView.height;
    } completion:^(BOOL finished) {
        [shareView removeFromSuperview];
    }];
    
    [[SDWebImageDownloader sharedDownloader] downloadImageWithURL:[NSURL URLWithString:item.imgurl] options:0 progress:nil completed:^(UIImage *image, NSData *data, NSError *error, BOOL finished) {
        if (image) {
            //自定义事件
            WXWebpageObject* webObj = [WXWebpageObject object];
            webObj.webpageUrl = item.url;
            NSLog(@"weburl----%@", webObj.webpageUrl);
            
            WXMediaMessage* message = [WXMediaMessage message];
            message.title = item.title;
            message.description = item.descri;
            message.mediaObject = webObj;
            UIImage* thumbImg = [TAPIUtility ajustImage:image size:CGSizeMake(30, 30)];
            message.thumbData = UIImageJPEGRepresentation(thumbImg, 1);
            
            SendMessageToWXReq* req = [[SendMessageToWXReq alloc] init];
            if (index == 0)
                req.scene = WXSceneSession;//聊天界面
            else if (index == 1)
                req.scene = WXSceneTimeline;
            req.bText = NO;
            req.message = message;
            [WXApi sendReq:req];
        } else {
            NSLog(@"+++++++NO image");
        }
    }];
}

- (NSString*)subStringWithRange1:(NSRange)range1 range2:(NSRange)range2 string:(NSString*)str{
    NSString* desc = [str substringWithRange:NSMakeRange(range1.location + range1.length, range2.location - range1.location - range1.length)];
    return desc;
}

@end
