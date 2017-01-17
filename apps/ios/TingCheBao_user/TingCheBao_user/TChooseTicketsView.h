//
//  TChooseTicketsView.h
//  TingCheBao_user
//
//  Created by apple on 14/11/3.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TTicketItem.h"

@protocol TChooseTicketsViewDelegate <NSObject>

- (void)ticketsChoose:(TTicketItem*)item;

@end
@interface TChooseTicketsView : UIView

@property(nonatomic, unsafe_unretained) id<TChooseTicketsViewDelegate> delegate;

@property(nonatomic, retain) NSArray* items;
@end
