//
//  TAlarmNoteItem.h
//  TingCheBao_user
//
//  Created by apple on 14/12/15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TAlarmNoteItem : NSObject<NSCoding>

@property(nonatomic, retain) NSString* orderId;
@property(nonatomic, retain) NSDate* alarmDate;
@property(nonatomic, retain) NSMutableArray* images;
@property(nonatomic, retain) NSString* note;

- (void)saveToFile;
+ (TAlarmNoteItem*)getFromFile;
+ (TAlarmNoteItem*)getFromFileWithOrderId:(NSString*)orderId;
+ (void)removeFile;

@end
