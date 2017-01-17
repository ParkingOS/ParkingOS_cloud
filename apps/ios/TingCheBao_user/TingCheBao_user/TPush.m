//
//  TPush.m
//  TingCheBao_user
//
//  Created by apple on 14/10/30.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TPush.h"
#import "TAPIUtility.h"
#import "CVAPIRequestModel.h"
#import "TViewController.h"
#import "THomeViewController.h"


@implementation TPush

+ (id)getInstance {
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [self new];
    });
    return instance;
}

//{
//    aps =     {
//        alert =         {
//            body = "\U5145\U503c\U8d2d\U4e70\U4ea7\U54c1 ";
//            "launch-image" = default;
//        };
//        badge = 1;
//        sound = defalut;
//    };
//    payload = 179;
//}

//"{"mtype":"0","msgid":"1","info":{"total":"0.0","parkname":"中关村","address":"北京市海淀区上地三街26号","etime":"1414653295","state":"0","btime":"1414653283","parkid":"3","orderid":"238732"}}"

//{
//    info =     {
//        address = "\U5317\U4eac\U5e02\U6d77\U6dc0\U533a\U4e0a\U5730\U4e09\U88579\U53f7-d\U5ea7";
//        btime = 1414655767;
//        etime = 1414655767;
//        orderid = 238733;
//        parkid = 1475;
//        parkname = "\U52a0\U5bc6\U8f66\U573a";
//        state = 0;
//        total = "0.0";
//    };
//    msgid = 1;
//    mtype = 0;
//}
- (void)handlePush:(NSDictionary*)userInfo {
    NSString* msgId = [TAPIUtility getValidString:[userInfo objectForKey:@"payload"]];
    if (![msgId isEqualToString:@""]) {
        [self requestMsg:msgId];
    }
}

- (void)requestMsg:(NSString*)msgId {
    if (![[NSUserDefaults standardUserDefaults] objectForKey:save_phone])
        return;
    NSString* apiPath = nil;
    CVAPIRequest* request  = nil;
    if (msgId) {
        apiPath = [NSString stringWithFormat:@"carservice.do?action=getmesg&mesgid=%@&version=%@", msgId,[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"]];
        request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    } else {
        apiPath = [NSString stringWithFormat:@"carmessage.do?mobile=%@&version=%@&msgid=",[[NSUserDefaults standardUserDefaults] objectForKey:save_phone],[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"]];
//        NSLog(@"request new msg");
        request = [[CVAPIRequest alloc] initWithAPIPath:apiPath mserver:YES];
    }
    
    CVAPIRequestModel* model = [[CVAPIRequestModel alloc] init];
    model.hideNetworkView = YES;
    [model sendRequest:request completion:^(NSDictionary *result, NSError *error) {
        if (!result)
            return;
        NSLog(@"msg-----%@", [result description]);
        if ([result count] == 0) {
            return;
        }
        // ibeacon支付
        if ([[result objectForKey:@"mtype"] isEqualToString:@"9"]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:notification_msg_payResult object:nil userInfo:result];
            return;
        }
        
        NSLog(@"now-msgid--%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_msg_id]);
        if ([[result objectForKey:@"msgid"] isEqualToString:[[NSUserDefaults standardUserDefaults] objectForKey:save_msg_id]]) {
            NSLog(@"message has read");
            return;
        }
        if ([[result objectForKey:@"mtype"] isEqualToString:@"0"]) {
            NSString* state = [[result objectForKey:@"info"] objectForKey:@"state"];
            if ([state isEqualToString:@"1"] || [state isEqualToString:@"0"]) {
                //车算结算  state 结算未支付
                [[NSNotificationCenter defaultCenter] postNotificationName:notification_msg_prepare_payOrder object:nil userInfo:result];
//                [self alertMessage];
            } else if ([state isEqualToString:@"2"] || [state isEqualToString:@"-1"]) {
                //车场支付后 返回的结果 2 成功 -1失败
                [[NSNotificationCenter defaultCenter] postNotificationName:notification_msg_payResult object:nil userInfo:result];
            }
        } else if ([[result objectForKey:@"mtype"] isEqualToString:@"2"]) {
            [[NSNotificationCenter defaultCenter] postNotificationName:notification_msg_payResult object:nil userInfo:result];
        }
        
        //            {"mtype":"2","msgid":"1302","info":{"result":"1","errmsg":"0.01元充值成功"}}
        //            if ([[result objectForKey:@"mtype"] isEqualToString:@"2"]) {
        //                if ([[[result objectForKey:@"info"] objectForKey:@"result"] isEqualToString:@"1"])
        //                [[NSNotificationCenter defaultCenter] postNotificationName:notification_msg_chongzhi object:nil userInfo:@{@"info" : [result objectForKey:@"info"]}];
    }];
}

- (void)alertMessage {
    TViewController* vc = (TViewController*)[UIApplication sharedApplication].keyWindow.rootViewController;
    if ([vc isKindOfClass:[TViewController class]]) {
        UINavigationController* center = vc.centerController;
        if (![center.topViewController isKindOfClass:[THomeViewController class]]) {
            [TAPIUtility alertMessage:@"您有新的消息，请打开主页面查看～"];
        }
    }
}

@end
