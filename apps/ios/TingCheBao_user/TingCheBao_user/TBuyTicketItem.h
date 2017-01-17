//
//  TBuyTicketItem.h
//  
//
//  Created by apple on 15/8/26.
//
//

#import <Foundation/Foundation.h>

@interface TBuyTicketItem : NSObject

@property(nonatomic, retain) NSString* isauth;//是否认证
@property(nonatomic, retain) NSString* auth;//认证折扣
@property(nonatomic, retain) NSString* notauth;//未认证折扣

+ (TBuyTicketItem*)getItemFromDic:(NSDictionary*)dic;

@end
