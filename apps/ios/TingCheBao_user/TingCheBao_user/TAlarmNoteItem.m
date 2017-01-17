//
//  TAlarmNoteItem.m
//  TingCheBao_user
//
//  Created by apple on 14/12/15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TAlarmNoteItem.h"
#import "TAPIUtility.h"

@implementation TAlarmNoteItem

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super init]) {
        _orderId = [aDecoder decodeObjectForKey:@"orderId"];
        
        _alarmDate = [aDecoder decodeObjectForKey:@"alarmDate"];
        if ([_alarmDate timeIntervalSinceNow] < 0)
            _alarmDate = nil;
        _images = [aDecoder decodeObjectForKey:@"images"] ? [aDecoder decodeObjectForKey:@"images"] : [NSMutableArray array];
        _note = [aDecoder decodeObjectForKey:@"note"] ?[aDecoder decodeObjectForKey:@"note"] : @"";
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)aCoder {
    [aCoder encodeObject:_orderId forKey:@"orderId"];
    [aCoder encodeObject:_alarmDate forKey:@"alarmDate"];
    [aCoder encodeObject:_images forKey:@"images"];
    [aCoder encodeObject:_note forKey:@"note"];
}

- (id)init {
    if (self = [super init]) {
        _orderId = @"";
        _alarmDate = nil;
        _images = [NSMutableArray array];
        _note = @"";
    }
    return self;
}

+ (TAlarmNoteItem*)getFromFile {
    TAlarmNoteItem* item = [NSKeyedUnarchiver unarchiveObjectWithFile:[TAlarmNoteItem getPath]];
    if (!item) {
        item = [[TAlarmNoteItem alloc] init];
    }
    return item;
}

+ (TAlarmNoteItem*)getFromFileWithOrderId:(NSString*)orderId {
    TAlarmNoteItem* item = [NSKeyedUnarchiver unarchiveObjectWithFile:[TAlarmNoteItem getPath]];
    if (!item || ![item.orderId isEqualToString:orderId]) {
        item = [[TAlarmNoteItem alloc] init];
        item.orderId = orderId;
    }
    return item;
}

- (void)saveToFile {
    [NSKeyedArchiver archiveRootObject:self toFile:[TAlarmNoteItem getPath]];
}

+ (NSString*)getPath {
    NSString* path = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
    path = [path stringByAppendingPathComponent:[NSString stringWithFormat:@"alarmNoteItem%@", [[NSUserDefaults standardUserDefaults] objectForKey:save_phone]]];
    return path;
}

+ (void)removeFile {
    NSFileManager* manager = [NSFileManager defaultManager];
    NSError* error = nil;
    [manager removeItemAtPath:[TAlarmNoteItem getPath] error:&error];
}

@end
