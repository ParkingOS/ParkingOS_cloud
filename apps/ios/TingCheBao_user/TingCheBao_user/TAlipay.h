//
//  TAlipay.h
//  TingCheBao_user
//
//  Created by apple on 14/11/5.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

//支付宝
@protocol TAlipayDelegate <NSObject>

- (void)alipayFail;

@end
@interface TAlipay : NSObject

+ (id)getInstance;

- (void)sendWithName:(NSString*)name price:(NSString*)price description:(NSString*)description delegate:(id<TAlipayDelegate>)delegate;
-(void)paymentResult:(NSString *)resultd;


@end
