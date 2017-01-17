//
//  TParkDetailItem.h
//  TingCheBao_user
//
//  Created by apple on 15/4/16.
//  Copyright (c) 2015年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TParkDetailItem : NSObject

@property(nonatomic, retain) NSString* parkId;
@property(nonatomic, retain) NSString* name;
@property(nonatomic, retain) NSString* lng;
@property(nonatomic, retain) NSString* lat;
@property(nonatomic, retain) NSString* free;
@property(nonatomic, retain) NSString* price;
@property(nonatomic, retain) NSString* total;
@property(nonatomic, retain) NSString* addr;
@property(nonatomic, retain) NSString* phone;
@property(nonatomic, retain) NSString* epay;
@property(nonatomic, retain) NSString* desc;
@property(nonatomic, retain) NSString* photo_url;

+ (TParkDetailItem*)getItemFromDictionary:(NSDictionary*)dictionary;

@end
