//
//  TPayView.h
//  TingCheBao_user
//
//  Created by apple on 14/10/21.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TTicketItem.h"

//这个类暂时废弃
//这个类暂时废弃
//这个类暂时废弃
typedef enum {
    PayMessageModeEntry = 0,
    PayMessageModeExit
} PayMessageMode;

@protocol TPayViewDelegate <NSObject>

- (void)payButtonTouched:(UIButton*)button orderId:(NSString*)orderId money:(NSString*)money ticketId:(NSString*)ticketId total:(NSString*)total collectorId:(NSString*)collectorId;

@end
@interface TPayView : UIView

@property(nonatomic, assign) PayMessageMode payMessageMode;

- (void)setPayInfo:(NSDictionary *)payInfo item:(TTicketItem*)ticketItem yuE:(NSString*)yuE;

@property(nonatomic, unsafe_unretained) id<TPayViewDelegate> delegate;

@end
