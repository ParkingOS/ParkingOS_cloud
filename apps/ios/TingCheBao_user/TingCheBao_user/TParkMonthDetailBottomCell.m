//
//  TParkMonthDetailBottomCell.m
//  TingCheBao_user
//
//  Created by apple on 14-10-11.
//  Copyright (c) 2014年 zhenLaiDian. All rights reserved.
//

#import "TParkMonthDetailBottomCell.h"
#import "TAPIUtility.h"

#define padding 5

@interface TParkMonthDetailBottomCell()

@property(nonatomic, retain) UILabel* parkInfoLabel;
@property(nonatomic, retain) UIView* line1View;
@property(nonatomic, retain) UILabel* nameLabel;
@property(nonatomic, retain) UILabel* addressLabel;
@property(nonatomic, retain) UIView* line2View;
@property(nonatomic, retain) UIButton* phoneButton ;
@property(nonatomic, retain) UIView* line3View;
@property(nonatomic, retain) UIView* commentView;
@property(nonatomic, retain) UIImageView* priseImgView;
@property(nonatomic, retain) UILabel* priseLabel;
@property(nonatomic, retain) UIImageView* projectImgView;
@property(nonatomic, retain) UILabel* projectLabel;
@property(nonatomic, retain) UILabel* commentLabel;
@property(nonatomic, retain) UIImageView* commentImgView;

@property(nonatomic, retain) UIView* line4View;
@property(nonatomic, retain) UILabel* produceInfoLabel;
@property(nonatomic, retain) UIView* line5View;
@property(nonatomic, retain) UILabel* descriptionLabel;

@property(nonatomic, retain) UIView* line6View;
@property(nonatomic, retain) UILabel* knowHeaderLabel;
@property(nonatomic, retain) UIView* line7View;
@property(nonatomic, retain) UILabel* knowLabel;

@property(nonatomic, retain) UIWebView* webView;

@end
@implementation TParkMonthDetailBottomCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        _parkInfoLabel = [[UILabel alloc] init];
        _parkInfoLabel.backgroundColor = [UIColor clearColor];
        _parkInfoLabel.textColor = [UIColor blackColor];
        _parkInfoLabel.text = @"停车场信息";
        
        _nameLabel = [[UILabel alloc] init];
        _nameLabel.backgroundColor = [UIColor clearColor];
        _nameLabel.textColor = [UIColor grayColor];
        _nameLabel.text = @"";
        _nameLabel.font = [UIFont systemFontOfSize:13];
        
        _addressLabel = [[UILabel alloc] init];
        _addressLabel.backgroundColor = [UIColor clearColor];
        _addressLabel.textColor = [UIColor grayColor];
        _addressLabel.text = @"";
        _addressLabel.font = [UIFont systemFontOfSize:13];
        
        _phoneButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_phoneButton setBackgroundImage:[UIImage imageNamed:@"phones.png"] forState:UIControlStateNormal];
        [_phoneButton addTarget:self action:@selector(phoneButtonTouched) forControlEvents:UIControlEventTouchUpInside];
        
        _commentView = [[UIView alloc] init];
        UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTapGesture:)];
        [_commentView addGestureRecognizer:tapGesture];
        
        _priseImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_praise.png"]];
        
        _priseLabel = [[UILabel alloc] init];
        _priseLabel.backgroundColor = [UIColor clearColor];
        _priseLabel.textColor = gray_color;
        _priseLabel.font = [UIFont systemFontOfSize:13];
        
        _projectImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_disparage.png"]];
        
        _projectLabel = [[UILabel alloc] init];
        _projectLabel.backgroundColor = [UIColor clearColor];
        _projectLabel.textColor = gray_color;
        _projectLabel.font = [UIFont systemFontOfSize:13];
        
        _commentLabel = [[UILabel alloc] init];
        _commentLabel.backgroundColor = [UIColor clearColor];
        _commentLabel.textColor =[UIColor grayColor];
        _commentLabel.textAlignment = NSTextAlignmentRight;
        _commentLabel.font = [UIFont systemFontOfSize:13];
        
        _commentImgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"ic_arrow_grey.png"]];
        
        _produceInfoLabel = [[UILabel alloc] init];
        _produceInfoLabel.backgroundColor = [UIColor clearColor];
        _produceInfoLabel.textColor = [UIColor blackColor];
        _produceInfoLabel.text = @"产品信息";
        
        _descriptionLabel = [[UILabel alloc] init];
        _descriptionLabel.backgroundColor = [UIColor clearColor];
        _descriptionLabel.textColor = [UIColor grayColor];
        _descriptionLabel.text = @"本车场环境优雅，车位多，价格优惠！欢迎光临!";
        _descriptionLabel.numberOfLines = 0;
        _descriptionLabel.font = [UIFont systemFontOfSize:13];
        
        _knowHeaderLabel = [[UILabel alloc] init];
        _knowHeaderLabel.backgroundColor = [UIColor clearColor];
        _knowHeaderLabel.textColor = gray_color;
        _knowHeaderLabel.text = @"购买须知";
        
        
        _knowLabel = [[UILabel alloc] init];
        _knowLabel.backgroundColor = [UIColor clearColor];
        _knowLabel.textColor = [UIColor grayColor];
        _knowLabel.text = @"";
        _knowLabel.numberOfLines = 0;
        _knowLabel.font = [UIFont systemFontOfSize:15];
        
        _webView = [[UIWebView alloc] init];
        NSURLRequest* request = [[NSURLRequest alloc] initWithURL:[NSURL URLWithString:[TAPIUtility getNetworkWithUrl:@"presume.jsp"]]];
        [_webView loadRequest:request];
        _webView.scrollView.scrollEnabled = NO;
        
        [_commentView addSubview:_priseImgView];
        [_commentView addSubview:_priseLabel];
        [_commentView addSubview:_projectImgView];
        [_commentView addSubview:_projectLabel];
        [_commentView addSubview:_commentLabel];
        [_commentView addSubview:_commentImgView];
        
        [self addSubview:_parkInfoLabel];
        [self addSubview:_nameLabel];
        [self addSubview:_addressLabel];
        [self addSubview:_phoneButton];
        [self addSubview:_commentView];
        [self addSubview:_produceInfoLabel];
        [self addSubview:_descriptionLabel];
        [self addSubview:_knowHeaderLabel];
        [self addSubview:_knowLabel];
        [self addSubview:_webView];
        
        //添加所有  lineView
        [self createLineViews];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    _parkInfoLabel.frame = CGRectMake(padding, 0, 200, 30);
    _line1View.frame = CGRectMake(0, _parkInfoLabel.bottom, self.width, 0.5);
    _nameLabel.frame = CGRectMake(padding, _line1View.bottom, self.width - 2*padding, 30);
    _addressLabel.frame = CGRectMake(padding, _nameLabel.bottom, self.width - 2*padding, 30);
    _line2View.frame = CGRectMake(self.width - 43, _addressLabel.top, 1, 30);
    _phoneButton.frame = CGRectMake(self.width - 40, _addressLabel.top, 28, 28);
    
    _line3View.frame = CGRectMake(0, _addressLabel.bottom, self.width, 0.5);
    _commentView.frame = CGRectMake(0, _line3View.bottom, self.width, 30);
    _priseImgView.frame = CGRectMake(padding, 5, 20, 20);
    _priseLabel.frame = CGRectMake(_priseImgView.right + padding, 0, 60, 30);
    _projectImgView.frame = CGRectMake(_priseLabel.right + padding, 5, 20, 20);
    _projectLabel.frame = CGRectMake(_projectImgView.right + padding, 0, 60, 30);
    _commentLabel.frame = CGRectMake(self.width - 150, 0, 130, 30);
    _commentImgView.frame = CGRectMake(_commentLabel.right, 7, 15, 15);
    
    _line4View.frame = CGRectMake(0, _commentView.bottom, self.width, 2);
    _produceInfoLabel.frame = CGRectMake(padding, _line4View.bottom, self.width - 2*padding, 30);
    _line5View.frame = CGRectMake(0, _produceInfoLabel.bottom, self.width, 0.5);
    
    CGSize size = [TAPIUtility sizeWithFont:_descriptionLabel.font size:CGSizeMake(self.width, 200) text:_descriptionLabel.text];
    _descriptionLabel.frame = CGRectMake(padding, _line5View.bottom, self.width - 2*padding, size.height + 8);
    
    _line6View.frame = CGRectMake(0, _descriptionLabel.bottom, self.width, 2);
    _knowHeaderLabel.frame = CGRectMake(padding, _line6View.bottom, self.width- 2*padding, 30);
    
    _line7View.frame = CGRectMake(0, _knowHeaderLabel.bottom, self.width, 0.5);
    CGSize size2 = [TAPIUtility sizeWithFont:_knowLabel.font size:CGSizeMake(self.width - padding*2, 200) text:_knowLabel.attributedText.string];
    _knowLabel.frame = CGRectMake(padding, _line7View.bottom, self.width - padding*2, size2.height);
    
    _webView.frame = CGRectMake(padding, _knowLabel.bottom, self.width - padding*2, 230);
}

- (void)createLineViews {
    for (int i = 0; i< 7; i++) {
        UIView* view = [[UIView alloc] init];
        view.backgroundColor = light_white_color;
        [self addSubview:view];
        
        if (!_line1View)
            _line1View = view;
        else if (!_line2View)
            _line2View = view;
        else if (!_line3View)
            _line3View = view;
        else if (!_line4View)
            _line4View = view;
        else if (!_line5View)
            _line5View = view;
        else if (!_line6View)
            _line6View = view;
        else if (!_line7View)
            _line7View = view;
    }
}

- (void)setItem:(TParkMonthDetailItem *)item monthItem:(TParkMonthItem*)monthItem {
    _item = item;
    _monthItem = monthItem;
    
    _nameLabel.text = item.company_name;
    _addressLabel.text = item.address;
    _priseLabel.text = item.praiseNum;
    _projectLabel.text = item.disparageNum;
    
    _commentLabel.text = [NSString stringWithFormat:@"%@人评价", item.commentnum];
    if ([_item.resume isEqualToString:@""]) {
        _descriptionLabel.text = @"本车场环境优雅，车位多，价格优惠！欢迎光临!";
    } else {
        _descriptionLabel.text = _item.resume;
    }
    
    //组成一个长的字符串
    NSDate* date = [NSDate dateWithTimeIntervalSince1970:[_item.limitday integerValue]];
    NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd之前"];
    NSString* validDay = [formatter stringFromDate:date];
    
    NSString* fixedString = [_monthItem.reserved isEqualToString:@"0"] ? @"车位不固定，但能保定包月用户到现场有车位可停！" : @"车位固定，专用车位，一个独享";
    
    NSString* know = [NSString stringWithFormat:@"有效期:\n%@\n使用时间:\n日间(%@)\n车位是否固定:\n%@\n使用规则:", validDay, _monthItem.limittime, fixedString];
    NSMutableAttributedString* knowAttributeString = [[NSMutableAttributedString alloc] initWithString:know];
    NSRange range1 = [know rangeOfString:@"有效期:"];
    NSRange range2 = [know rangeOfString:@"使用时间:"];
    NSRange range3 = [know rangeOfString:@"车位是否固定:"];
    NSRange range4 = [know rangeOfString:@"使用规则:"];
    
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range1];
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range2];
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range3];
    [knowAttributeString addAttributes:@{NSForegroundColorAttributeName : orange_color} range:range4];
    _knowLabel.attributedText = knowAttributeString;
    
    [self layoutSubviews];
}

- (void)handleTapGesture:(UIGestureRecognizer*)gesture {
    if (_delegate && [_delegate respondsToSelector:@selector(cellCommentTouched)]) {
        [_delegate cellCommentTouched];
    }
}

- (void)phoneButtonTouched {
    
    if ([_item.mobile isEqualToString:@""]) {
        [TAPIUtility alertMessage:@"该停车场暂未提供电话" success:NO toViewController:nil];
        return;
    }
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", _item.mobile]]];
}

@end
