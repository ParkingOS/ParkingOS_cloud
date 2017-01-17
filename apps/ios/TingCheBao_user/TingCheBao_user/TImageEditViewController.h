//
//  TImageEditViewController.h
//  
//
//  Created by apple on 15/7/14.
//
//

#import "TBaseViewController.h"

typedef void(^ImageEditHandle)(UIImage* clipsImg);

@interface TImageEditViewController : TBaseViewController

- (id)initWithImage:(UIImage*)image completeHandle:(ImageEditHandle)handle;

@end
