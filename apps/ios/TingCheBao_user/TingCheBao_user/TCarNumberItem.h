//
//  TCarNumberItem.h
//  
//
//  Created by apple on 15/7/13.
//
//

#import <Foundation/Foundation.h>

@interface TCarNumberItem : NSObject

@property(nonatomic, retain) NSString* car_number;
@property(nonatomic, retain) NSString* is_auth;

+ (TCarNumberItem*)getItemFromeDictionary:(NSDictionary*)dic;

//[{"car_number":"京QLL578","is_auth":"1"},{"car_number":"京QLL577","is_auth":"1"}]
@end
