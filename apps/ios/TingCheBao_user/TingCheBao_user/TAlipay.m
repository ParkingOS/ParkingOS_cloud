//
//  TAlipay.m
//  TingCheBao_user
//
//  Created by apple on 14/11/5.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAlipay.h"

#import "Order.h"
#import "DataSigner.h"
#import <AlipaySDK/AlipaySDK.h>
#import "PartnerConfig.h"

@interface TAlipay()

@property(nonatomic, unsafe_unretained) id<TAlipayDelegate> delegate;
@property(nonatomic, retain) NSString* name;
@property(nonatomic, retain) NSString* price;
@property(nonatomic, retain) NSString* produceDescription;

@end

@implementation TAlipay

+ (id)getInstance {
    static dispatch_once_t once;
    static id instance;
    dispatch_once(&once, ^{
        instance = [self new];
    });
    return instance;
}

- (void)sendWithName:(NSString*)name price:(NSString*)price description:(NSString*)description delegate:(id<TAlipayDelegate>)delegate{
    _delegate = delegate;
    _name = name;
    _price = price;
    _produceDescription = description;
    
    NSString *appScheme = @"tingCheBao";
    NSString* orderInfo = [self getOrderInfo];
    NSString* signedStr = [self doRsa:orderInfo];
    
    NSLog(@"%@",signedStr);
    
    NSString *orderString = [NSString stringWithFormat:@"%@&sign=\"%@\"&sign_type=\"%@\"",
                             orderInfo, signedStr, @"RSA"];
    
    [[AlipaySDK defaultService] payOrder:orderString fromScheme:appScheme callback:^(NSDictionary *resultDic) {
//        NSLog(@"reslut = %@",resultDic);
        if (![[resultDic objectForKey:@"resultStatus"] isEqualToString:@"9000"]) {
            if (_delegate && [_delegate respondsToSelector:@selector(alipayFail)]) {
                [_delegate alipayFail];
            }
        }
    }];
}

#pragma mark 支付宝

-(NSString*)getOrderInfo
{
    /*
     *点击获取prodcut实例并初始化订单信息
     */
    
    
    Order *order = [[Order alloc] init];
    order.partner = PartnerID;
    order.seller = SellerID;
    
    order.tradeNO = [self generateTradeNO]; //订单ID（由商家自行制定）
    order.productName = _name; //商品标题

    order.productDescription = _produceDescription;
    order.amount = _price; //商品价格
    order.notifyURL =  @"http%3A%2F%2Fs.zhenlaidian.com/zld/rechage"; //回调URL
    
    //new version
    order.service = @"mobile.securitypay.pay";
    order.paymentType = @"1";
    order.inputCharset = @"utf-8";
    order.itBPay = @"30m";
    order.showUrl = @"m.alipay.com";
    
    return [order description];
}

- (NSString *)generateTradeNO
{
    const int N = 15;
    
    NSString *sourceString = @"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    NSMutableString *result = [[NSMutableString alloc] init] ;
    srand(time(0));
    for (int i = 0; i < N; i++)
    {
        unsigned index = rand() % [sourceString length];
        NSString *s = [sourceString substringWithRange:NSMakeRange(index, 1)];
        [result appendString:s];
    }
    return result;
}

-(NSString*)doRsa:(NSString*)orderInfo
{
    id<DataSigner> signer;
    signer = CreateRSADataSigner(PartnerPrivKey);
    NSString *signedString = [signer signString:orderInfo];
    return signedString;
}

-(void)paymentResultDelegate:(NSString *)result
{
    NSLog(@"%@",result);
}

////wap回调函数
//-(void)paymentResult:(NSString *)resultd
//{
//    //结果处理
//    
//    AlixPayResult* result = [[AlixPayResult alloc] initWithString:resultd];
//    if (result)
//    {
//        
//        if (result.statusCode == 9000)
//        {
//            /*
//             *用公钥验证签名 严格验证请使用result.resultString与result.signString验签
//             */
//            
//            //交易成功
//            NSString* key = AlipayPubKey;//签约帐户后获取到的支付宝公钥
//            id<DataVerifier> verifier;
//            verifier = CreateRSADataVerifier(key);
//            
//            if ([verifier verifyString:result.resultString withSign:result.signString])
//            {
//                //验证签名成功，交易结果无篡改
//                NSLog(@"支付宝成功");
//            }
//        }
//        else
//        {
//            //交易失败
//            if (_delegate && [_delegate respondsToSelector:@selector(alipayFail)]) {
//                [_delegate alipayFail];
//            }
//        }
//    }
//    else
//    {
//        //失败
//            if (_delegate && [_delegate respondsToSelector:@selector(alipayFail)]) {
//                [_delegate alipayFail];
//            }
////        _overView.hidden = NO;
////        _resultView.hidden = NO;
////        [_resultView setObjectWithSucc:NO money:_price errorMsg:@"支付过程中出现异常" mode:_rechargeMode];
//    }

//}

@end
