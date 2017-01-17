//
//  CVAPIEntityModel.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "CVAPIEntityModel.h"

#define ENCODE_KEY_ENTITY_KEY    @"key"

@implementation CVAPIEntityModel

- (id)initWithKey:(NSString*)key {
    if (self = [super init]) {
        _key = key;
        self.readThroughCache = YES;
    }
    return self;
}


- (void)load {
    NSString* apiPath = [self getAPIPath];
    
    if (!apiPath)
        return;
    
    CVAPIRequest* request = [[CVAPIRequest alloc] initWithAPIPath:apiPath];
    [request setHTTPMethod:[self getMethd]];
    
    [self sendRequest:request completion:^(NSDictionary* apiResult, NSError* error){
        [self updateModelWithResult:apiResult error:error action:[self getAction]];
    }];
    
}

/* The following can be overridden by the subclass */

- (NSString*) getAPIPath {
    return nil;
}

- (NSString*) getMethd {
    return @"GET";
}

- (NSString*) getAction {
    return @"load";

}

#pragma mark - NSCoding

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super init]) {
        // decode instance properties with the coder
        _key = [aDecoder decodeObjectForKey:ENCODE_KEY_ENTITY_KEY];
        self.delegate = nil;
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)aCoder {
    
    // encode the properties
    // note: never encode delegate property
    
    [super encodeWithCoder:aCoder];
    [aCoder encodeObject:_key forKey:ENCODE_KEY_ENTITY_KEY];
    
}


#pragma mark - Cache Management

- (NSString *) getCacheKey
{
    NSString* className = NSStringFromClass([self class]);
    NSString *cacheKey = [NSString stringWithFormat:@"%@-%@", className, _key];
    return cacheKey;
}


@end
