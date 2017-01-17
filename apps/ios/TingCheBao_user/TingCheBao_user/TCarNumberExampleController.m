//
//  TCarNumberExampleController.m
//  
//
//  Created by apple on 15/7/15.
//
//


#import "TCarNumberExampleController.h"

@interface TCarNumberExampleController ()

@property(nonatomic, retain) UIImageView* imgView;

@end

@implementation TCarNumberExampleController

- (id)init {
    if (self = [super init]) {
        _imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"example.png"]];
        CGFloat img_width = 260;
        if (isIphoneNormal == NO) {
            img_width = 300;
        }
        _imgView.frame = CGRectMake((self.view.width - img_width)/2, (self.view.height - img_width/2*3)/2, img_width, img_width/2*3);
        
        [self.view addSubview:_imgView];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.titleView.text = @"示例照片";
    self.view.backgroundColor = [UIColor blackColor];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
