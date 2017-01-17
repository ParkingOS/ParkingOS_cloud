//
//  TSession.m
//  TingCheBao_user
//
//  Created by apple on 14-8-27.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TSession.h"
#import "TAPIUtility.h"

static TSession* instance;

@interface TSession ()

@property(nonatomic, retain) NSString* sessionFilePath;
@property(nonatomic, retain) NSMutableDictionary* session;

@end

NSString* pathOfSession() {
    
    NSString* sessionsDirPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject];
    sessionsDirPath = [sessionsDirPath stringByAppendingPathComponent:@"sessions"];
    
    
//    NSError* error;
//    if (![[NSFileManager defaultManager] fileExistsAtPath:sessionsDirPath]) {
//		
//		if (![[NSFileManager defaultManager] createDirectoryAtPath:sessionsDirPath
//									   withIntermediateDirectories:YES
//														attributes:nil
//															 error:&error])
//		{
//			NSLog(@"Failed to create folder '%@', error: %@", sessionsDirPath, error);
//            return nil;
//		}
//	}
    
//    NSString *sessionPath = [sessionsDirPath stringByAppendingFormat:@"/%@", [TAPIUtility getName]];
    return sessionsDirPath;
}

@implementation TSession
+ (TSession*)shared {
    if (instance == nil) {
        instance = [[TSession alloc] init];
    }
    return instance;
}

- (id)init {
    self = [super init];
    if (self) {
        _sessionFilePath = pathOfSession();
        _session = [NSMutableDictionary dictionaryWithContentsOfFile:_sessionFilePath];
        if (_session == nil)
            _session = [NSMutableDictionary dictionary];
    }
    return self;
}

#pragma mark - private

- (void)persistSession {
    [_session writeToFile:_sessionFilePath atomically:YES];
}

#pragma public 

- (void)setCarNumbers:(NSArray *)carNumbers {
    [_session setObject:carNumbers forKey:@"carNumbers"];
    
    [self persistSession];
}

- (NSArray*)carNumbers {
    NSArray* carNumbers =[_session objectForKey:@"carNumbers"];
    if (carNumbers == nil) {
        carNumbers = @[];
    }
    return carNumbers;
}

//- (void)setCarAuth:(NSString *)carAuth {
//    [_session setObject:carAuth forKey:@"carAuth"];
//    [self persistSession];
//}
//
//- (NSString*)carAuth {
//    return [_session objectForKey:@"carAuth"];
//}

- (void)removeAll {
    [_session removeAllObjects];
}

@end
