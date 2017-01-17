//
//  TScanParkRedPackageItem.h
//  
//
//  Created by apple on 15/7/27.
//
//

#import <Foundation/Foundation.h>

@interface TScanParkRedPackageItem : NSObject

@property(nonatomic, retain) NSString* collectorId;//收费员id
@property(nonatomic, retain) NSString* collectorName;//收费员名字
@property(nonatomic, retain) NSString* cname;//车场名称
@property(nonatomic, retain) NSString* money;//券金额

@end
