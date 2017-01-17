//
//  TShareItem.h
//  TingCheBao_user
//
//  Created by apple on 14/12/25.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TShareItem : NSObject

@property(nonatomic, retain) NSString* imgurl;
@property(nonatomic, retain) NSString* title;
@property(nonatomic, retain) NSString* descri;
@property(nonatomic, retain) NSString* url;

//{"imageurl":"hbonou_l.png","title":"全城派送大红包，快来领取停车券吧","description":"停车没烦恼，就用停车宝。快速找车位，一键付车费，体验萌萌哒。","url":"carowner.do?action=getobonus"}
+ (TShareItem*) getItemFromDic:(NSDictionary*)dic;

@end
