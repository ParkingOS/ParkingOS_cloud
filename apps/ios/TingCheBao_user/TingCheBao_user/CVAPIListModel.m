//
//  CVAPIListModel.m
//  TingCheBao
//
//  Created by apple on 14-7-15.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "CVAPIListModel.h"

#define ENCODE_KEY_ITEMS         @"items"
#define ENCODE_KEY_NEXT_ITEMS    @"nextItems"
#define ENCODE_KEY_PREV_ITEMS    @"prevItems"
#define ENCODE_KEY_INDEX         @"idx"
#define ENCODE_KEY_HASMORE       @"hasMore"
#define ENCODE_KEY_SELECTEDKEYS  @"selectedKeys"
#define ENCODE_KEY_TOTAL         @"total"

@implementation CVAPIListModel

- (id)init {
    if (self = [super init]) {
        _items = [NSMutableArray array];
        _nextItems = [NSMutableArray array];
        _prevItems = [NSMutableArray array];
        _selectedKeys = [NSMutableSet set];
        _isActionMode = NO;
        _keySetOfContactItems = [NSMutableSet set];
        _total = 0;
    }
    return self;
}

- (void)loadMore:(BOOL)more {
    // to be overriden
}

#pragma mark - NSCoding

- (id)initWithCoder:(NSCoder *)aDecoder {
    if (self = [super init]) {
        // decode instance properties with the coder
        _selectedKeys = [NSMutableSet set];
        _items = [[aDecoder decodeObjectForKey:ENCODE_KEY_ITEMS] mutableCopy];
        _nextItems = [[aDecoder decodeObjectForKey:ENCODE_KEY_NEXT_ITEMS] mutableCopy];
        _prevItems = [[aDecoder decodeObjectForKey:ENCODE_KEY_PREV_ITEMS] mutableCopy];
        _pageIdx = [aDecoder decodeIntegerForKey:ENCODE_KEY_INDEX];
        _hasMore = [aDecoder decodeBoolForKey:ENCODE_KEY_HASMORE];
        _total = [aDecoder decodeIntegerForKey:ENCODE_KEY_TOTAL];
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)aCoder {
    
    // encode the properties
    // note: never encode delegate property
    
    [super encodeWithCoder:aCoder];
    
    [aCoder encodeObject:_items forKey:ENCODE_KEY_ITEMS];
    [aCoder encodeObject:_nextItems forKey:ENCODE_KEY_NEXT_ITEMS];
    [aCoder encodeObject:_prevItems forKey:ENCODE_KEY_PREV_ITEMS];
    [aCoder encodeInteger:_pageIdx forKey:ENCODE_KEY_INDEX];
    [aCoder encodeBool:_hasMore forKey:ENCODE_KEY_HASMORE];
    [aCoder encodeInteger:_total forKey:ENCODE_KEY_TOTAL];
}


- (void)updateWithFilter:(NSString*)filter {
    
    _filter = filter;
    
    [self loadMore:NO];
    
}


-(void)updateWithStatus:(NSString *)status {
    
    _status = status;
    
    [self loadMore:NO];
}

- (void)updateWithType:(NSString*)type {
    
    _type = type;
    
    [self loadMore:NO];
}

- (void)search:(NSString *)searchText {
    
    _searchText = searchText;
    
    [self loadMore:NO];
}

- (void)sortByField:(NSString*)sortBy {
    
    _sortBy = sortBy;
    
    [self loadMore:NO];
}

- (void)sortByField:(NSString*)sortBy withOrder:(NSString*)order {
    
    _sortBy = sortBy;
    _order = order;
    
    [self loadMore:NO];
}


@end
