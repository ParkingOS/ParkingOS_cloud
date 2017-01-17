//
//  TWeixin.m
//  TingCheBao_user
//
//  Created by apple on 14/11/5.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TWeixin.h"


@interface TWeixin()<WXApiDelegate>

@property(nonatomic, unsafe_unretained) id<TWeixinDelegate>delegate;

@end
@implementation TWeixin

+ (id)getInstance {
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [self new];
    });
    return instance;
}

- (void)sendWithName:(NSString*)name price:(NSString*)price description:(NSString*)description delegate:(id<TWeixinDelegate>)delegate{
    _delegate = delegate;
    
    NSDictionary* params = @{@"action" : @"preorder",
                             @"body" : name,
                             @"total_fee" : price,
                             @"attach" : description};
    NSString* apiPath = @"wxpreorder.do";
    apiPath = [apiPath stringByAppendingString:[CVAPIRequest GETParamString:params]];
    CVAPIRequest* request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [model sendRequest:request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        //微信支付
        PayReq *request = [[PayReq alloc] init];
        request.partnerId = WXPartnerID;
        request.prepayId= [result objectForKey:@"prepayId"];
        request.package = @"Sign=WXpay";
        request.nonceStr= [result objectForKey:@"nonceStr"];
        request.timeStamp= [[result objectForKey:@"timeStamp"] longLongValue];
        request.sign= [result objectForKey:@"sign"];
        [WXApi sendReq:request];
    }];
}

- (void)onResp:(BaseResp *)resp {
    if ([resp isKindOfClass:[PayResp class]]) {
        PayResp *response = (PayResp *)resp;
        if (response.errCode == WXSuccess) {
            NSLog(@"wx success＝＝＝＝%@",response.errStr);
        } else {
            NSLog(@"wx fail---code:%d", response.errCode);
//            WXSuccess           = 0,
//            WXErrCodeCommon     = -1,
//            WXErrCodeUserCancel = -2,
//            WXErrCodeSentFail   = -3,
//            WXErrCodeAuthDeny   = -4,
//            WXErrCodeUnsupport  = -5,
            NSString* msg = @"";
            switch (response.errCode) {
                case WXErrCodeUserCancel:
                    msg = @"您取消了支付";
                    break;
                default:
                    msg = @"支付过程中出现异常";
                    break;
            }
            if (_delegate && [_delegate respondsToSelector:@selector(weixinFail)]) {
                [_delegate weixinFail];
            }
//            _overView.hidden = NO;
//            _resultView.hidden = NO;
//            [_resultView setObjectWithSucc:NO money:_price errorMsg:msg mode:_rechargeMode];
        }
    }
}
@end
